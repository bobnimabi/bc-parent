package com.bc.manager.redPacket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.*;
import com.bc.manager.redPacket.server.RedPacketManagerServer;
import com.bc.manager.redPacket.vo.ExportRecordVo;
import com.bc.manager.redPacket.vo.VsPayRecordVo;
import com.bc.service.redPacket.dto.RobotLoginDto;
import com.bc.utils.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mrt on 2019/4/8 0008 下午 1:45
 */
@Api("红包后台管理")
@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class RedPacketManagerController {

    @Autowired
    RedPacketManagerServer rpmServer;
    @Value("${redPacketM.excelDownUrl}")
    private String excelDownUrl;

    @ApiOperation("红包活动：修改")
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
        if ((null != activeDto.getTimeEnd()
                && null != activeDto.getTimeStart()
                && activeDto.getTimeStart().isAfter(activeDto.getTimeEnd()))) {
            ExceptionCast.castInvalid("活动开始时间不能晚于截止时间");
        }
        if (StringUtils.isNotEmpty(activeDto.getDayTimeStart())
                && StringUtils.isNotEmpty(activeDto.getDayTimeEnd())
                &&  activeDto.getDayTimeStart().compareTo(activeDto.getDayTimeEnd()) > 0) {
            ExceptionCast.castInvalid("活动每天开始的时间不能晚于每天截止时间");
        }
        return rpmServer.updateActive(activeDto);
    }

    @ApiOperation("红包活动:查看")
    @GetMapping("/queryActive")
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
            ExceptionCast.castInvalid("奖品金额不能<=0分");
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
                || StringUtils.isEmpty(prizeDto.getPrizeRemark())
        ) ExceptionCast.castInvalid("参数不足");
        if (VarParam.RedPacketM.PRIZE_TYPE_ONE != prizeDto.getPrizeType()
                && VarParam.RedPacketM.PRIZE_TYPE_TWO != prizeDto.getPrizeType()) {
            ExceptionCast.castInvalid("奖品类型有误");
        }

        if (prizeDto.getPrizeStoreNums() <= VarParam.RedPacketM.PRIZE_STORE_NUM_MIN) {
            ExceptionCast.castInvalid("库存数量不能低于" + VarParam.RedPacketM.PRIZE_STORE_NUM_MIN);
        }
        if (0 > prizeDto.getPrizeDrawNums()|| prizeDto.getPrizeDrawNums() > prizeDto.getPrizeStoreNums()) {
            ExceptionCast.castInvalid("派送数量需>=0 & <=库存");
        }
        prizeDto.setPrizeDrawNums(null);
        if (prizeDto.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            ExceptionCast.castInvalid("奖品金额不能低于0分");
        }
        if (prizeDto.getPrizePercent() > 100 || prizeDto.getPrizePercent() < 0){
            ExceptionCast.castInvalid("中奖概率应该0-100之间");
        }
        return rpmServer.updatePrize(prizeDto);
    }

    @ApiOperation("奖品管理：查看全部")
    @PostMapping("/queryAllPrize")
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

    @ApiOperation("奖品管理：上下架")
    @PostMapping("/prizeSoldOut")
    public ResponseResult prizeSoldOut(@RequestBody VsAwardPrizeDto prizeDto) throws Exception{
        if (null == prizeDto || null == prizeDto.getId() || null== prizeDto.getPrizeStatus()) {
            ExceptionCast.castInvalid("奖品Id不能为空");
        }
        if (VarParam.NO != prizeDto.getPrizeStatus() && VarParam.YES != prizeDto.getPrizeStatus()) {
            ExceptionCast.castInvalid("奖品状态有误");
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

    @ApiOperation("转换规则：查看全部")
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

    @ApiOperation("转换规则：修改")
    @PostMapping("updateTransform")
    public ResponseResult updateTransform(@RequestBody VsAwardTransformDto transformDto) throws Exception{
        if (null == transformDto
                || null == transformDto.getId()
                || null == transformDto.getTimes()
                || null ==transformDto.getAmount()
        ) {
            ExceptionCast.castInvalid("参数不全");
        }
        if (transformDto.getTimes() <= 0) {
            ExceptionCast.castInvalid("抽奖次数不能<=0");
        }
        if (transformDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            ExceptionCast.castInvalid("金额不能<=0");
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
            ExceptionCast.castInvalid("金额不能<0");
        }
        if (playerDto.getJoinTimes() < 0) {
            ExceptionCast.castInvalid("可抽奖次数不能<0");
        }
        return rpmServer.addPlayer(playerDto);
    }

    @ApiOperation("会员管理：下载Excel模板")
    @GetMapping("/downPlayerExcel")
    public void downPlayerExcel(HttpServletResponse response) throws Exception{
        // 下载本地文件
        String fileName = "player.xlsx"; // 文件的默认保存名/usr/local/games/澳门新濠影汇.mobileconfig
        // 读到流中
        InputStream inStream = new FileInputStream(excelDownUrl);// 文件的存放路径
        // 设置输出的格式
        response.reset();
        response.setContentType("bin");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        // 循环取出流中的数据
        byte[] b = new byte[100];
        int len;
        try {
            while ((len = inStream.read(b)) > 0)
                response.getOutputStream().write(b, 0, len);
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            && VarParam.RedPacketM.PLAYER_CREATTIME_DESC != playerDto.getOrderBy()
            && VarParam.RedPacketM.PLAYER_AMOUNT_DESC != playerDto.getOrderBy()
            && VarParam.RedPacketM.PLAYER_JOINTIME_DESC != playerDto.getOrderBy()
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
    @ApiOperation("会员管理：上下架")
    @PostMapping("/playerSoldOut")
    public ResponseResult playerSoldOut(@RequestBody VsAwardPlayerDto playerDto) throws Exception{
        if (null == playerDto || null == playerDto.getId() || null == playerDto.getPlayerStatus())
            ExceptionCast.castInvalid("参数不全");
        return rpmServer.playerSoldOut(playerDto);
    }

    @ApiOperation("会员管理：批量清除会员")
    @PostMapping("/delPlayersBatch")
    public ResponseResult delPlayers(@RequestBody VsAwardPlayerDto playerDto) throws Exception{
        if (null == playerDto || null == playerDto.getDays()) {
            ExceptionCast.castInvalid("参数不全");
        }
        if (playerDto.getDays() < 0) {
            ExceptionCast.castInvalid("保留天数不能小于0");
        }
        return rpmServer.delPlayers(playerDto);
    }

    @ApiOperation("会员管理：删除会员")
    @PostMapping("/delPlayers")
    public ResponseResult delPlayersBatch(@RequestBody IdList idList) throws Exception {
        if (null == idList || CollectionUtils.isEmpty(idList.getIds())) {
            ExceptionCast.castInvalid("参数不全");
        }
        return rpmServer.delPlayersBatch(idList.getIds());
    }

    @ApiOperation("抽奖记录：删除")
    @PostMapping("/delRecord")
    public ResponseResult delRecord(@RequestBody IdList idList) throws Exception{
        if (null == idList || null == idList.getIds()) {
            ExceptionCast.castInvalid("参数不全");
        }
        return rpmServer.delRecord(idList.getIds());
    }

//    @ApiOperation("抽奖记录：重派")

    @ApiOperation("抽奖记录：条件分页查询")
    @PostMapping("/queryRecordByCriteria")
    public ResponseResult queryRecordByCriteria(@RequestBody VsPayRecordDto recordDto) throws Exception{
        if (null == recordDto) {
            ExceptionCast.castInvalid("未传入参数");
        }
        if (null != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_ONE != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_TWO != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_THREE != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_FAIL != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_CANCLE != recordDto.getPayStatus()
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
    @GetMapping("/exportRecordExcel")
    public void exportRecordExcel(@RequestBody(required = false) VsPayRecordDto recordDto,HttpServletResponse response) throws Exception {
        recordDto = new VsPayRecordDto();
        if (null != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_ONE != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_TWO != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_THREE != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_FAIL != recordDto.getPayStatus()
                &&VarParam.RedPacketM.PAY_STATUS_CANCLE != recordDto.getPayStatus()
        ) ExceptionCast.castInvalid("派送状态有误");
        if (null != recordDto.getStartTime()
                && null != recordDto.getEndTime()
                && recordDto.getStartTime().isAfter(recordDto.getEndTime())) {
            ExceptionCast.castInvalid("起始时间不能早于结束时间");
        }
        List<VsPayRecordVo> vsPayRecordVos = rpmServer.queryRecordByCriteria2(recordDto);
        List<ExportRecordVo> exportRecordVos = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (VsPayRecordVo recordVo : vsPayRecordVos) {
            ExportRecordVo exRecord = new ExportRecordVo();
            exRecord.setClientType(recordVo.getClientType() == VarParam.RedPacketM.CLIENT_TYPE_ONE ? "PC" : "MOBILE");
            exRecord.setId(recordVo.getId() + "");
            String payStatus = "";
            switch (recordVo.getPayStatus()){
                case 1:
                    payStatus = "待派送";
                    break;
                case 2:
                    payStatus = "待派中";
                    break;
                case 3:
                    payStatus = "已派送";
                    break;
                case 4:
                    payStatus = "派送失败";
                    break;
                case 0:
                    payStatus = "作废";
                    break;
                default:

            }
            exRecord.setPayStatus(payStatus);
            exRecord.setTimeOrder(df.format(recordVo.getTimeOrder()));
            exRecord.setTotalAmount(recordVo.getTotalAmount().longValue() + "");
            exRecord.setUserName(recordVo.getUserName());
            exRecord.setTimePay(df.format(recordVo.getTimePay()));
            exportRecordVos.add(exRecord);
        }
        ExcelUtil.writeExcel(response,exportRecordVos , df.format( LocalDateTime.now()), "express", new ExportRecordVo());
    }

    @ApiOperation("抽奖记录：清除n天前记录")
    @PostMapping("/delRecordBatch")
    public ResponseResult delRecordBatch(@RequestBody VsPayRecordDto recordDto) throws Exception{
        if (null == recordDto || null == recordDto.getDays()) {
            ExceptionCast.castInvalid("参数不全");
        }
        if (recordDto.getDays() < 0) {
            ExceptionCast.castInvalid("保留天数不能小于0");
        }
        return rpmServer.delRecordBatch(recordDto);
    }

    @ApiOperation("抽奖记录：统计")
    @PostMapping("/statisRecords")
    public ResponseResult statisRecords() throws Exception{
        return rpmServer.statisRecords();
    }
    @ApiOperation("品牌设置：查询")
    @PostMapping("/queryBrand")
    public ResponseResult queryBrand() throws Exception {
        return rpmServer.queryBrand();
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


    @ApiOperation("导航设置：添加")
    @PostMapping("/navAdd")
    public ResponseResult navAdd(@RequestBody VsNavDto navDto) throws Exception {
        if (null == navDto
                || StringUtils.isEmpty(navDto.getNavName())
                || StringUtils.isEmpty(navDto.getNavUrl())
                || null == navDto.getNavTarget()
        ) ExceptionCast.castInvalid("参数不全");
        return rpmServer.navAdd(navDto);
    }

    @ApiOperation("导航设置：分页查询")
    @PostMapping("/navQuery")
    public ResponseResult navQuery(@RequestBody Page page) throws Exception {
        if (null == page
            ||  page.getCurrent() <= 0
            ||  page.getSize() <= 0
        ) ExceptionCast.castInvalid("参数无效或有误");
        return rpmServer.navQuery(page);
    }

    @ApiOperation("导航设置：删除")
    @PostMapping("/navDel")
    public ResponseResult navDel(@RequestBody VsNavDto navDto) throws Exception {
        if (null == navDto
                ||  null == navDto.getId()
        ) ExceptionCast.castInvalid("参数无效或有误");

        return rpmServer.navDel(navDto);
    }

    @ApiOperation("导航设置：按id查询")
    @PostMapping("/navQueryById")
    public ResponseResult navQueryById(@RequestBody VsNavDto navDto) throws Exception {
        if (null == navDto
                ||  null == navDto.getId()
        ) ExceptionCast.castInvalid("参数无效或有误");

        return rpmServer.navQueryById(navDto);
    }

    @ApiOperation("导航设置：修改")
    @PostMapping("/navUpdateById")
    public ResponseResult navUpdateById(@RequestBody VsNavDto navDto) throws Exception {
        if (null == navDto
                || StringUtils.isEmpty(navDto.getNavName())
                || StringUtils.isEmpty(navDto.getNavUrl())
                || null == navDto.getNavTarget()
        ) ExceptionCast.castInvalid("参数不全");
        return rpmServer.navUpdateById(navDto);
    }

    @ApiOperation("配置项：添加")
    @PostMapping("/configAdd")
    public ResponseResult configAdd(@RequestBody VsConfigureDto configureDto) throws Exception {
        if (null == configureDto
                || StringUtils.isEmpty(configureDto.getConfigureKey())
                || StringUtils.isEmpty(configureDto.getConfigureValue())
        ) ExceptionCast.castInvalid("参数不全");
        return rpmServer.configAdd(configureDto);
    }

    @ApiOperation("配置项：分页查询")
    @PostMapping("/configQuery")
    public ResponseResult configQuery(@RequestBody Page page) throws Exception {
        if (null == page
                ||  page.getCurrent() <= 0
                ||  page.getSize() <= 0
        ) ExceptionCast.castInvalid("参数无效或有误");
        return rpmServer.configQuery(page);
    }

    @ApiOperation("配置项：删除")
    @PostMapping("/configDel")
    public ResponseResult configDel(@RequestBody VsConfigureDto configureDto) throws Exception {
        if (null == configureDto
                || null == configureDto.getId()
        ) ExceptionCast.castInvalid("参数不全");
        return rpmServer.configDel(configureDto);
    }
    @ApiOperation("配置项：按id查询")
    @PostMapping("/configQueryById")
    public ResponseResult configQueryById(@RequestBody VsConfigureDto configureDto) throws Exception {
        if (null == configureDto
                || null == configureDto.getId()
        ) ExceptionCast.castInvalid("参数不全");
        return rpmServer.configQueryById(configureDto);
    }
    @ApiOperation("配置项：修改")
    @PostMapping("/configUpdate")
    public ResponseResult configUpdate(@RequestBody VsConfigureDto configureDto) throws Exception {
        if (null == configureDto
                || null == configureDto.getId()
                || StringUtils.isEmpty(configureDto.getConfigureKey())
                || StringUtils.isEmpty(configureDto.getConfigureValue())
        ) ExceptionCast.castInvalid("参数不全");

        return rpmServer.configUpdate(configureDto);
    }

    @ApiOperation("补单")
    @PostMapping("/repayOrder")
    public ResponseResult repayOrder(@RequestParam Long id) throws Exception {
        if (null == id) {
            ExceptionCast.castFail("未传入补单的id");
        }
        return rpmServer.repayOrder(id);
    }

    @ApiOperation("机器人：获取图片验证码")
    @PostMapping("/getVarCode")
    public void getVarCode(@RequestBody RobotLoginDto robotLoginDto, HttpServletResponse response) throws Exception {
        if (null == robotLoginDto ||  null == robotLoginDto.getRobotNum()) {
            ExceptionCast.castFail("未传入机器人编号");
        }
        rpmServer.getVarCode(robotLoginDto);
    }

    @ApiOperation("机器人：登录")
    @PostMapping("/robotLogin")
    public ResponseResult robotLogin(@RequestBody RobotLoginDto robotLoginDto) throws Exception {
        if (null == robotLoginDto || StringUtils.isEmpty(robotLoginDto.getImageCode()) || null == robotLoginDto.getRobotNum()) {
            ExceptionCast.castFail("未传入验证码或机器人编号");
        }
        return rpmServer.robotLogin(robotLoginDto);
    }

    @ApiOperation("机器人：增加")
    @PostMapping("/addRobot")
    public ResponseResult addRobot(@RequestBody VsRobotDto robotDto) throws Exception {
        if (null == robotDto
                || StringUtils.isEmpty(robotDto.getRobotName())
                || null == robotDto.getRobotNum()
                || StringUtils.isEmpty(robotDto.getRobotDesc())
                || StringUtils.isEmpty(robotDto.getPlatAccount())
                || StringUtils.isEmpty(robotDto.getPlatPassword())
                || null == robotDto.getRobotStatus()
        ) ExceptionCast.castFail("参数不全");
        return rpmServer.addRobot(robotDto);
    }

    @ApiOperation("机器人：开启或停止工作")
    @PostMapping("/robotStatus")
    public ResponseResult robotStatus(@RequestBody VsRobotDto robotDto) throws Exception {
        if (null == robotDto
                || null == robotDto.getRobotStatus()
        ) ExceptionCast.castFail("参数不全");
        return rpmServer.robotStatus(robotDto);
    }

    /**
     * 机器人：查询全部，分页
     */
    @ApiOperation("机器人：分页查询")
    @PostMapping("/robotQuery")
    public ResponseResult robotQuery(@RequestBody VsRobotDto robotDto) throws Exception {
        if (null == robotDto || robotDto.getCurrent() <= 0 || robotDto.getSize() <= 0) {
            ExceptionCast.castFail("参数不全或有误");
        }
        return rpmServer.robotQuery(robotDto);
    }

    @ApiOperation("测试")
    @GetMapping("/test")
    public String test() throws Exception {
        return "OK";
    }
}
