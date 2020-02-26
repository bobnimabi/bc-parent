package com.bc.service.redPacket.server;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bc.common.Exception.CustomException;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.CommonCode;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.IdLongList;
import com.bc.manager.redPacket.dto.VsPayRecordDto;
import com.bc.manager.redPacket.vo.VsAwardActiveVo;
import com.bc.manager.redPacket.vo.VsPayRecordVo;
import com.bc.service.common.redPacket.entity.*;
import com.bc.service.common.redPacket.service.*;
import com.bc.service.redPacket.dto.RedPacketDto;
import com.bc.service.redPacket.dto.TaskAtom;
import com.bc.service.redPacket.vo.RedResultVo;
import com.bc.service.redPacket.exception.RedCode;
import com.bc.utils.DateUtil;
import com.bc.utils.MyBloomFilter;
import com.bc.utils.project.MyBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Created by mrt on 2019/4/8 0008 下午 12:25
 */
//@Service
@Slf4j
public class RedPacketServer {
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
    @Autowired
    private RobotServer robotServer;

    @Autowired
    private IVsHtmlService htmlService;

    //活动锁
    private ReentrantLock activeLock = new ReentrantLock();
    //奖品锁
    private ReentrantLock prizeLock = new ReentrantLock();

    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void initBloomFilter() throws Exception{
        log.info("布隆过滤器初始化：开始");
        //删除原始的缓存
        redis.delete(VarParam.RedPacketM.BLOOM_RED);

        //为用户建立布隆过滤器
        Page<VsAwardPlayer> page = new Page<>(0L,100);
        page.setCurrent(0);//<1时默认current=1
        while (true) {
            page.setCurrent(page.getCurrent() + 1);
            Page<VsAwardPlayer> page1 = (Page<VsAwardPlayer>)playerService.page(page);
            List<VsAwardPlayer> records = page1.getRecords();
            for (VsAwardPlayer player : records) {
                MyBloomFilter.put(
                        VarParam.RedPacketM.BLOOM_RED,
                        player.getUserName(),
                        VarParam.RedPacketM.SIZE_RED,
                        VarParam.RedPacketM.FPP_RED,
                        redis
                );
            }
            if (!page1.hasNext()) {
                break;
            }
        }
        log.info("布隆过滤器初始化：结束");
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
     * 抽红包总调度
     */
    public ResponseResult playRedPacket(RedPacketDto redPacketDto) throws Exception{
        String username = redPacketDto.getUsername();
        //检查活动
        this.checkActive();
        //检查用户并获取userId
        VsAwardPlayer player = this.checkUser(username);
        //检查奖品并获取可用奖品
        List<VsAwardPrize> prizes = this.checkPrize();
        return userPlay(player,redPacketDto);
    }
    /**
     * 获取红包活动(防止并发和缓存雪崩)
     * 双重检查锁+互斥锁+递归调用
     */
    private VsAwardActive getActive() throws Exception {
        String activeJson = redis.opsForValue().get(VarParam.RedPacketM.ACTIVE_KEY);
        if (null == activeJson) {
            VsAwardActive active = null;
            if (activeLock.tryLock()) {
                activeJson = redis.opsForValue().get(VarParam.RedPacketM.ACTIVE_KEY);
                if (null == activeJson) {
                    active = activeService.getById(VarParam.RedPacketM.AWARD_ACTIVE_ID);
                    if (active == null) ExceptionCast.castFail("数据库没有任何红包活动");
                    redis.opsForValue().set(VarParam.RedPacketM.ACTIVE_KEY, JSON.toJSONString(active));
                }
                activeLock.unlock();
                if (null != active) return active;
            } else {
                Thread.sleep(100);
                return getActive();
            }
        }
        VsAwardActive active = JSON.parseObject(activeJson, VsAwardActive.class);
        return active;
    }

    /**
     * 活动：查看活动状态
     */
    private boolean checkActive() throws Exception{
        VsAwardActive active = this.getActive();

        //查看活动是否开启
        if (VarParam.NO == active.getActiveStatus()) {
            ExceptionCast.castFail("对不起，活动未开启");
        }

        //校验总活动时间
        if (active.getTimeStart().isAfter(LocalDateTime.now())) {
            ExceptionCast.castFail("活动开始时间："+active.getTimeStart().getMonthValue()+"月"+active.getTimeStart().getDayOfMonth()+"日");
        }
        if (active.getTimeEnd().isBefore(LocalDateTime.now())) {
            ExceptionCast.castFail("活动在 "+active.getTimeEnd().getMonthValue()+"月"+active.getTimeEnd().getDayOfMonth()+"日"+" 已结束");
        }

        String time = DateUtil.HOUR_MIN_TWO.format(LocalDateTime.now());
        //校验日活动时间
        if (active.getDayTimeStart().compareTo(time) > 0) {
            ExceptionCast.castFail("活动每日开始时间："+active.getDayTimeStart());
        }
        if (active.getDayTimeEnd().compareTo(time) < 0) {
            ExceptionCast.castFail("活动每日结束时间："+active.getDayTimeEnd());
        }
        return true;
    }

    /**
     * 会员:状态检查
     */
    private VsAwardPlayer checkUser(String userName) throws Exception{
        //查询用户是否存在
        //布隆过滤器防止恶意点击
        boolean exist = MyBloomFilter.isExist(
                VarParam.RedPacketM.BLOOM_RED,
                userName,
                VarParam.RedPacketM.SIZE_RED,
                VarParam.RedPacketM.FPP_RED, redis
        );
        if (!exist) ExceptionCast.castFail("该账号不存在");

        VsAwardPlayer player = playerService.getOne(new QueryWrapper<VsAwardPlayer>().eq("user_name", userName));
        if (null == player) ExceptionCast.castFail("账号不存在");

        //检查账号是否被锁定
        if (VarParam.NO == player.getPlayerStatus())
            ExceptionCast.castFail("账号被锁定");

        //检查账号库存
        if (player.getJoinTimes() <=0)
            ExceptionCast.cast(RedCode.TIMES_NOT_ENOUGH);
        return player;
    }

    /**
     * 获取所有奖品(防止并发和缓存雪崩)
     */
    private List<VsAwardPrize> getPrizes() throws Exception{
        List<String> prizesJsons = redis.opsForList().range(VarParam.RedPacketM.PRIZE_KEY, 0, -1);
        if (CollectionUtils.isEmpty(prizesJsons)){
            List<VsAwardPrize> prizeList = null;
            if (prizeLock.tryLock()) {
                prizesJsons = redis.opsForList().range(VarParam.RedPacketM.PRIZE_KEY, 0, -1);
                if (CollectionUtils.isEmpty(prizesJsons)) {
                    //从数据库获取，放入缓存
                    prizeList = prizeService.list();
                    if (CollectionUtils.isEmpty(prizeList)) ExceptionCast.castFail("数据库没有任何奖品");
                    //按照金额从小到大排序
                    prizeList.sort(new Comparator<VsAwardPrize>() {
                        @Override
                        public int compare(VsAwardPrize o1, VsAwardPrize o2) {
                            return o1.getTotalAmount().compareTo(o2.getTotalAmount());
                        }
                    });
                    List<String> prizeStrList = new ArrayList<>();
                    for (VsAwardPrize prize : prizeList) {
                        prizeStrList.add(JSON.toJSONString(prize));
                    }
                    redis.opsForList().leftPushAll(VarParam.RedPacketM.PRIZE_KEY,prizeStrList);
                }
                prizeLock.unlock();
                if (!CollectionUtils.isEmpty(prizeList)) return prizeList;
            } else {
                Thread.sleep(100);
                return getPrizes();
            }
        }
        List<VsAwardPrize> prizeList= new ArrayList<>();
        for (String prizeJson: prizesJsons) {
            VsAwardPrize prize = JSON.parseObject(prizeJson, VsAwardPrize.class);
            prizeList.add(prize);
        }
        return prizeList;
    }

    /**
     * 奖品的检查
     */
    private List<VsAwardPrize> checkPrize() throws Exception{
        //获取所有的奖品，并去除下架的和库存为0的
        List<VsAwardPrize> prizes = this.getPrizes();
        Iterator<VsAwardPrize> iterator = prizes.iterator();
        while (iterator.hasNext()) {
            VsAwardPrize prize = iterator.next();
            if (VarParam.NO == prize.getPrizeStatus() || prize.getPrizeStoreNums() <= 0)
                iterator.remove();
        }
        if (CollectionUtils.isEmpty(prizes)) {
            ExceptionCast.castFail("所有奖品已下架或库存为0");
        }

        return prizes;
    }

    /**
     * 抽红包开始
     * 1.随机产生红包（经典抽奖算法）
     * 2.减少抽到的redis奖品的库存(redis乐观锁，防止并发高载，如果这里出错显示“系统繁忙”)
     *      异步通知另一个线程获取redis的库存更新到数据库
     * 3.减少用户的库存(mysql乐观锁)，防止有人故意在同一时刻高速刷红包
     * 4.生成订单
     * 5.入队列（双端队列+监听器模式），红包入队需要排序（11秒内不能重复派单），
     *      每个线程监听一个队列，有任务就不停的执行
     * 6.打款（httpclient），双机器人监控双队列（打款钱使用myql乐观锁，防止抖动重复打款）
     * 7.打款完成后修改订单状态
     */
    @Transactional
    public ResponseResult userPlay(VsAwardPlayer player,RedPacketDto redPacketDto) throws Exception{
        RedResultVo redResultVo = new RedResultVo();
        //先watch，获取要修改的值，多命令
        redis.watch(VarParam.RedPacketM.PRIZE_KEY);
        List<VsAwardPrize> prizeList = this.checkPrize();
        log.info("抽奖前红包打印：{}", JSON.toJSONString(prizeList));
        //开启事务(预备减库存)
        redis.setEnableTransactionSupport(true);
        redis.multi();
        //抽奖
        int i = drawLottery(prizeList);
        VsAwardPrize prizeNew = prizeList.get(i);
        log.info("username:" + player.getUserName() + ",抽得：" + prizeNew.getPrizeName() + " index：" + i);

        //未中奖
        if (VarParam.RedPacketM.PRIZE_TYPE_TWO == prizeNew.getPrizeType()) {
            redResultVo.setIsSuccess(false);
            redResultVo.setAmount(BigDecimal.ZERO);
            log.info("username:" + player.getUserName() + ",抽得：" + prizeNew.getPrizeName() + " index：" + i + " 返回未中奖");
            return ResponseResult.SUCCESS(redResultVo);
        }
        if (prizeNew.getPrizeStoreNums() > 0) {
            //减少库存
            prizeNew.setPrizeStoreNums(prizeNew.getPrizeStoreNums() - 1);
            prizeNew.setPrizeDrawNums(prizeNew.getPrizeDrawNums() + 1);
            if (prizeNew.getPrizeStoreNums() == 0) {
                prizeNew.setPrizeStatus(VarParam.NO);
            }
            //将对应的奖品库存放入缓存
            redis.opsForList().set(VarParam.RedPacketM.PRIZE_KEY, i, JSON.toJSONString(prizeNew));
            redis.opsForList().size(VarParam.RedPacketM.PRIZE_KEY);//这个查询只是希望exec有不为empty，因为set返回值为void
            //执行
            List<Object> exec = redis.exec();
            redis.setEnableTransactionSupport(false);//关闭事务
            if (CollectionUtils.isEmpty(exec)) {//执行失败
                log.error(" redis乐观锁:更新库存失败" + " username:" + player.getUserName() + ",抽得：" + prizeNew.getPrizeName() + " index：" + i);
                ExceptionCast.cast(CommonCode.SERVER_ERROR);
            } else {//执行成功robotQuery
                log.info(" redis乐观锁:更新库存成功" + " username:" + player.getUserName() + ",抽得：" + prizeNew.getPrizeName() + " index：" + i);
                updatePrizeStore(prizeNew);
                redResultVo.setIsSuccess(true);
                redResultVo.setAmount(prizeNew.getTotalAmount());
            }
        } else {//库存不足
            log.error("username:" + player.getUserName() + ",抽得：" + prizeNew.getPrizeName() + " 库存不足");
            ExceptionCast.cast(CommonCode.SERVER_ERROR);
        }


        //减少用户的库存（mysql乐观锁）
        boolean update = playerService.update(
                new UpdateWrapper<VsAwardPlayer>()
                        .eq("id", player.getId())
                        .eq("version", player.getVersion())
                        .set("version", player.getVersion() + 1)
                        .set("join_times", player.getJoinTimes() - 1)
        );
        if (update) {
            log.info("username:" + player.getUserName() + ",抽得：" + redResultVo.getAmount() + "分红包"+" mysql乐观锁：更新库存成功，会员剩余库存："+(player.getJoinTimes() - 1));
        } else {
            log.info("username:" + player.getUserName() + ",抽得：" + redResultVo.getAmount() + "分红包" + " mysql乐观锁：更新库存失败");
        }

        //生成订单
        VsPayRecord record = new VsPayRecord();
        record.setId(Long.parseLong(System.currentTimeMillis()+""+(int)(Math.random()*10)));
        record.setUserName(player.getUserName());
        record.setClientIp(redPacketDto.getClientIp());
        record.setClientType(redPacketDto.getClientType());
        record.setTotalAmount(redResultVo.getAmount());
        record.setRechargeType(VarParam.RedPacketM.RECHARGE_TYPE);
        record.setPrizeType(VarParam.RedPacketM.PRIZE_TYPE_ONE);
        record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_ONE);
        record.setOperatorPaid(VarParam.RedPacketM.CONFIRM_PAY);

        record.setTimeOrder(LocalDateTime.now());
        boolean save = recordService.save(record);
        if (!save) {
            log.error("订单生成失败："+JSON.toJSONString(record));
            ExceptionCast.castFail("订单生成失败");
        }
        log.info("订单生成成功："+JSON.toJSONString(record));

        if (record.getTotalAmount().compareTo(VarParam.RedPacketM.AMOUNT_QOUTA) > 0) {
            ExceptionCast.castFail("超出限额");
        }

        //入队+执行
        joinMyQueue(record,player);

        //展示给用户：金额 分->元
        redResultVo.setAmount(redResultVo.getAmount().divide(VarParam.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_DOWN));
        return ResponseResult.SUCCESS(redResultVo);
    }

