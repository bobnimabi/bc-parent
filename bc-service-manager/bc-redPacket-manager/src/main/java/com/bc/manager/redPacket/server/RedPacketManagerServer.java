package com.bc.manager.redPacket.server;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.CommonCode;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.*;
import com.bc.manager.redPacket.vo.*;
import com.bc.service.common.redPacket.entity.*;
import com.bc.service.common.redPacket.service.*;
import com.bc.utils.project.MyBeanUtil;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mrt on 2019/4/8 0008 下午 2:00
 */
@Service
public class RedPacketManagerServer {
    @Autowired
    private IVsAwardActiveService activeService;
    @Autowired
    private IVsAwardTransformService transformService;
    @Autowired
    private IVsAwardPlayerService playerService;
    @Autowired
    private IVsAwardPrizeService prizeService;
    @Autowired
    private IVsConfigureService configureService;
    @Autowired
    private IVsLogService logService;
    @Autowired
    private IVsMediaService mediaService;
    @Autowired
    private IVsPayRecordService recordService;
    @Autowired
    private IVsSiteService siteService;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private IVsNavService navService;

    //活动锁
    private ReentrantLock activeLock;
    //奖品锁
    private ReentrantLock prizeLock;
    //转换规则锁
    private ReentrantLock tranLock;


    /**
     * 红包活动修改
     */
    @Transactional
    public ResponseResult updateActive(VsAwardActiveDto activeDto) throws Exception {
        VsAwardActive active = new VsAwardActive();
        MyBeanUtil.copyProperties(activeDto, active);
        boolean flag = activeService.updateById(active);
        if (!flag) ExceptionCast.castFail("数据库更新失败");
        //删除过期的活动缓存
        if (!redis.delete(VarParam.RedPacketM.ACTIVE_KEY))
            ExceptionCast.castFail("缓存清理失败");
        return ResponseResult.SUCCESS();
    }

    /**
     * 获取红包活动(防止并发下缓存雪崩)
     * 双重检查锁+互斥锁+递归调用
     */
    private VsAwardActive getActive() throws Exception {
        String activeJson = redis.opsForValue().get(VarParam.RedPacketM.ACTIVE_KEY);
        if (null == activeJson) {
            if (activeLock.tryLock()) {
                activeJson = redis.opsForValue().get(VarParam.RedPacketM.ACTIVE_KEY);
                if (null == activeJson) {
                    VsAwardActive active = activeService.getById(VarParam.RedPacketM.AWARD_ACTIVE_ID);
                    if (active == null) ExceptionCast.castFail("数据库没有任何红包活动");
                    redis.opsForValue().set(VarParam.RedPacketM.ACTIVE_KEY, JSON.toJSONString(active));
                }
                activeLock.unlock();
            } else {
                Thread.sleep(100);
                return getActive();
            }
        }
        VsAwardActive active = JSON.parseObject(activeJson, VsAwardActive.class);
        return active;
    }

    /**
     * 红包活动的查看
     */
    public ResponseResult queryActive() throws Exception {
        VsAwardActive active = this.getActive();
        VsAwardActiveVo activeVo = new VsAwardActiveVo();
        MyBeanUtil.copyProperties(active,activeVo);
        return ResponseResult.SUCCESS(activeVo);
    }

    /**
     * 奖品管理:添加
     */
    @Transactional
    public ResponseResult addPrize(VsAwardPrizeDto prizeDto) throws Exception {
        VsAwardPrize prize = new VsAwardPrize();
        MyBeanUtil.copyProperties(prizeDto, prize);
        boolean save = prizeService.save(prize);
        if (!save) ExceptionCast.castFail("数据库添加失败");
        if (!redis.delete(VarParam.RedPacketM.PRIZE_KEY))
            ExceptionCast.castFail("缓存清理失败");
        return ResponseResult.SUCCESS();
    }

    /**
     * 奖品管理：删除
     */
    @Transactional
    public ResponseResult delPrize(List<Integer> ids) throws Exception {
        Long prizeSize = redis.opsForList().size(VarParam.RedPacketM.PRIZE_KEY);
        if (ids.size() >= prizeSize.intValue())
            return ResponseResult.FAIL("奖品不能全部删除！");
        boolean remove = prizeService.removeByIds(ids);
        if (!remove) ExceptionCast.castFail("数据库更新失败");
        if (!redis.delete(VarParam.RedPacketM.PRIZE_KEY))
            ExceptionCast.castFail("缓存清理失败");
        return ResponseResult.SUCCESS();
    }

