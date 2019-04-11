package com.bc.manager.redPacket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.*;
import com.bc.manager.redPacket.server.RedPacketManagerServer;
import com.bc.manager.redPacket.vo.ExportRecordVo;
import com.bc.manager.redPacket.vo.VsPayRecordVo;
import com.bc.service.common.redPacket.entity.VsAwardPrize;
import com.bc.service.common.redPacket.entity.VsPayRecord;
import com.bc.utils.ExcelUtil;
import com.bc.utils.project.MyBeanUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
        if (activeDto.getTimeStart().isAfter(activeDto.getTimeEnd())) {
            ExceptionCast.castInvalid("活动开始时间不能晚于截止时间");
        }
        if (activeDto.getDayTimeStart().compareTo(activeDto.getDayTimeEnd()) > 0) {
            ExceptionCast.castInvalid("活动每天开始的时间不能晚于每天截止时间");
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
                || null == prizeDto.getPrizePercent()
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
        if (prizeDto.getPrizePercent() > 100 || prizeDto.getPrizePercent() <= 0){
            ExceptionCast.castInvalid("中奖概率不能超过100或不能小于0");
        }
        return rpmServer.addPrize(prizeDto);
    }

    @ApiOperation("奖品管理：删除")
    @PostMapping("/delPrize")
    public ResponseResult delPrize(@RequestBody IdList idList) throws Exception{
        if (null == idList || CollectionUtils.isEmpty(idList.getIds()))
            ExceptionCast.castInvalid("未传入奖品的id");
        return rpmServer.delPrize(idList.getIds());
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
                || null == prizeDto.getPrizePercent()
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
        if (prizeDto.getPrizePercent() > 100 || prizeDto.getPrizePercent() <= 0){
            ExceptionCast.castInvalid("中奖概率不能超过100或不能小于0");
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

    @ApiOperation("奖品管理：下架")
    @PostMapping("/prizeSoldOut")
    public ResponseResult prizeSoldOut(@RequestBody VsAwardPrizeDto prizeDto) throws Exception{
        if (null == prizeDto || null == prizeDto.getId()) {
            ExceptionCast.castInvalid("奖品Id不能为空");
        }
        return rpmServer.prizeSoldOut(prizeDto);
    }

    @ApiOperation("转换规则：添加")
    @PostMapping("/addTransform")
    public ResponseResult addTransform(@RequestBody VsAwardTransformDto transformDto) throws Exception{
        if (null == transformDto
                || null == transformDto.getTimes()
                || null ==transformDto.getAmount()
        ) ExceptionCast.castInvalid("参数不全");
        if (transformDto.getTimes() < 0) {
            ExceptionCast.castInvalid("抽奖次数不能小于0");
        }
        if (transformDto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            ExceptionCast.castInvalid("金额不能小于0");
        }

        return rpmServer.addTransform(transformDto);
    }

    @ApiOperation("转换规则：查看")
    @PostMapping("queryTransform")
    public ResponseResult queryTransforms() throws Exception{
        return rpmServer.queryTransforms();
    }

    @ApiOperation("转换规则：删除")
    @PostMapping("delTransformById")
    public ResponseResult delTransformById(@RequestBody VsAwardTransformDto transformDto) throws Exception{
        if (null == transformDto || null == transformDto.getId()) {
            ExceptionCast.castInvalid("未传入Id");
        }
        return rpmServer.delTransformById(transformDto);
    }


    @ApiOperation("转换规则：根据id查看")
    @PostMapping("queryTransformById")
    public ResponseResult queryTransformById(@RequestBody VsAwardTransformDto transformDto) throws Exception{
        if (null == transformDto || null == transformDto.getId()) {
            ExceptionCast.castInvalid("未传入Id");
        }
        return rpmServer.queryTransformById(transformDto);
    }

    @ApiOperation("转换规则：编辑")
    @PostMapping("updateTransform")
    public ResponseResult updateTransform(@RequestBody VsAwardTransformDto transformDto) throws Exception{
        if (null == transformDto
                || null == transformDto.getId()
                || null == transformDto.getTimes()
                || null ==transformDto.getAmount()
        ) {
            ExceptionCast.castInvalid("参数不全");
        }
        if (transformDto.getTimes() < 0) {
            ExceptionCast.castInvalid("抽奖次数不能小于0");
        }
        if (transformDto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            ExceptionCast.castInvalid("金额不能小于0");
        }

        return rpmServer.updateTransform(transformDto);
    }

    @ApiOperation("会员管理：单笔添加")
    @PostMapping("/addPlayer")
    public ResponseResult addPlayer(@RequestBody VsAwardPlayerDto playerDto) throws Exception{
        if (null == playerDto
                || StringUtils.isEmpty(playerDto.getUserName())
                || null == playerDto.getHasAmount()
                || null == playerDto.getPlayerStatus()
                || null == playerDto.getJoinTimes()
        ) ExceptionCast.castInvalid("参数不全");

        if (VarParam.YES != playerDto.getPlayerStatus() && VarParam.NO != playerDto.getPlayerStatus()) {
            ExceptionCast.castInvalid("上架状态有误");
        }
        if (playerDto.getHasAmount().compareTo(BigDecimal.ZERO) < 0) {
            ExceptionCast.castInvalid("金额不能小于0");
        }
        if (playerDto.getJoinTimes() < 0) {
            ExceptionCast.castInvalid("可抽奖次数不能小于0");
        }
        return rpmServer.addPlayer(playerDto);
    }

    //sheet 的序号 从1开始
    //headLineNum 行号最小值为0，去掉表头所以从第1行开始读
    @ApiOperation("会员管理：批量导入")
    @PostMapping("readExcelPlays")
    public ResponseResult readExcelPlays(MultipartFile excel,@RequestParam(defaultValue = "1") int sheetNo,
                            @RequestParam(defaultValue = "1") int headLineNum) throws Exception{
        List<Object> list = ExcelUtil.readExcel(excel, new ImportPlaysDto(), sheetNo, headLineNum);
        if (CollectionUtils.isEmpty(list)) {
            ExceptionCast.castFail("空Excel文件");
        }
        return rpmServer.readExcelPlays(list);
    }


    @ApiOperation("会员管理：条件分页查询")
    @PostMapping("/queryPlayersByCriteria")
    public ResponseResult queryPlayersByCriteria(@RequestBody VsAwardPlayerDto playerDto) throws Exception{
        if (null == playerDto) {
            ExceptionCast.castInvalid("未传入任何参数");
        }
        //校验排序
        if (null != playerDto.getPlayerStatus()
            && VarParam.RedPacketM.PLAYER_CREATTIME_DESC != playerDto.getPlayerStatus()
            && VarParam.RedPacketM.PLAYER_AMOUNT_DESC != playerDto.getPlayerStatus()
            && VarParam.RedPacketM.PLAYER_JOINTIME_DESC != playerDto.getPlayerStatus()
            ) ExceptionCast.castFail("排序条件有误");

        //校验状态
        if (null != playerDto.getPlayerStatus()
                &&VarParam.YES != playerDto.getPlayerStatus()
                && VarParam.NO != playerDto.getPlayerStatus()) {
            ExceptionCast.castInvalid("状态有误");
        }
        return rpmServer.queryPlayersByCriteria(playerDto);
    }

    @ApiOperation("会员管理：按照id查询")
    @PostMapping("/queryPlayersById")
    public ResponseResult queryPlayersById(@RequestBody VsAwardPlayerDto playerDto) throws Exception{
        if (null == playerDto || null == playerDto.getId()) {
            ExceptionCast.castInvalid("未传入Id");
        }
        return rpmServer.queryPlayersById(playerDto);
    }
    @ApiOperation("会员管理：修改")
    @PostMapping("/updatePlayer")
    public ResponseResult updatePlayer(@RequestBody VsAwardPlayerDto playerDto) throws Exception{
        if (null == playerDto
                || null == playerDto.getId()
                || StringUtils.isEmpty(playerDto.getUserName())
                || null == playerDto.getHasAmount()
                || null == playerDto.getPlayerStatus()
                || null == playerDto.getJoinTimes()
        ) ExceptionCast.castInvalid("参数不全");
        if (VarParam.YES != playerDto.getPlayerStatus() && VarParam.NO != playerDto.getPlayerStatus()) {
            ExceptionCast.castInvalid("上架状态有误");
        }

        if (playerDto.getHasAmount().compareTo(BigDecimal.ZERO) < 0) {
            ExceptionCast.castInvalid("金额不能小于0");
        }
        if (playerDto.getJoinTimes() < 0) {
            ExceptionCast.castInvalid("可抽奖次数不能小于0");
        }
        return rpmServer.updatePlayer(playerDto);

    }
    @ApiOperation("会员管理：下架")
    @PostMapping("/playerSoldOut")
    public ResponseResult playerSoldOut(@RequestBody VsAwardPlayerDto playerDto) throws Exception{
        if (null == playerDto || null == playerDto.getId())
            ExceptionCast.castInvalid("未传入id");
        return rpmServer.playerSoldOut(playerDto);
    }

    @ApiOperation("会员管理：清除会员")
    @PostMapping("/delPlayers")
    public ResponseResult delPlayers(@RequestBody VsAwardPlayerDto playerDto) throws Exception{
        if (null == playerDto || null == playerDto.getDays()) {
            ExceptionCast.castInvalid("参数不全");
        }
        if (playerDto.getDays() < 0) {
            ExceptionCast.castInvalid("保留天数不能小于0");
        }
        return rpmServer.delPlayers(playerDto);
    }

    @ApiOperation("会员管理：清除会员")
    @PostMapping("/delPlayersBatch")
    public ResponseResult delPlayersBatch(@RequestBody IdList idList) throws Exception {
        if (null == idList || CollectionUtils.isEmpty(idList.getIds())) {
            ExceptionCast.castInvalid("参数不全");
        }
        return rpmServer.delPlayersBatch(idList.getIds());
    }

    @ApiOperation("抽奖记录：删除")
    @PostMapping("/delRecordBatch")
    public ResponseResult delRecordBatch(@RequestBody IdList idList) throws Exception{
        if (null == idList || null == idList.getIds()) {
            ExceptionCast.castInvalid("参数不全");
        }
        return rpmServer.delRecordBatch(idList.getIds());
    }

//    @ApiOperation("抽奖记录：重派")

    @ApiOperation("抽奖记录：条件查询")
    @PostMapping("/queryRecordByCriteria")
    public ResponseResult queryRecordByCriteria(@RequestBody VsPayRecordDto recordDto) throws Exception{
        if (null == recordDto) {
            ExceptionCast.castInvalid("未传入参数");
        }
        if (null != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_ONE != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_TWO != recordDto.getPayStatus()
        ) ExceptionCast.castInvalid("派送状态有误");
        if (null != recordDto.getStartTime()
                && null != recordDto.getEndTime()
                && recordDto.getStartTime().isAfter(recordDto.getEndTime())) {
            ExceptionCast.castInvalid("起始时间不能早于结束时间");
        }
        Page<VsPayRecordVo> vsPayRecordVoPage = rpmServer.queryRecordByCriteria(recordDto);
        return ResponseResult.SUCCESS(vsPayRecordVoPage);
    }
    @ApiOperation("抽奖记录：导出Excel")
    @PostMapping("/exportRecordExcel")
    public void exportRecordExcel(@RequestBody VsPayRecordDto recordDto,HttpServletResponse response) throws Exception {
        if (null == recordDto) {
            ExceptionCast.castInvalid("未传入参数");
        }
        if (null != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_ONE != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_TWO != recordDto.getPayStatus()
        ) ExceptionCast.castInvalid("派送状态有误");
        if (null != recordDto.getStartTime()
                && null != recordDto.getEndTime()
                && recordDto.getStartTime().isAfter(recordDto.getEndTime())) {
            ExceptionCast.castInvalid("起始时间不能早于结束时间");
        }
        List<VsPayRecordVo> vsPayRecordVos = rpmServer.queryRecordByCriteria2(recordDto);
        List<ExportRecordVo> exportRecordVos = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        vsPayRecordVos.forEach(recordVo->{
            ExportRecordVo exRecord = new ExportRecordVo();
            exRecord.setClientType(recordVo.getClientType() == VarParam.RedPacketM.CLIENT_TYPE_ONE ? "PC" : "MOBILE");
            exRecord.setId(recordVo.getId() + "");
            exRecord.setPayStatus(recordVo.getPayStatus() == VarParam.RedPacketM.PAY_STATUS_ONE ? "待派送" : "已派送");
            exRecord.setTimeOrder(df.format(recordVo.getTimeOrder()));
            exRecord.setTotalAmount(recordVo.getTotalAmount().longValue()+"");
            exRecord.setUserName(recordDto.getUserName());
            exRecord.setTimePay(df.format(recordVo.getTimePay()));
            exportRecordVos.add(exRecord);
        });

        Long time = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));;
        ExcelUtil.writeExcel(response,exportRecordVos , time+"", time+"", new ExportRecordVo());
    }

    @ApiOperation("抽奖记录：清除记录")
    @PostMapping("/delRecords")
    public ResponseResult delRecords(@RequestBody VsPayRecordDto recordDto) throws Exception{
        if (null == recordDto || null == recordDto.getDays()) {
            ExceptionCast.castInvalid("参数不全");
        }
        if (recordDto.getDays() < 0) {
            ExceptionCast.castInvalid("保留天数不能小于0");
        }
        return rpmServer.delRecords(recordDto);
    }

    @ApiOperation("抽奖记录：统计")
    @PostMapping("/statisRecords")
    public ResponseResult statisRecords() throws Exception{
        return rpmServer.statisRecords();
    }

    @ApiOperation("品牌设置：修改")
    @PostMapping("/updateBrand")
    public ResponseResult updateBrand(@RequestBody VsSiteDto siteDto) throws Exception {
        if (null == siteDto
            || StringUtils.isEmpty(siteDto.getSiteName())
            || StringUtils.isEmpty(siteDto.getCustomerUrl())
            || StringUtils.isEmpty(siteDto.getSiteUrl())
        ) ExceptionCast.castInvalid("参数不全");
        return rpmServer.updateBrand(siteDto);
    }


//    @ApiOperation("导航设置：添加")
//    @ApiOperation("导航设置：查询")
//    @ApiOperation("导航设置：删除")
//    @ApiOperation("导航设置：修改")
//    @ApiOperation("导航设置：按id查询")
//
//    @ApiOperation("配置项：添加")
//    @ApiOperation("配置项：查询")
//    @ApiOperation("配置项：删除")
//    @ApiOperation("配置项：修改")
//    @ApiOperation("配置项：按id查询")








}
