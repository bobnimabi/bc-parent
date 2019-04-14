package com.bc.service.redPacket.server;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.service.common.redPacket.entity.VsPayRecord;
import com.bc.service.common.redPacket.service.*;
import com.bc.service.redPacket.Dto.TaskAtom;
import com.bc.service.redPacket.Vo.LoginResultVo;
import com.bc.service.redPacket.Vo.PayResultVo;
import com.bc.service.redPacket.Vo.QueryResultVo;
import com.bc.utils.MyHttpResult;
import com.bc.utils.SendRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mrt on 2019/4/12 0012 下午 9:26
 */
@Service
@Slf4j
public class RobotServer {
    @Autowired
    private IVsPayRecordService recordService;
    @Autowired
    private StringRedisTemplate redis;

    //1号机器人
    public static BasicCookieStore cookieStore1 = new BasicCookieStore();
    public static CloseableHttpClient client1 = HttpClients.custom().setDefaultCookieStore(cookieStore1).build();
    private static ReentrantLock robotLock1;


    //1号队列==1号机器人
    @Async
    public void exe1() throws Exception{
        //只允许一个线程执行队列
        if (robotLock1.tryLock()){
            Long size = 1L;
            while (true){
                Set<String> range = redis.opsForZSet().range(VarParam.RedPacketM.TASK_QUEUE, 0, 0);
                if (CollectionUtils.isEmpty(range)) break; //只有队列为空退出
                String json = range.iterator().next();
                TaskAtom taskAtom = JSON.parseObject(json, TaskAtom.class);
                Long expire = redis.getExpire(VarParam.RedPacketM.PLAYER_WAIT + taskAtom.getUserId(), TimeUnit.MILLISECONDS);
                if (expire == -2) {
                    redis.opsForZSet().removeRange(VarParam.RedPacketM.TASK_QUEUE, 0, 0);
                    dispatcher(taskAtom,client1);
                }
                Thread.sleep(expire);
            }
            robotLock1.unlock();
        }
    }

    /**
     * 执行打款
     */
    public void dispatcher(TaskAtom taskAtom,CloseableHttpClient client) throws Exception{
        log.info("机器人：分配：开始："+ JSON.toJSONString(taskAtom));

        //打钱前：查询
        QueryResultVo queryResultVo = this.queryInfo(taskAtom.getUsername(),client);

        //打钱前：更新record记录
        VsPayRecord record = recordService.getById(taskAtom.getRecordId());
        log.info("机器人：分配：获取用户记录："+ JSON.toJSONString(record));
        if (null == record) ExceptionCast.castFail("打款前：该条记录不存在：recordId:"+record.getId());
        record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_TWO);
        record.setOperatorDispatch(VarParam.RedPacketM.CONFIRM_DISPATCH_ONE);
        record.setTimePay(LocalDateTime.now());
        record.setAutoTry(record.getAutoTry() + 1);
        record.setPayInfo(JSON.toJSONString(taskAtom));
        record.setPreBalance(new BigDecimal(queryResultVo.getBalance()).multiply(VarParam.ONE_HUNDRED));//元->分
        boolean update = recordService.update(
                record,
                new UpdateWrapper<VsPayRecord>()
                        .eq("id", record.getId())
                        .eq("version", record.getVersion())
        );
        if (!update) ExceptionCast.castFail("打款前：更新record失败："+JSON.toJSONString(record)+" userId:"+taskAtom.getUserId());
        log.info("机器人：分配：更新record成功：recordId:" + record.getId());