    /**
     * 奖品管理：修改
     */
    @Transactional
    public ResponseResult updatePrize(VsAwardPrizeDto prizeDto) throws Exception {
        VsAwardPrize prize = new VsAwardPrize();
        MyBeanUtil.copyProperties(prizeDto, prize);
        boolean update = prizeService.updateById(prize);
        if (!update) ExceptionCast.castFail("数据库更新失败");
        if (!redis.delete(VarParam.RedPacketM.PRIZE_KEY))
            ExceptionCast.castFail("缓存清理失败");
        return ResponseResult.SUCCESS();
    }

    /**
     * 获取所有奖品
     */
    public List<VsAwardPrize> getPrizes() throws Exception{
        List<String> prizesJsons = redis.opsForList().range(VarParam.RedPacketM.PRIZE_KEY, 0, -1);
        if (CollectionUtils.isEmpty(prizesJsons)){
            if (prizeLock.tryLock()) {
                prizesJsons = redis.opsForList().range(VarParam.RedPacketM.PRIZE_KEY, 0, -1);
                if (CollectionUtils.isEmpty(prizesJsons)) {
                    //从数据库获取，放入缓存
                    List<VsAwardPrize> prizeList = prizeService.list();
                    if (CollectionUtils.isEmpty(prizeList)) ExceptionCast.castFail("数据库没有任何奖品");
                    //按照金额从小到大排序
                    //奖品排序
                    prizeList.sort(new Comparator<VsAwardPrize>() {
                        @Override
                        public int compare(VsAwardPrize o1, VsAwardPrize o2) {
                            return o1.getTotalAmount().compareTo(o2.getTotalAmount());
                        }
                    });
                    List<String> prizeStrList = null;
                    for (VsAwardPrize prize : prizeList) {
                        prizeStrList.add(JSON.toJSONString(prize));
                    }
                    redis.opsForList().leftPushAll(VarParam.RedPacketM.PRIZE_KEY,prizeStrList);
                }
                prizeLock.unlock();
            } else {
                Thread.sleep(100);
                return getPrizes();
            }
        }
        List<VsAwardPrize> prizeList=  null;
        for (String prizeJson: prizesJsons) {
            VsAwardPrize prize = JSON.parseObject(prizeJson, VsAwardPrize.class);
            prizeList.add(prize);
        }
        return prizeList;
    }