    /**
     * 任务入队+执行
     */
    public void joinMyQueue(VsPayRecord record, VsAwardPlayer player) throws Exception{
        //查询用户的限制时间（毫秒值）
        Long expireTime = redis.getExpire(VarParam.RedPacketM.PLAYER_WAIT + player.getId(), TimeUnit.MILLISECONDS);
        if (-1 == expireTime) ExceptionCast.castFail("userId："+player.getId()+" 红包打款限制时间无限长");
        //放入机器人派送队列(跳表)
        Boolean add = redis.opsForZSet().add(
                VarParam.RedPacketM.JUST_TASK_QUEUE,
                JSON.toJSONString(new TaskAtom(player.getId(),player.getUserName(), record.getId())),
                expireTime == -2L ? 50d : Double.valueOf(expireTime + "")
        );
        if (!add) {
            log.error("订单进入队列失败：userId:"+player.getId()+" recordId:"+record.getId());
        }
        log.info("订单进入队列成功：userId:"+player.getId()+" recordId:"+record.getId());
        robotServer.exeQueue();
    }

    /**
     * 异步更新数据库红包库存
     */
    @Async
    public void updatePrizeStore(VsAwardPrize prizeNew){
        boolean updateById = prizeService.updateById(prizeNew);
        if (updateById){
            log.info("更新奖品库存（数据库）成功："+JSON.toJSONString(prizeNew));
        }else{
            log.error("更新奖品库存（数据库）失败："+JSON.toJSONString(prizeNew));
        }
    }

