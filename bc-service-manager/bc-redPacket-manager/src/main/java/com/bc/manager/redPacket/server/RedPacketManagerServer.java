package com.bc.manager.redPacket.server;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.VsAwardActiveDto;
import com.bc.manager.redPacket.dto.VsAwardPlayerDto;
import com.bc.manager.redPacket.dto.VsAwardPrizeDto;
import com.bc.manager.redPacket.dto.VsAwardTransformDto;
import com.bc.manager.redPacket.vo.VsAwardActiveVo;
import com.bc.manager.redPacket.vo.VsAwardPrizeVo;
import com.bc.manager.redPacket.vo.VsAwardTransformVo;
import com.bc.service.common.redPacket.entity.VsAwardActive;
import com.bc.service.common.redPacket.entity.VsAwardPlayer;
import com.bc.service.common.redPacket.entity.VsAwardPrize;
import com.bc.service.common.redPacket.entity.VsAwardTransform;
import com.bc.service.common.redPacket.service.*;
import com.bc.utils.project.MyBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    private IVsPayRechargeService payRechargeService;
    @Autowired
    private IVsSiteService siteService;
    @Autowired
    private StringRedisTemplate redis;

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
        if (flag) {
            //删除过期的活动缓存
            if (redis.delete(VarParam.RedPacketM.ACTIVE_KEY))
                return ResponseResult.SUCCESS();
            ExceptionCast.castFail("缓存清理失败");
        }
        ExceptionCast.castFail("数据库更新失败");
        return null;
    }

    /**
     * 获取红包(防止并发下缓存雪崩)
     * 双重检查锁+互斥锁+递归调用
     *
     * @return
     * @throws Exception
     */
    private VsAwardActiveVo getActive() throws Exception {
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
        VsAwardActiveVo activeVo = JSON.parseObject(activeJson, VsAwardActiveVo.class);
        return activeVo;
    }

    /**
     * 红包活动的查看
     */
    public ResponseResult queryActive() throws Exception {
        return ResponseResult.SUCCESS(getActive());
    }

    /**
     * 奖品管理:添加
     */
    @Transactional
    public ResponseResult addPrize(VsAwardPrizeDto prizeDto) throws Exception {
        VsAwardPrize prize = new VsAwardPrize();
        MyBeanUtil.copyProperties(prizeDto, prize);
        boolean save = prizeService.save(prize);
        if (save) {
            if (redis.delete(VarParam.RedPacketM.PRIZE_KEY))
                return ResponseResult.SUCCESS();
            ExceptionCast.castFail("缓存清理失败");
        }
        ExceptionCast.castFail("数据库添加失败");
        return null;
    }

    /**
     * 奖品管理：删除
     */
    @Transactional
    public ResponseResult delPrize(List<VsAwardPrizeDto> prizeDtoList) throws Exception {
        List<Integer> ids = new ArrayList<>();
        prizeDtoList.forEach(prizeDto -> ids.add(prizeDto.getId()));
        Long prizeSize = redis.opsForList().size(VarParam.RedPacketM.PRIZE_KEY);
        if (ids.size() >= prizeSize.intValue())
            return ResponseResult.FAIL("奖品不能全部删除！");
        boolean remove = prizeService.removeByIds(ids);
        if (remove) {
            if (redis.delete(VarParam.RedPacketM.PRIZE_KEY))
                return ResponseResult.SUCCESS();
            ExceptionCast.castFail("缓存清理失败");
        }
        ExceptionCast.castFail("数据库更新失败");
        return null;
    }

    /**
     * 奖品管理：修改
     */
    @Transactional
    public ResponseResult updatePrize(VsAwardPrizeDto prizeDto) throws Exception {
        VsAwardPrize prize = new VsAwardPrize();
        MyBeanUtil.copyProperties(prizeDto, prize);
        boolean update = prizeService.updateById(prize);
        if (update) {
            if (redis.delete(VarParam.RedPacketM.PRIZE_KEY))
                return ResponseResult.SUCCESS();
            ExceptionCast.castFail("缓存清理失败");
        }
        ExceptionCast.castFail("数据库更新失败");
        return null;
    }

    /**
     * 获取所有奖品
     */
    public List<VsAwardPrize> getPrizes() throws Exception{
        List<String> prizesJsons = redis.opsForList().range(VarParam.RedPacketM.PRIZE_KEY, 0, -1);
        if (CollectionUtils.isEmpty(prizesJsons)){
            if (prizeLock.tryLock()) {
                if (CollectionUtils.isEmpty(prizesJsons)) {
                    //从数据库获取，放入缓存
                    List<VsAwardPrize> prizeList = prizeService.list();
                    if (CollectionUtils.isEmpty(prizeList)) ExceptionCast.castFail("数据库没有任何奖品");
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
                return o1.getTotalAmount().intValue() - o2.getTotalAmount().intValue();
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
                    return o1.getTotalAmount().intValue() - o2.getTotalAmount().intValue();
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
        if (updateById) {
            //删除缓存
            if (redis.delete(VarParam.RedPacketM.PRIZE_KEY))
                return ResponseResult.SUCCESS();
            ExceptionCast.castFail("缓存清理失败");
        }
        ExceptionCast.castFail("数据库更新失败");
        return null;
    }

    /**
     * 转换规则:添加
     */
    @Transactional
    public ResponseResult addTransform(VsAwardTransformDto transformDto) throws Exception{
        VsAwardTransform transform = new VsAwardTransform();
        MyBeanUtil.copyProperties(transformDto, transform);
        boolean save = transformService.save(transform);
        if (save) {
            if (redis.delete(VarParam.RedPacketM.TRANSFORM_KEY))
                return ResponseResult.SUCCESS();
            ExceptionCast.castFail("缓存清理失败");
        }
        ExceptionCast.castFail("数据库存入失败");
        return null;
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
                return o1.getConfigureValue() - o2.getConfigureValue();
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
        if (removeById) {
            if (redis.delete(VarParam.RedPacketM.TRANSFORM_KEY)) {
                return ResponseResult.SUCCESS();
            }
            ExceptionCast.castFail("缓存删除失败");
        }
        ExceptionCast.castFail("数据库删除失败");
        return null;
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

    public ResponseResult updateTransform(VsAwardTransformDto transformDto) throws Exception{
        VsAwardTransform transform = new VsAwardTransform();
        MyBeanUtil.copyProperties(transformDto,transform);
        boolean update = transformService.updateById(transform);
        if (update) {
            if (redis.delete(VarParam.RedPacketM.TRANSFORM_KEY)) {
                return ResponseResult.SUCCESS();
            }
            ExceptionCast.castFail("缓存删除失败");
        }
        ExceptionCast.castFail("数据库删除失败");
        return null;
    }

    @Transactional
    public ResponseResult addPlayer(VsAwardPlayerDto playerDto) throws Exception{
        VsAwardPlayer player = new VsAwardPlayer();
        MyBeanUtil.copyProperties(playerDto, player);
        boolean save = playerService.save(player);
        if (save) {
            return ResponseResult.SUCCESS();
        }
        return ResponseResult.FAIL();
    }

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


        return ResponseResult.SUCCESS(Collections.emptyList());
    }

    /**
     *
     */


}