    /**
     * 查看奖品
     */
    public ResponseResult queryPrizes() throws Exception{
        List<VsAwardPrize> prizes = this.getPrizes();
        List<VsAwardPrizeVo> prizeVos = MyBeanUtil.copyListToList(prizes, VsAwardPrizeVo.class);
        //根据红包金额排序
        Collections.sort(prizeVos, new Comparator<VsAwardPrizeVo>() {
            @Override
            public int compare(VsAwardPrizeVo o1, VsAwardPrizeVo o2) {
                return o1.getTotalAmount().compareTo(o2.getTotalAmount());
            }
        });
        return ResponseResult.SUCCESS(prizeVos);
    }
    /**
     * 奖品管理：根据名称查看
     */
    public ResponseResult queryPrizeByName(VsAwardPrizeDto prizeDto) throws Exception{
        List<VsAwardPrize> prizes = this.getPrizes();
        Iterator<VsAwardPrize> iterator = prizes.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getPrizeName().contains(prizeDto.getPrizeName())) {
                iterator.remove();
            }
        }
        if(!CollectionUtils.isEmpty(prizes)){
            //根据红包金额排序
            List<VsAwardPrizeVo> prizeVos = MyBeanUtil.copyListToList(prizes, VsAwardPrizeVo.class);
            Collections.sort(prizeVos, new Comparator<VsAwardPrizeVo>() {
                @Override
                public int compare(VsAwardPrizeVo o1, VsAwardPrizeVo o2) {
                    return o1.getTotalAmount().compareTo(o2.getTotalAmount());
                }
            });
            return ResponseResult.SUCCESS(prizeVos);
        }
        return ResponseResult.SUCCESS(Collections.emptyList());
    }

    /**
     * 奖品管理：根据奖品Id查看
     */
    public ResponseResult queryPrizeById(VsAwardPrizeDto prizeDto) throws Exception {
        List<VsAwardPrize> prizes = this.getPrizes();
        Iterator<VsAwardPrize> iterator = prizes.iterator();
        while (iterator.hasNext()) {
            VsAwardPrize prize = iterator.next();
            if (prize.getId() == prizeDto.getId()) {
                return ResponseResult.SUCCESS(prize);
            }
        }
        return ResponseResult.FAIL("奖品的id有误");
    }
    /**
     * 奖品管理：下架
     */
    @Transactional
    public ResponseResult prizeSoldOut(VsAwardPrizeDto prizeDto) throws Exception {
        prizeDto.setPrizeStatus(VarParam.NO);
        VsAwardPrize prize = new VsAwardPrize();
        MyBeanUtil.copyProperties(prizeDto,prize);
        boolean updateById = prizeService.updateById(prize);
        if (!updateById) ExceptionCast.castFail("数据库更新失败");
        //删除缓存
        if (!redis.delete(VarParam.RedPacketM.PRIZE_KEY))
            ExceptionCast.castFail("缓存清理失败");
        return ResponseResult.SUCCESS();
    }

    /**
     * 转换规则:添加
     */
    @Transactional
    public ResponseResult addTransform(VsAwardTransformDto transformDto) throws Exception{
        VsAwardTransform transform = new VsAwardTransform();
        MyBeanUtil.copyProperties(transformDto, transform);
        boolean save = transformService.save(transform);
        if (!save) ExceptionCast.castFail("数据库存入失败");
        if (!redis.delete(VarParam.RedPacketM.TRANSFORM_KEY))
            ExceptionCast.castFail("缓存清理失败");
        return ResponseResult.SUCCESS();
    }

    private List<VsAwardTransform> getTransforms() throws Exception{
        List<String> tranJsons = redis.opsForList().range(VarParam.RedPacketM.TRANSFORM_KEY, 0, -1);
        if (CollectionUtils.isEmpty(tranJsons)) {
            if (tranLock.tryLock()) {
                tranJsons = redis.opsForList().range(VarParam.RedPacketM.TRANSFORM_KEY, 0, -1);
                if (CollectionUtils.isEmpty(tranJsons)) {
                    List<VsAwardTransform> transforms = transformService.list();
                    if (CollectionUtils.isEmpty(transforms)) {
                        ExceptionCast.castFail("转换规则数据库没有，请立刻设置");
                    }
                    List<String> list = new ArrayList<>();
                    transforms.forEach(tran -> list.add(JSON.toJSONString(tran)));
                    redis.opsForList().leftPushAll(VarParam.RedPacketM.TRANSFORM_KEY, list);
                }
                tranLock.unlock();
            } else {
                Thread.sleep(100);
                return getTransforms();
            }
        }
        List<VsAwardTransform> transformList = new ArrayList<>();
        tranJsons.forEach(tranStr->{
            VsAwardTransform transform = JSON.parseObject(tranStr, VsAwardTransform.class);
            transformList.add(transform);
        });
        //从小到大排序
        transformList.sort(new Comparator<VsAwardTransform>() {
            @Override
            public int compare(VsAwardTransform o1, VsAwardTransform o2) {
                return o1.getAmount().compareTo(o2.getAmount());
            }
        });
        return transformList;
    }

    /**
     * 转换规则：查看所有转换规则
     */
    public ResponseResult queryTransforms() throws Exception{
        List<VsAwardTransform> transforms = this.getTransforms();
        List<VsAwardTransformVo> transformVos = MyBeanUtil.copyListToList(transforms, VsAwardTransformVo.class);

        //按抽奖次数排序
        Collections.sort(transformVos, new Comparator<VsAwardTransformVo>() {
            @Override
            public int compare(VsAwardTransformVo o1, VsAwardTransformVo o2) {
                return o1.getTimes() - o2.getTimes();
            }
        });
        return ResponseResult.SUCCESS(transformVos);
    }

    /**
     * 转换规则：删除
     */
    @Transactional
    public ResponseResult delTransformById(VsAwardTransformDto transformDto) throws Exception{
        boolean removeById = transformService.removeById(transformDto.getId());
        if (!removeById) ExceptionCast.castFail("数据库删除失败");
        if (!redis.delete(VarParam.RedPacketM.TRANSFORM_KEY)) {
            ExceptionCast.castFail("缓存删除失败");
        }
        return ResponseResult.SUCCESS();
    }

    /**
     * 转换规则：根据id查看
     */
    public ResponseResult queryTransformById(VsAwardTransformDto transformDto) throws Exception{
        List<VsAwardTransform> transforms = this.getTransforms();
        for (VsAwardTransform tran : transforms) {
            if (tran.getId() == transformDto.getId()) {
                return ResponseResult.SUCCESS(tran);
            }
        }
        return ResponseResult.FAIL("未找到");
    }
    /**
     * 转换规则：更新
     */
    @Transactional
    public ResponseResult updateTransform(VsAwardTransformDto transformDto) throws Exception{
        VsAwardTransform transform = new VsAwardTransform();
        MyBeanUtil.copyProperties(transformDto,transform);
        boolean update = transformService.updateById(transform);
        if (!update) ExceptionCast.castFail("数据库更新失败");

        if (!redis.delete(VarParam.RedPacketM.TRANSFORM_KEY))
            ExceptionCast.castFail("缓存删除失败");
        return ResponseResult.SUCCESS();
    }

    /**
     * 会员管理：单笔添加
     */
    @Transactional
    public ResponseResult addPlayer(VsAwardPlayerDto playerDto) throws Exception{
        VsAwardPlayer player = new VsAwardPlayer();
        MyBeanUtil.copyProperties(playerDto, player);
        boolean save = playerService.save(player);
        if (!save) ExceptionCast.cast(CommonCode.FAIL);
        return ResponseResult.SUCCESS();
    }

    /**
     * 会员管理：条件分页查询
     */
    public ResponseResult queryPlayersByCriteria(VsAwardPlayerDto playerDto) throws Exception{
        //拼装条件
        QueryWrapper<VsAwardPlayer> queryWrapper = new QueryWrapper<>();
        if (null != playerDto.getPlayerStatus()) {
            queryWrapper.eq("player_status", playerDto.getPlayerStatus());
        }
        if (null != playerDto.getOrderBy()) {
            if (VarParam.RedPacketM.PLAYER_CREATTIME_DESC == playerDto.getOrderBy()) {
                queryWrapper.orderByDesc("create_time");
            } else if (VarParam.RedPacketM.PLAYER_AMOUNT_DESC == playerDto.getOrderBy()) {
                queryWrapper.orderByDesc("has_amount");
            } else if (VarParam.RedPacketM.PLAYER_JOINTIME_DESC == playerDto.getOrderBy()) {
                queryWrapper.orderByDesc("join_times");
            }
        }
        if (!StringUtils.isEmpty(playerDto.getUserName())) {
            queryWrapper.likeRight("user_name", playerDto.getUserName());
        }

        IPage<VsAwardPlayer> page = playerService.page(new Page<VsAwardPlayer>(playerDto.getCurrent(), playerDto.getSize()), queryWrapper);
        return ResponseResult.SUCCESS(MyBeanUtil.copyPageToPage(page, VsAwardPlayerVo.class));
    }

    /**
     * 会员管理：按照id查询
     */
    public ResponseResult queryPlayersById(VsAwardPlayerDto playerDto) throws Exception{
        VsAwardPlayer player = playerService.getById(playerDto.getId());
        if (null == player) {
            return ResponseResult.FAIL("未查询到任何信息");
        }
        return ResponseResult.SUCCESS(player);
    }

    /**
     * 会员管理：修改
     */
    @Transactional
    public ResponseResult updatePlayer(VsAwardPlayerDto playerDto) throws Exception{
        VsAwardPlayer player = new VsAwardPlayer();
        MyBeanUtil.copyProperties(playerDto, player);
        boolean updateById = playerService.updateById(player);
        if (updateById) {
            return ResponseResult.SUCCESS();
        }
        ExceptionCast.cast(CommonCode.FAIL);
        return null;
    }
    /**
     * 会员管理：下架
     */
    @Transactional
    public ResponseResult playerSoldOut(VsAwardPlayerDto playerDto) throws Exception{
        boolean update = playerService.update(
                new UpdateWrapper<VsAwardPlayer>()
                        .eq("id", playerDto.getId())
                        .set("active_status", VarParam.NO)
        );
        if (!update) ExceptionCast.cast(CommonCode.FAIL);
        return ResponseResult.SUCCESS();
    }

    /**
     * 会员管理：清除会员
     */
    @Transactional
    public ResponseResult delPlayers(VsAwardPlayerDto playerDto) throws Exception{
        //删除days天前的会员
        boolean remove = playerService.remove(
                new QueryWrapper<VsAwardPlayer>()
                        .lt("create_time", LocalDateTime.now().minusDays(playerDto.getDays()))
            );
        if (!remove) ExceptionCast.castFail("批量会员清除失败");
        return ResponseResult.SUCCESS();
    }


    /**
     * 会员管理：批量导入
     */
    @Transactional
    public ResponseResult readExcelPlays(List<Object> list) throws Exception{
        List<VsAwardPlayer> exPlayers = MyBeanUtil.copyListToList(list, VsAwardPlayer.class);
        //将金额全部转换成次数
        List<VsAwardTransform> transforms = getTransforms();
        int size = transforms.size();
        exPlayers.forEach(exPlay->{
            int i = 1;
            while (i <= size) {
                if (exPlay.getHasAmount().compareTo(transforms.get(size - i).getAmount()) >= 0) {
                    exPlay.setJoinTimes(exPlay.getJoinTimes() + transforms.get(size - i).getTimes());
                    exPlay.setHasAmount(BigDecimal.ZERO);
                }
            }
        });

        List<String> userNameList = new ArrayList<>();
        exPlayers.forEach(player->userNameList.add(player.getUserName()));

        VsConfigure configure = configureService.getOne(
                new QueryWrapper<VsConfigure>().eq("configure_key", "players_import_type"));
        if (null == configure) {
            ExceptionCast.castFail("请设置导入Excel用户类型");
        }
        int playersImportType = Integer.parseInt(configure.getConfigureValue());

        if (playersImportType == VarParam.RedPacketM.IMPORT_PLAYERS_TYPE_ONE) {
            //导入会员类型：覆盖
            boolean remove = playerService.remove(
                    new QueryWrapper<VsAwardPlayer>()
                            .in("user_name", userNameList)
            );
            if (!remove)  ExceptionCast.castFail("删除已存在的会员失败");
            if (!playerService.saveBatch(exPlayers))  ExceptionCast.castFail("批量会员入库失败");
            return ResponseResult.SUCCESS();
        }

        //导入会员类型：累加
        //如果该会员已经存在，则增加次数
        List<VsAwardPlayer> dataPlays = playerService.list(
                new QueryWrapper<VsAwardPlayer>()
                        .select("id","user_name as userName","join_times as joinTimes")
                        .in("user_name", userNameList)
        );
        if (!CollectionUtils.isEmpty(dataPlays)){
            Iterator<VsAwardPlayer> iterator = exPlayers.iterator();
            dataPlays.forEach(dataPlay->{
                while (iterator.hasNext()) {
                    VsAwardPlayer exPlayer = iterator.next();
                    if (dataPlay.getUserName().equals(exPlayer.getUserName())) {
                        dataPlay.setJoinTimes(dataPlay.getJoinTimes() + exPlayer.getJoinTimes());
                        iterator.remove();
                    }
                }
            });
            boolean updateBatchById = playerService.updateBatchById(dataPlays);
            if (!updateBatchById) {
                ExceptionCast.castFail("更新已有用户的抽红包次数失败");
            }
        }
        if (!CollectionUtils.isEmpty(exPlayers)){
            boolean saveBatch = playerService.saveBatch(exPlayers);
            if (!saveBatch) {
                ExceptionCast.castFail("批量添加新用户失败");
            }
        }
        return ResponseResult.SUCCESS();
    }

    /**
     * 会员管理：批量删除
     */
    public ResponseResult delPlayersBatch(List<Integer> ids) throws Exception{
        boolean removeByIds = playerService.removeByIds(ids);
        if (!removeByIds) {
            ExceptionCast.castFail("批量删除失败");
        }
        return ResponseResult.SUCCESS();
    }
    /**
     * 抽奖记录：删除
     */
    public ResponseResult delRecordBatch(List<Integer> ids) {
        boolean removeByIds = recordService.removeByIds(ids);
        if (!removeByIds) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        return ResponseResult.SUCCESS();
    }

    /**
     * 抽奖记录：条件查询
     */
    public Page<VsPayRecordVo> queryRecordByCriteria(VsPayRecordDto recordDto) throws Exception{
        QueryWrapper<VsPayRecord> queryWrapper = new QueryWrapper<>();
        if (null != recordDto.getPayStatus()) {
            queryWrapper.eq("pay_status", recordDto.getPayStatus());
        }
        if (null != recordDto.getStartTime()) {
            queryWrapper.gt("time_order", recordDto.getStartTime());
        }
        if (null != recordDto.getEndTime()) {
            queryWrapper.lt("time_order", recordDto.getEndTime());
        }
        if (!StringUtils.isEmpty(recordDto.getUserName())) {
            queryWrapper.eq("id", recordDto.getUserName())
                        .or()
                        .eq("user_name", recordDto.getUserName());
        }
        IPage<VsPayRecord> page = recordService.page(new Page<VsPayRecord>(recordDto.getCurrent(), recordDto.getSize()), queryWrapper);
        Page<VsPayRecordVo> vsPayRecordVoPage = MyBeanUtil.copyPageToPage(page, VsPayRecordVo.class);
        return vsPayRecordVoPage;

    }
    /**
     * 抽奖记录：导出Excel
     */
    public List<VsPayRecordVo> queryRecordByCriteria2(VsPayRecordDto recordDto) throws Exception{
        QueryWrapper<VsPayRecord> queryWrapper = new QueryWrapper<>();
        if (null != recordDto.getPayStatus()) {
            queryWrapper.eq("pay_status", recordDto.getPayStatus());
        }
        if (null != recordDto.getStartTime()) {
            queryWrapper.gt("time_order", recordDto.getStartTime());
        }
        if (null != recordDto.getEndTime()) {
            queryWrapper.lt("time_order", recordDto.getEndTime());
        }
        if (!StringUtils.isEmpty(recordDto.getUserName())) {
            queryWrapper.eq("id", recordDto.getUserName())
                    .or()
                    .eq("user_name", recordDto.getUserName());
        }
        List<VsPayRecord> list = recordService.list(queryWrapper);
        return MyBeanUtil.copyListToList(list, VsPayRecordVo.class);
    }

    /**
     * 抽奖记录：清除
     */
    public ResponseResult delRecords(VsPayRecordDto recordDto) throws Exception{
        //删除days天前的会员
        boolean remove = recordService.remove(
                new QueryWrapper<VsPayRecord>()
                        .lt("create_time", LocalDateTime.now().minusDays(recordDto.getDays()))
        );
        if (!remove) ExceptionCast.castFail("批量订单清除失败");
        return ResponseResult.SUCCESS();
    }
    /**
     * 抽奖记录：统计
     */
    public ResponseResult statisRecords() throws Exception{
        StaticRecordsVo staticRecordsVo = new StaticRecordsVo();

        //查询今日每小时赠送明细
        BigDecimal[] dayHour = staticRecordsVo.getDayHourAmount();
        for (int i = 0; i < dayHour.length; i++) {
            dayHour[i] = BigDecimal.ZERO;
        }
        staticRecordsVo.setBeforeAmount(BigDecimal.ZERO);
        staticRecordsVo.setTodayAmount(BigDecimal.ZERO);
        staticRecordsVo.setYestAmount(BigDecimal.ZERO);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);//今天零点
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);//今天24点

        List<VsPayRecord> todayRecords = recordService.list(
                new QueryWrapper<VsPayRecord>()
                        .select("id","total_amount as totalAmount","create_time as createTime")
                        .between("create_time", todayStart, todayEnd)
        );
        todayRecords.forEach(record->{
            int hour = record.getCreateTime().getHour();
            dayHour[hour] = dayHour[hour].add(record.getTotalAmount());
        });

        LocalDateTime beforeStart = LocalDateTime.of(LocalDate.now().minusDays(2), LocalTime.MIN);//前天零点
        LocalDateTime todayEnd2 = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);//今天24点
        int dayOfYear = LocalDateTime.now().getDayOfYear();
        List<VsPayRecord> threeDayRecords = recordService.list(
                new QueryWrapper<VsPayRecord>()
                        .between("create_time", beforeStart, todayEnd2)
        );
        threeDayRecords.forEach(record->{
            if (dayOfYear == record.getCreateTime().getDayOfYear()){
                staticRecordsVo.setTodayAmount(staticRecordsVo.getTodayAmount().add(record.getTotalAmount()));
            }else if (record.getCreateTime().getDayOfYear() == dayOfYear-1){
                staticRecordsVo.setYestAmount(staticRecordsVo.getYestAmount().add(record.getTotalAmount()));
            } else if (record.getCreateTime().getDayOfYear() == dayOfYear - 2) {
                staticRecordsVo.setBeforeAmount(staticRecordsVo.getBeforeAmount().add(record.getTotalAmount()));
            }
        });
        return ResponseResult.SUCCESS(staticRecordsVo);
    }

    /**
     * 品牌设置：修改
     */
    public ResponseResult updateBrand(VsSiteDto siteDto) throws Exception{
        VsSite site = new VsSite();
        MyBeanUtil.copyProperties(siteDto,site);
        boolean updateById = siteService.updateById(site);
        if (!updateById) ExceptionCast.castFail("更新失败");
        return ResponseResult.SUCCESS();
    }

    /**
     * 导航设置：添加
     */
    public ResponseResult navAdd(VsNavDto navDto) throws Exception{
        VsNav nav = new VsNav();
        MyBeanUtil.copyProperties(navDto,nav);
        boolean save = navService.save(nav);
        if (!save) {
            ExceptionCast.castFail("添加失败");
        }
        return ResponseResult.SUCCESS("添加成功");
    }

    /**
     * 导航查询
     */
    public ResponseResult navQuery(Page page) throws Exception{
        IPage page1 = navService.page(page);
        return ResponseResult.SUCCESS(page1);
    }
    /**
     * 导航删除
     */
    public ResponseResult navDel(VsNavDto navDto) throws Exception{
        boolean removeById = navService.removeById(navDto.getId());
        if (!removeById) {
            ExceptionCast.castFail("删除失败");
        }
        return null;
    }

    /**
     * 导航根据id查询
     */
    public ResponseResult navQueryById(VsNavDto navDto) throws Exception{
        VsNav byId = navService.getById(navDto.getId());
        if (null == byId) {
            ExceptionCast.castFail("查询失败");
        }
        return ResponseResult.SUCCESS(byId);
    }

    /**
     * 配置项：添加
     */
    public ResponseResult configAdd(VsConfigureDto configureDto) throws Exception{
        VsConfigure configure = new VsConfigure();
        MyBeanUtil.copyProperties(configureDto, configure);
        boolean save = configureService.save(configure);
        if (!save) {
            ExceptionCast.castFail("存入失败");
        }
        return ResponseResult.SUCCESS();
    }

    /**
     *配置项：查询
     */
    public ResponseResult configQuery(Page page) throws Exception{
        IPage page1 = configureService.page(page);
        return ResponseResult.SUCCESS(page1);
    }

    /**
     *配置项：删除
     */
    public ResponseResult configDel(VsConfigureDto configureDto) throws Exception{
        boolean removeById = configureService.removeById(configureDto.getId());
        if (!removeById) {
            ExceptionCast.castFail("删除失败");
        }
        return ResponseResult.SUCCESS();
    }

    /**
     * 配置项：根据id查询
     */
    public ResponseResult configQueryById(VsConfigureDto configureDto) throws Exception{
        VsConfigure byId = configureService.getById(configureDto.getId());
        if (null == byId) {
            ExceptionCast.castFail("查询失败");
        }
        return ResponseResult.SUCCESS(byId);
    }

    /**
     * 配置项：修改
     */
    public ResponseResult configUpdate(VsConfigureDto configureDto) throws Exception{
        VsConfigure configure = new VsConfigure();
        MyBeanUtil.copyProperties(configureDto, configure);
        boolean updateById = configureService.updateById(configure);
        if (!updateById) {
            ExceptionCast.castFail("修改失败");
        }
        return ResponseResult.SUCCESS();
    }
}