    /**
     * 经典抽奖算法
     */
    private int drawLottery(List<VsAwardPrize> prizes){
        Random r = new Random () ;
        int len = 0;
        for (VsAwardPrize prize : prizes) {
            len += prize.getPrizePercent().intValue();
        }

        for (int i = 0; i < prizes.size(); i++) {
            int random = r.nextInt(len);
            if (random < prizes.get(i).getPrizePercent().intValue()) {
                return i;
            } else {
                len -= prizes.get(i).getPrizePercent().intValue();
            }
        }
        throw new CustomException(RedCode.LOTTERY_EXCEPTION);
    }

    /**
     * 补单
     */
    @Transactional
    public ResponseResult repay(List<Long> ids) throws Exception{
        List<VsPayRecord> records = recordService.list(new QueryWrapper<VsPayRecord>().in("id", ids));
        for (VsPayRecord record : records) {
            if (record.getPayStatus() != VarParam.RedPacketM.PAY_STATUS_FAIL)
                ExceptionCast.castFail("有订单不可补单");
            VsAwardPlayer player = playerService.getOne(new QueryWrapper<VsAwardPlayer>().eq("user_name", record.getUserName()));
            boolean update = recordService.update(
                    new UpdateWrapper<VsPayRecord>()
                            .eq("id", record.getId())
                            .eq("version",record.getVersion())
                            .set("operator_dispatch", "人工")
                            .set("version", record.getVersion()+1)
            );
            if (!update) ExceptionCast.castFail("人工补单：更新record失败");
            //入队
            this.joinMyQueue(record,player);
        }
        return ResponseResult.SUCCESS("已补单");
    }

