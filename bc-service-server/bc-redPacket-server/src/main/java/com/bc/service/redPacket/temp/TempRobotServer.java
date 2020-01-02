package com.bc.service.redPacket.temp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.VsRobotDto;
import com.bc.service.common.redPacket.entity.VsAwardPlayer;
import com.bc.service.common.redPacket.entity.VsPayRecord;
import com.bc.service.common.redPacket.entity.VsRobot;
import com.bc.service.common.redPacket.entity.VsRobotRecord;
import com.bc.service.common.redPacket.service.IVsAwardPlayerService;
import com.bc.service.common.redPacket.service.IVsPayRecordService;
import com.bc.service.common.redPacket.service.IVsRobotRecordService;
import com.bc.service.common.redPacket.service.IVsRobotService;
import com.bc.service.redPacket.dto.PayMoneyDTO;
import com.bc.service.redPacket.dto.TaskAtom;
import com.bc.service.redPacket.vo.LoginResultVo;
import com.bc.service.redPacket.vo.PayResultVo;
import com.bc.service.redPacket.vo.QueryResultVo;
import com.bc.utils.MyHttpResult;
import com.bc.utils.SendRequest;
import com.bc.utils.project.MyBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mrt on 2019/4/12 0012 下午 9:26
 */
@Service
@Slf4j
public class TempRobotServer {
    @Autowired
    private IVsPayRecordService recordService;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private IVsAwardPlayerService playerService;
    @Autowired
    private JiuWuClient jiuWuClient;

    @Async
    public void pay(VsPayRecord record, VsAwardPlayer player) throws Exception {
        BigDecimal payAmount = record.getTotalAmount().divide(VarParam.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_DOWN);
        PayMoneyDTO payMoneyDTO = new PayMoneyDTO();
        payMoneyDTO.setUsername(player.getUserName());
        payMoneyDTO.setOutPayNo(record.getId() + "");
        payMoneyDTO.setPaidAmount(payAmount);
        payMoneyDTO.setMemo("红包雨彩金");

        ResponseResult responseResult = jiuWuClient.tempPay(payMoneyDTO);
        if (responseResult.isSuccess()) {
            updateRecord(record.getId(), true, JSON.toJSONString(responseResult), "");
        } else {
            updateRecord(record.getId(), false, JSON.toJSONString(responseResult), "");
        }
    }

    public void updateRecord(long recordId, boolean isSuccess, String resultInfo, String errorMes) {
        VsPayRecord record = recordService.getById(recordId);
        if (isSuccess) {
            record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_THREE);
            record.setPayRemark("打款成功");
        } else {
            record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_FAIL);
            record.setPayRemark(errorMes);
        }
        record.setResultInfo(resultInfo);
        record.setTimeNotice(LocalDateTime.now());
        //打钱后：更新record记录
        boolean updateById = recordService.updateById(record);
        if (updateById) {
            log.info("流水更新成功，recordId：{}，isSuccess:{},resultInfo:{},errorMes：{}", recordId, isSuccess, resultInfo, errorMes);
        } else {
            log.info("流水更新失败，recordId：{}，isSuccess:{},resultInfo:{},errorMes：{}", recordId, isSuccess, resultInfo, errorMes);
        }
    }
}