        //打钱：单位：元 ->分转元
        BigDecimal payAmount = record.getTotalAmount().divide(VarParam.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_DOWN);
        PayResultVo payResultVo = this.pay(record.getUserName(), queryResultVo.getMemberId(), payAmount.toString(),client);
        if (payResultVo.getSuccess() == false) {
            log.info("机器人：分配：打款失败");
            record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_FAIL);
            record.setPayRemark(payResultVo.getMessage());
        } else {
            record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_FAIL);
            //成功后，再次查询余额
            QueryResultVo queryResultVo2 = this.queryInfo(taskAtom.getUsername(),client);
            record.setAftBalance(new BigDecimal(queryResultVo2.getBalance()).multiply(VarParam.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_DOWN));
        }
        record.setPayInfo(JSON.toJSONString(payResultVo));
        record.setTimeNotice(LocalDateTime.now());

        //打钱后：查询余额，更新record记录
        boolean updateById = recordService.updateById(record);
        if (!updateById) ExceptionCast.castFail("机器人：分配：打钱后更新record失败");
        log.info("机器人：分配：打钱后更新record成功:"+JSON.toJSONString(record));

        //限制11秒内不能再次打款
        redis.opsForValue().set(VarParam.RedPacketM.PLAYER_WAIT + taskAtom.getUserId(), "1", VarParam.RedPacketM.WAIT_SECOND, TimeUnit.SECONDS);
        log.info("机器人：分配：结束");
    }

    /**
     * 获取验证码
     */
    public void  getCode(OutputStream outputStream,CloseableHttpClient client) throws Exception {
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("type","agent");
        bodyMap.put("t", ""+Math.random());
        log.info("机器人：获取验证码:发送参数："+JSON.toJSONString(bodyMap));
        log.info("机器人：获取验证码:cookie："+JSON.toJSONString(cookieStore1.getCookies()));
        MyHttpResult result = SendRequest.sendGet(client,VarParam.RedPacketM.CODE_URL, headMap, bodyMap, VarParam.ENCODING,true);
        new BufferedInputStream(result.getHttpEntity().getContent());
        result.getHttpEntity().writeTo(outputStream);
    }

    /**
     * 登录
     * 1.ip限制（红包后台->代码限制ip，抽红包）
     * 2.opt（红包后台登录，谷歌验证器）
     */
    public ResponseResult login(String varCode,String account,String password,CloseableHttpClient client) throws Exception{
        log.info("机器人：登录开始：入参：验证码："+varCode+",账号："+account+" 密码："+password);
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("account",account);
        bodyMap.put("password",password);
        bodyMap.put("type","agentLogin");
        bodyMap.put("rmNum",varCode);
        log.info("机器人：登录发送参数："+JSON.toJSONString(bodyMap));
        log.info("机器人：登录cookie："+JSON.toJSONString(cookieStore1.getCookies()));
        MyHttpResult result = SendRequest.sendPost(client,VarParam.RedPacketM.LOGIN_URL, headMap, bodyMap, VarParam.ENCODING,true);

        String jsonStr = IOUtils.toString(result.getHttpEntity().getContent(), VarParam.ENCODING);
        log.info("机器人：响应结果："+jsonStr);
        LoginResultVo loginResultVo = JSON.parseObject(jsonStr, LoginResultVo.class);
        if (VarParam.SUCCESS_CODE != result.getStatusCode() || loginResultVo.getSuccess()== false) {
            log.info("登录失败：响应json："+jsonStr);
            log.info("登录失败：响应结果："+JSON.toJSONString(result));
            ExceptionCast.castFail("登录失败");
        }
        log.info("登录成功：响应json："+jsonStr);
        log.info("机器人：登录结束");
        return ResponseResult.SUCCESS();
    }

    /**
     * 查询 memeberId和现金余额
     */
    public static QueryResultVo queryInfo(String username,CloseableHttpClient client) throws Exception{
        log.info("机器人：查询账户信息开始：userName:"+username);
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("type","queryManDeposit");
        bodyMap.put("account",username);
        log.info("机器人：查询账户信息：发送参数："+JSON.toJSONString(bodyMap));
        MyHttpResult result = SendRequest.sendGet(client,VarParam.RedPacketM.QUERY_URL, headMap, bodyMap, VarParam.ENCODING,true);
        String htmlCode = result.getResultInfo();
        Document doc = Jsoup.parse(htmlCode);
        String memberId = doc.getElementById("memberId").attr("value");

        //第2个table的第3个tr的第2个td的值(注意：顺序都是从0开始)
        Elements table = doc.getElementsByTag("table");
        Element table2 = table.get(1);
        Elements trs = table2.getElementsByTag("tr");
        Element tr3 = trs.get(2);
        Elements tds = tr3.getElementsByTag("td");
        Element td = tds.get(1);
        String balance = td.text();
        log.info("机器人：查询账户信息：响应结果：账号:"+username+",memberId："+memberId+",现金余额：" + balance+"元");
        return new QueryResultVo(memberId,balance);
    }

    /**
     * 打钱
     */
    public static PayResultVo pay(String account,String memberId,String payAmount,CloseableHttpClient client)throws Exception{
        log.info("机器人：打款:开始：账号："+account+" memberId:"+memberId+" 打入金额："+payAmount+"  ");
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("type","saveSet"); //存款类型
        bodyMap.put("memberId",memberId);// 身份信息
        bodyMap.put("depositMoney",payAmount); //存入金额，单位：元
        bodyMap.put("depositMoneyRemark","红包入款");//存入金额备注
        bodyMap.put("depositPreStatus","0");//是否开启存款优惠 0不开启 1开启
        bodyMap.put("depositPre","0");//存款优惠
        bodyMap.put("depositPreRemark","");//存款优惠备注
        bodyMap.put("otherPreStatus","0");//是否开启汇款优惠 0不开启 1开启
        bodyMap.put("otherPre","0");//汇款优惠金额
        bodyMap.put("otherPreRemark","");//汇款优惠备注
        bodyMap.put("compBetCheckStatus","1");//是否开启综合打码量稽核  0不开启 1开启
        bodyMap.put("compBet",payAmount);//综合打码量稽核
        bodyMap.put("normalStatus","1");//常态性稽核
        bodyMap.put("depositPro","2存款优惠");//存入
        // 项目类型
        log.info("机器人：打款：发送参数："+JSON.toJSONString(bodyMap));
        MyHttpResult result = SendRequest.sendPost(client,VarParam.RedPacketM.PAY_URL, headMap, bodyMap, VarParam.ENCODING,true);
        String jsonStr = result.getResultInfo();
        log.info("机器人：打款：响应结果："+jsonStr);
        PayResultVo payResultVo = JSON.parseObject(jsonStr, PayResultVo.class);
        log.info("机器人：打款：结束：账号："+account+" memberId:"+memberId+" 打入金额："+payAmount+"元");
        return payResultVo;
    }
}
