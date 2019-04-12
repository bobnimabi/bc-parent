package com.bc.service.redPacket.server;

import com.alibaba.fastjson.JSON;
import com.bc.common.constant.VarParam;
import com.bc.service.common.login.service.IXcUserService;
import com.bc.service.common.redPacket.service.*;
import com.bc.service.redPacket.Dto.TaskAtom;
import com.bc.utils.Result;
import com.bc.utils.SendRequest;
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
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by mrt on 2019/4/12 0012 下午 9:26
 */
@Service
public class RobotServer {
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
    private IXcUserService userService;

    private static BasicCookieStore cookieStore1 = new BasicCookieStore();
    private static CloseableHttpClient client1 = HttpClients.custom().setDefaultCookieStore(cookieStore1).build();

    @Async
    public void exe1() throws Exception{
        while (true){
            Set<String> range = redis.opsForZSet().range(VarParam.RedPacketM.TASK_QUEUE, 0, 0);
            String json = range.iterator().next();
            TaskAtom taskAtom = JSON.parseObject(json, TaskAtom.class);
            Long expire = redis.getExpire(VarParam.RedPacketM.PLAYER_WAIT + taskAtom.getUserId(), TimeUnit.MILLISECONDS);
            if (expire == -2) {
                dispatcher("");
                Long size = redis.opsForZSet().size(VarParam.RedPacketM.TASK_QUEUE);
                if (0 == size)
                    break;
                continue;
            }
            Thread.sleep(expire);
        }
    }
    public void dispatcher(String account){
        //打钱前：更新record记录
        //打钱
        //打钱后：更新record记录

    }

    public static final String ENCODING = "utf-8";

    static String codeUrl = "https://ovwyfq040.prealmd.com/agent/validCode?type=agent";
    /**
     * 获取验证码
     */
    public static void  getCode() throws Exception {
        String imageCodeUrl = codeUrl+"&t="+Math.random();

        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("type","agent");
        bodyMap.put("t", ""+Math.random());
        System.out.println("获取验证码请求前cookie："+JSON.toJSONString(cookieStore1.getCookies()));
        Result result = SendRequest.sendGet(client1,imageCodeUrl, headMap, bodyMap, ENCODING,true);
        result.getHttpEntity().writeTo(new FileOutputStream("E:\\pic\\a.jpg"));
        login();
    }

    /**
     * 登录
     * 1.ip限制（红包后台->代码限制ip，抽红包）
     * 2.opt（红包后台登录，谷歌验证器）
     */
    public static void login()throws Exception{
        String url = "https://ovwyfq040.prealmd.com/agent/agent";
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("account","amlzaHUwMDE=");
        bodyMap.put("password","MWU0NDAzODQwNjAyYmZhZmM1M2E3ZDVmOTY1M2JmYzA=");
        bodyMap.put("type","agentLogin");
        Scanner scan = new Scanner(System.in);
        System.out.print("请输入验证码：");
        String code = scan.next();
        System.out.println("录入的验证码：" + code);
        bodyMap.put("rmNum",code);
        System.out.println("获取验证码请求前cookie："+JSON.toJSONString(cookieStore1.getCookies()));
        Result result = SendRequest.sendPost(client1,url, headMap, bodyMap, ENCODING,true);
        System.out.println(result.toString());

        String toString = IOUtils.toString(result.getHttpEntity().getContent(), ENCODING);
        System.out.println(toString);
    }

    /**
     * 查询 memeberId和现金余额
     */
    public static int k = 0;
    public static String account = "aaa7158";
    public static String payAmount = "1.1";
    public static void query(String payAmount)throws Exception{
        System.out.println("查询账户余额开始。。。");
        k+=1;
        String url = "https://ovwyfq040.prealmd.com/agent/ComRecordServlet";
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("type","queryManDeposit");
        bodyMap.put("account",account);
        Result result = SendRequest.sendGet(client1,url, headMap, bodyMap, ENCODING,true);

        String htmlCode = IOUtils.toString(result.getHttpEntity().getContent(), ENCODING);
        Document doc = Jsoup.parse(htmlCode);
        String memberId = doc.getElementById("memberId").attr("value");

        //第2个table的第3个tr的第2个td的值(注意：顺序都是从0开始)
        Elements table = doc.getElementsByTag("table");
        Element table2 = table.get(1);
        Elements trs = table2.getElementsByTag("tr");
        Element tr3 = trs.get(2);
        Elements tds = tr3.getElementsByTag("td");
        Element td = tds.get(1);
        String amount = td.text();
        System.out.println("账号:"+account+",memberId："+memberId);
        System.out.println("账号:" + account + ",现金余额：" + amount);
        System.out.println("查询账户余额结束");
        pay(account,memberId,payAmount);
    }

    /**
     * 打钱
     */
    public static void pay(String account,String memberId,String payAmount)throws Exception{
        System.out.println("账号："+account+" memberId:"+memberId+" 打入金额："+payAmount+"  开始。。。");
        String url = "https://ovwyfq040.prealmd.com/agent/ComRecordServlet";
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("type","saveSet"); //存款类型
        bodyMap.put("memberId",memberId);// 身份信息
        bodyMap.put("depositMoney",payAmount); //存入金额
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
        bodyMap.put("depositPro","2存款优惠");//存入项目类型
        Result result = SendRequest.sendPost(client1,url, headMap, bodyMap, ENCODING,true);

        String result2 = IOUtils.toString(result.getHttpEntity().getContent(), ENCODING);
        System.out.println("打款结果：" + result2);
        System.out.println("账号："+account+" memberId:"+memberId+" 打入金额："+payAmount+"  结束");
    }
}