    public ResponseResult queryNav() throws Exception{
        List<VsNav> list = navService.list();
        if (CollectionUtils.isEmpty(list)) {
            ExceptionCast.castFail("未查询到任何导航网址");
        }
        return ResponseResult.SUCCESS(list);
    }

    public ResponseResult queryMyRecord(VsPayRecordDto recordDto) throws Exception{
        //布隆过滤器防止恶意点击，DOS攻击
        boolean exist = MyBloomFilter.isExist(
                VarParam.RedPacketM.BLOOM_RED,
                recordDto.getUserName(),
                VarParam.RedPacketM.SIZE_RED,
                VarParam.RedPacketM.FPP_RED, redis
        );
        if (!exist) ExceptionCast.castFail("该账号不存在");

        IPage page = recordService.page(recordDto, new QueryWrapper<VsPayRecord>()
                .eq("user_name", recordDto.getUserName())
                .eq("pay_status",VarParam.RedPacketM.PAY_STATUS_THREE)
                .orderByDesc("create_time")
        );
        Page<VsPayRecordVo> vsPayRecordVoPage = MyBeanUtil.copyPageToPage(page, VsPayRecordVo.class);
        vsPayRecordVoPage.getRecords().forEach(recordVo->{
            recordVo.setCreateTimeStr(DateUtil.MONTH_DAY_MORE.format(recordVo.getCreateTime()));
            recordVo.setTotalAmount(recordVo.getTotalAmount().divide(VarParam.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_DOWN));
        });
       return ResponseResult.SUCCESS(vsPayRecordVoPage);
    }

    public ResponseResult queryHtml(long id) throws Exception{
        VsHtml html = htmlService.getById(id);
        return ResponseResult.SUCCESS(html);
    }

    public ResponseResult queryNewRecord() {
        IPage<VsPayRecord> page = recordService.page(new Page<VsPayRecord>(1, 20),
                new QueryWrapper<VsPayRecord>().orderByDesc("create_time")
                        .eq("pay_status", 3)
        );
        BigDecimal oneHun = new BigDecimal(100);
        for (VsPayRecord record : page.getRecords()) {
            record.setTotalAmount(record.getTotalAmount().divide(oneHun).setScale(2, BigDecimal.ROUND_DOWN));
        }
        return ResponseResult.SUCCESS(page.getRecords());
    }
}
