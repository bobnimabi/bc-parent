package com.bc.manager.redPacket.controller;

import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.VsAwardActiveDto;
import com.bc.manager.redPacket.dto.VsAwardActiveDtoList;
import com.bc.manager.redPacket.dto.VsAwardPrizeDto;
import com.bc.manager.redPacket.server.RedPacketManagerServer;
import com.bc.service.common.redPacket.entity.VsAwardPrize;
import com.bc.utils.project.MyBeanUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mrt on 2019/4/8 0008 下午 1:45
 */
@Api("红包后台管理")
@RestController
public class RedPacketManagerController {
    @Autowired
    RedPacketManagerServer rpmServer;

    @ApiOperation("红包活动修改")
    @PostMapping("/updateActive")
    public ResponseResult updateActive(@RequestBody VsAwardActiveDto activeDto) throws Exception{
        if (null == activeDto && null == activeDto.getId()) {
            ExceptionCast.castInvalid("未传入活动的id");
        }
        if (null != activeDto.getActiveStatus()
                && (VarParam.NO != activeDto.getActiveStatus() && VarParam.YES != activeDto.getActiveStatus())) {
            ExceptionCast.castInvalid("活动状态有误");
        }
        if (null != activeDto.getTimeEnd()
                && activeDto.getTimeEnd().isBefore(LocalDateTime.now())) {
            ExceptionCast.castInvalid("活动过期过期时间不能早于当前时间");
        }
        if (null != activeDto.getActiveModel()
                && (VarParam.RedPacketM.ACTIVEMODEL_ONE != activeDto.getActiveModel()
                && VarParam.RedPacketM.ACTIVEMODEL_TWO != activeDto.getActiveModel())) {
            ExceptionCast.castInvalid("活动模式不符合规范");
        }
        return rpmServer.updateActive(activeDto);
    }

    @ApiOperation("红包活动查看")
    @PostMapping("/queryActive")
    public ResponseResult queryActive() throws Exception{
        return rpmServer.queryActive();
    }

    @ApiOperation("奖品管理：添加")
    @PostMapping("/addPrize")
    public ResponseResult addPrize(@RequestBody VsAwardPrizeDto prizeDto) throws Exception{
        if (null == prizeDto
                || StringUtils.isEmpty(prizeDto.getPrizeName())
                || null == prizeDto.getPrizeType()
                || null == prizeDto.getPrizeStoreNums()
                || null == prizeDto.getPrizeDrawNums()
                || null == prizeDto.getTotalAmount()
        ) ExceptionCast.castInvalid("参数不足");
        if (VarParam.RedPacketM.PRIZE_TYPE_ONE != prizeDto.getPrizeType()
                && VarParam.RedPacketM.PRIZE_TYPE_TWO != prizeDto.getPrizeType()) {
            ExceptionCast.castInvalid("奖品类型有误");
        }
        if (prizeDto.getPrizeStoreNums() <= VarParam.RedPacketM.PRIZE_STORE_NUM_MIN) {
            ExceptionCast.castInvalid("库存数量不能低于" + VarParam.RedPacketM.PRIZE_STORE_NUM_MIN);
        }
        if (0 != prizeDto.getPrizeDrawNums()) {
            ExceptionCast.castInvalid("派送数量必须是0");
        }
        if (prizeDto.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            ExceptionCast.castInvalid("奖品金额不能低于或等于0分");
        }
        return rpmServer.addPrize(prizeDto);
    }

    @ApiOperation("奖品管理：删除")
    @PostMapping("/delPrize")
    public ResponseResult delPrize(@RequestBody VsAwardActiveDtoList vsAwardActiveDtoList) throws Exception{
        if (null == vsAwardActiveDtoList || CollectionUtils.isEmpty(vsAwardActiveDtoList.getPrizeDtoList()))
            ExceptionCast.castInvalid("未传入奖品的id");
        List<VsAwardPrizeDto> prizeDtoList = vsAwardActiveDtoList.getPrizeDtoList();

        for (VsAwardPrizeDto prizeDto : prizeDtoList ) {
            if (null == prizeDto.getId()) ExceptionCast.castInvalid("未传入奖品的id");
        }
        return rpmServer.delPrize(prizeDtoList);
    }

    @ApiOperation("奖品管理：修改")
    @PostMapping("/updatePrize")
    public ResponseResult updatePrize(@RequestBody VsAwardPrizeDto prizeDto) throws Exception{
        if (null == prizeDto
                || null == prizeDto.getId()
                || StringUtils.isEmpty(prizeDto.getPrizeName())
                || null == prizeDto.getPrizeType()
                || null == prizeDto.getPrizeStoreNums()
                || null == prizeDto.getPrizeDrawNums()
                || null == prizeDto.getTotalAmount()
        ) ExceptionCast.castInvalid("参数不足");
        if (VarParam.RedPacketM.PRIZE_TYPE_ONE != prizeDto.getPrizeType()
                && VarParam.RedPacketM.PRIZE_TYPE_TWO != prizeDto.getPrizeType()) {
            ExceptionCast.castInvalid("奖品类型有误");
        }
        if (prizeDto.getPrizeStoreNums() <= VarParam.RedPacketM.PRIZE_STORE_NUM_MIN) {
            ExceptionCast.castInvalid("库存数量不能低于" + VarParam.RedPacketM.PRIZE_STORE_NUM_MIN);
        }
        if (0 <= prizeDto.getPrizeDrawNums()) {
            ExceptionCast.castInvalid("派送数量必须大于或等于0");
        }
        if (prizeDto.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            ExceptionCast.castInvalid("奖品金额不能低于0分");
        }
        return rpmServer.updatePrize(prizeDto);
    }

    @ApiOperation("奖品管理：查看")
    @PostMapping("/updatePrize")
    public ResponseResult queryAllPrize() throws Exception{
        return rpmServer.queryPrizes();
    }

    @ApiOperation("奖品管理：根据名称查看")
    @PostMapping("/queryPrizeByName")
    public ResponseResult queryPrizeByName(@RequestBody VsAwardPrizeDto prizeDto) throws Exception{
        if (null == prizeDto || StringUtils.isEmpty(prizeDto.getPrizeName())) {
            ExceptionCast.castInvalid("奖品名称不能为空");
        }
        return rpmServer.queryPrizeByName(prizeDto);
    }

    @ApiOperation("奖品管理：根据奖品Id查看")
    @PostMapping("/queryPrizeById")
    public ResponseResult queryPrizeById(@RequestBody VsAwardPrizeDto prizeDto) throws Exception{
        if (null == prizeDto || null == prizeDto.getId()) {
            ExceptionCast.castInvalid("奖品Id不能为空");
        }
        return rpmServer.queryPrizeById(prizeDto);
    }

    @ApiOperation("奖品管理：根据奖品Id查看")
    @PostMapping("/prizeSoldOut")
    public ResponseResult prizeSoldOut(@RequestBody VsAwardPrizeDto prizeDto) throws Exception{
        if (null == prizeDto || null == prizeDto.getId()) {
            ExceptionCast.castInvalid("奖品Id不能为空");
        }
        return rpmServer.prizeSoldOut(prizeDto);
    }

    @ApiOperation("转换规则：添加")
    @PostMapping("/addTransform")
    public ResponseResult addTransform(@RequestBody VsAwardPrizeDto prizeDto) throws Exception{

        return null;
    }


//    @ApiOperation("转换规则：查看")
//    @PostMapping("")
//
//    @ApiOperation("转换规则：删除")
//    @PostMapping("")
//
//    @ApiOperation("转换规则：根据id查看")
//    @PostMapping("")
//
//    @ApiOperation("转换规则：编辑")
//    @PostMapping("")








}
