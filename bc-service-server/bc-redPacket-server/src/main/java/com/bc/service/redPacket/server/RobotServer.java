package com.bc.service.redPacket.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.VsRobotDto;
import com.bc.service.common.redPacket.entity.VsPayRecord;
import com.bc.service.common.redPacket.entity.VsRobot;
import com.bc.service.common.redPacket.entity.VsRobotRecord;
import com.bc.service.common.redPacket.service.*;
import com.bc.service.redPacket.dto.TaskAtom;
import com.bc.service.redPacket.vo.LoginResultVo;
import com.bc.service.redPacket.vo.PayResultVo;
import com.bc.service.redPacket.vo.QueryResultVo;
import com.bc.utils.MyHttpResult;
import com.bc.utils.SendRequest;
import com.bc.utils.SymmetricEncoder;
import com.bc.utils.project.MyBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
    @Autowired
    private IVsRobotService robotService;
    @Autowired
    private IVsRobotRecordService robotRecordService;
    @Autowired
    private IVsAwardPlayerService playerService;
    //总执行队列锁
    private static ReentrantLock exeQueueLock = new ReentrantLock();
    //初始化机器人集合锁
    private static ReentrantLock initRobotLock = new ReentrantLock();
    //机器人客户端集合
    public static Map<Integer,CloseableHttpClient> clientMap = new HashMap<>();
    //机器人客户端集合
    public static Map<Integer,ReentrantLock> clientLockMap = new HashMap<>();
    //负载均衡
    private static int robinNum = 0;

    //初始化机器人集合
    @PostConstruct
    public void initRobots() throws Exception{
        redis.delete(VarParam.RedPacketM.ROBOT_MAP);
        List<VsRobot> robots = getRobots();
    }

    //初始化所有client
    @PostConstruct
    public void initClient() throws Exception{
        List<VsRobot> robots = getRobots();
        robots.forEach(robot -> {
            BasicCookieStore cookieStore = new BasicCookieStore();
            String userAgent = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36";
            CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).setUserAgent(userAgent).build();
            clientMap.put(robot.getRobotNum(), client);
        });
    }

    //初始化所有client的锁
    @PostConstruct
    public void initClientLock() throws Exception{
        List<VsRobot> robots = getRobots();
        robots.forEach(robot -> {
            clientLockMap.put(robot.getRobotNum(), new ReentrantLock());
        });
    }

    //调用循环查询=>异步调用
    @PostConstruct
    public void initInterval() throws Exception{
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    intervalQuery();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //刷新cookie用
    private static TaskAtom commTaskAtom = new TaskAtom(null,VarParam.RedPacketM.ROBOT_QUERY_ACCOUNT,null);

    //每10分钟轮询查询一次，防止cookie失效
    @Async
    public void intervalQuery() throws Exception {
        while (true) {
            //先睡5分钟，防止刚启动机器人还没有登录
            Thread.sleep(1000*60*5);
            List<VsRobot> robots = getRobots();
            if (!CollectionUtils.isEmpty(robots)) {
                //去除状态为0的机器人
                Iterator<VsRobot> iterator = robots.iterator();
                while (iterator.hasNext()) {
                    VsRobot next = iterator.next();
                    if (VarParam.NO == next.getRobotStatus()) {
                        iterator.remove();
                    }
                }
                if (!CollectionUtils.isEmpty(robots)) {
                    for (VsRobot robot:robots) {
                        log.info("5分钟机器人轮询：robotNum:" + robot.getRobotNum());
                        ReentrantLock lock = clientLockMap.get(robot.getRobotNum());
                        //如果能获取到锁，说明该客户端现在空闲，故调用一次查询
                        if (lock.tryLock()) {
                            this.queryInfo(commTaskAtom, robot);
                            log.info("5分钟机器人轮询：robotNum:" + robot.getRobotNum()+",刷新cookie成功");
                            lock.unlock();
                        }else{
                            log.info("5分钟机器人轮询：robotNum:" + robot.getRobotNum()+",正在执行，无需cookie刷新");
                        }
                    }
                }
            }
        }
    }

    //获取指定客户端
    public CloseableHttpClient getClient(int robotNum) throws Exception{
        CloseableHttpClient client = clientMap.get(robotNum);
        if (null == client) {
            initClient();
            client = clientMap.get(robotNum);
            if (null == client) {
                ExceptionCast.castFail("获取指定可以端：robotNum："+robotNum+" ,未获取到指定客户端");
            }
        }
        return client;
    }
    //获取指定客户端的锁
    public ReentrantLock getClientLock(int robotNum) throws Exception{
        ReentrantLock lock = clientLockMap.get(robotNum);
        if (null == lock) {
            initClientLock();
            lock = clientLockMap.get(robotNum);
            if (null == lock) {
                ExceptionCast.castFail("获取指定客户端：robotNum："+robotNum+" ,未获取到指定客户端锁");
            }
        }
        return lock;
    }

    //获取所有的机器人<robotNum,VsRobot>
    public List<VsRobot> getRobots() throws Exception{
        List<Object> robotStrs = redis.opsForHash().values(VarParam.RedPacketM.ROBOT_MAP);
        if (CollectionUtils.isEmpty(robotStrs)) {
            if (initRobotLock.tryLock()) {
                List<VsRobot> list = null;
                robotStrs = redis.opsForHash().values(VarParam.RedPacketM.ROBOT_MAP);
                if (CollectionUtils.isEmpty(robotStrs)) {
                    list = robotService.list();
                    if (CollectionUtils.isEmpty(list)) {
                        ExceptionCast.castFail("机器人初始化：数据库没有任何机器人了，请立刻配置");
                    }
                    Map<String,String> jsonMap = new HashMap<>();
                    list.forEach(robot -> jsonMap.put(robot.getRobotNum()+"",JSON.toJSONString(robot)));
                    redis.opsForHash().putAll(VarParam.RedPacketM.ROBOT_MAP,jsonMap);
                    log.info("机器人初始化：入redis队列成功");
                }
                initRobotLock.unlock();
                if (!CollectionUtils.isEmpty(list)) return list;
            } else {
                Thread.sleep(100);
                return getRobots();
            }
        }
        List<VsRobot> robots = new ArrayList<>();
        robotStrs.forEach(robotStr -> robots.add(JSON.parseObject(String.valueOf(robotStr), VsRobot.class)));
        robots.sort(new Comparator<VsRobot>() {
            @Override
            public int compare(VsRobot o1, VsRobot o2) {
                return o1.getRobotNum() - o2.getRobotNum();
            }
        });
        return robots;
    }
    /**
     * 机器人增加
     */
    public ResponseResult addRobot(VsRobotDto robotDto) throws Exception{
        VsRobot robot = new VsRobot();
        MyBeanUtil.copyProperties(robotDto,robot);
        //更新数据库记录
        List<VsRobot> robots = robotService.list();
        robots.forEach(robotc -> {
            if (robot.getRobotNum() == robotc.getRobotNum())
                ExceptionCast.castFail("robotNum重复");
        });
        boolean save = robotService.save(robot);
        if (!save) ExceptionCast.castFail("机器人增加：入库失败");
        //给机器人增加客户端和锁
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        clientMap.put(robot.getRobotNum(), client);
        clientLockMap.put(robot.getRobotNum(), new ReentrantLock());
        //更新下机器人缓存
        redis.delete(VarParam.RedPacketM.ROBOT_MAP);
        getRobots();
        return ResponseResult.SUCCESS();
    }

    /**
     * 机器人修改
     */
    public ResponseResult updateRobot(VsRobotDto robotDto) throws Exception{
        VsRobot robot = new VsRobot();
        MyBeanUtil.copyProperties(robotDto,robot);
        boolean updateById = robotService.updateById(robot);
        if (!updateById) ExceptionCast.castFail("机器人修改：修改失败:"+JSON.toJSONString(robot));
        log.info("机器人修改：修改成功：robotId:"+robot.getId());
        //更新下机器人缓存
        redis.delete(VarParam.RedPacketM.ROBOT_MAP);
        getRobots();
        return ResponseResult.SUCCESS();
    }

    /**
     * 机器人开启或关闭
     */
    public ResponseResult robotStatus(VsRobotDto robotDto) throws Exception{
        VsRobot robot = new VsRobot();
        MyBeanUtil.copyProperties(robotDto,robot);
        robot.setRobotInfo(robotDto.getRobotStatus() == VarParam.YES ? "机器人已开启：运行中" : "机器人已关闭");

        boolean updateById = robotService.updateById(robot);
        if (!updateById) {
            ExceptionCast.castFail("操作失败");
        }
        //更新下机器人缓存
        redis.delete(VarParam.RedPacketM.ROBOT_MAP);
        getRobots();
        return ResponseResult.SUCCESS();
    }

    //执行队列
    @Async
    public void exeQueue() throws Exception{
        //只允许一个线程执行调整队列
        if (exeQueueLock.tryLock()){
            long recordId = 0L;
            out:while (true){
                Set<String> range = redis.opsForZSet().range(VarParam.RedPacketM.JUST_TASK_QUEUE, 0, 0);//包头包尾
                if (CollectionUtils.isEmpty(range)){
                    break; //只有队列为空退出
                }
                String json = range.iterator().next();
                TaskAtom taskAtom = JSON.parseObject(json, TaskAtom.class);
                Long expireTime = redis.getExpire(VarParam.RedPacketM.PLAYER_WAIT + taskAtom.getUserId(), TimeUnit.MILLISECONDS);
                if (expireTime == -1) {
                    log.error("userId:" + taskAtom.getUserId() + " 该用户红包过期时间无限长");
                    redis.opsForZSet().removeRange(VarParam.RedPacketM.JUST_TASK_QUEUE, 0, 0);
                    log.error("userId:" + taskAtom.getUserId() + " 该用户红包已从缓存中移除");
                    continue;
                }
                if (expireTime > 0) {
                    //如果recordId没有变化，说明队列头没有变化，则sleep
                    if (recordId == taskAtom.getRecordId()) {
                        Thread.sleep(1000);//这里不是sleep(expireTime)，防止新任务进来等待
                        continue;
                    }
                    //调整score为expireTime
                    Double oldScore = redis.opsForZSet().score(VarParam.RedPacketM.JUST_TASK_QUEUE, json);
                    redis.opsForZSet().incrementScore(VarParam.RedPacketM.JUST_TASK_QUEUE, json, Double.valueOf(expireTime + "") - oldScore);
                    //记录首位的recordId
                    recordId = taskAtom.getRecordId();
                    continue;
                }else{//可执行
                    //负载均衡
                    List<VsRobot> robots = getRobots();
                    VsRobot robotGo = null;
                    int i=0;
                    while (true) {
                        VsRobot robot = robots.get(robinNum++ % robots.size());
                        if (VarParam.YES == robot.getRobotStatus()) {
                            robotGo=robot;
                            break;
                        }
                        if (++i == robots.size()) {
                            log.error("机器人轮询：所有机器人状态：关闭,或不存在");
                            break out;
                        }
                    }
                    log.info("机器人：负载均衡：启动机器人执行任务：robotNum:" + robotGo.getRobotNum()+"任务："+json);
                    //找到机器人再入任务队列
                    redis.opsForZSet().removeRange(VarParam.RedPacketM.JUST_TASK_QUEUE, 0, 0);
                    redis.opsForList().leftPush(VarParam.RedPacketM.TASK_QUEUE, json);
                    //抢食模式=>打款
                    ReentrantLock clientLock = this.getClientLock(robotGo.getRobotNum());
                    robinGo(robotGo,clientLock);
                    //限制11秒内不能再次打款
                    redis.opsForValue().set(VarParam.RedPacketM.PLAYER_WAIT + taskAtom.getUserId(), "1", VarParam.RedPacketM.WAIT_SECOND, TimeUnit.SECONDS);
                }
            }
            exeQueueLock.unlock();
        }
    }

    /**
     * 客户端负载均衡
     */
    @Async
    public void robinGo(VsRobot robotGo,ReentrantLock clientLock) throws Exception{
        if (clientLock.tryLock()) {
            log.info("robinGo:加锁线程："+Thread.currentThread().getId());
            while (true) {
                if (VarParam.NO == robotGo.getRobotStatus()) break;
                String jsonStr = redis.opsForList().rightPop(VarParam.RedPacketM.TASK_QUEUE);
                if (StringUtils.isEmpty(jsonStr)) break;
                TaskAtom taskAtom = JSON.parseObject(jsonStr, TaskAtom.class);
                log.info("机器人执行队列任务：robotNum:" + robotGo.getRobotNum() + " ,任务：" + jsonStr);
                dispatcher(taskAtom, robotGo);
            }
            log.info("robinGo:开锁线程："+Thread.currentThread().getId());
            clientLock.unlock();
        }
    }

    /**
     * 执行打款流程
     */
    public void dispatcher(TaskAtom taskAtom,VsRobot robot) throws Exception{
        log.info("机器人：分配：开始："+ JSON.toJSONString(taskAtom)+" robot"+JSON.toJSONString(robot));

        //打钱前：查询
        QueryResultVo queryResultVo = this.queryInfo(taskAtom, robot);
        if (null == queryResultVo) return;

        //打钱前：更新record记录
        VsPayRecord record = recordService.getById(taskAtom.getRecordId());
        if (null == record) ExceptionCast.castFail("打款前：该条记录不存在：recordId:"+record.getId());
        log.info("机器人：分配：获取用户记录："+ JSON.toJSONString(record));
        record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_TWO);
        record.setOperatorDispatch(StringUtils.isEmpty(record.getOperatorDispatch()) ? "robotNum:"+robot.getRobotNum() : record.getOperatorDispatch());
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

        //打钱：单位：分 ->元
        BigDecimal payAmount = record.getTotalAmount().divide(VarParam.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_DOWN);
        PayResultVo payResultVo = this.pay(record.getUserName(), queryResultVo.getMemberId(), payAmount.toString(),robot,record.getId());
        if (null == payResultVo) return;
        if (payResultVo.getSuccess() == false) {
            log.info("机器人：分配：打款失败");
            record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_FAIL);
            record.setPayRemark(payResultVo.getMessage());
        } else {
            record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_THREE);
            record.setPayRemark("打款成功");
        }
        record.setResultInfo(JSON.toJSONString(payResultVo));
        record.setTimeNotice(LocalDateTime.now());

        //打钱后：更新record记录
        boolean updateById = recordService.updateById(record);
        if (!updateById) ExceptionCast.castFail("机器人：分配：打钱后更新record失败");
        log.info("机器人：分配：打钱后更新record成功:"+JSON.toJSONString(record));
        log.info("机器人：分配：结束");
    }

    /**
     * 获取验证码
     */
    public MyHttpResult  getCode(int robinNum) throws Exception {
        log.info("机器人：获取验证码：开始：入参：robotNum：" + robinNum);
        CloseableHttpClient client = this.getClient(robinNum);
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("type","agent");
        bodyMap.put("t", ""+Math.random());
        log.info("机器人：获取验证码:发送参数："+JSON.toJSONString(bodyMap));
        MyHttpResult result = SendRequest.sendGet(client,VarParam.RedPacketM.CODE_URL, headMap, bodyMap, VarParam.ENCODING,true);
       return result;
    }

    /**
     * 登录
     */
    public ResponseResult login(String imageCode,int robotNum,int varCode) throws Exception{
        log.info("机器人：登录开始：入参：验证码："+imageCode+",机器人编码："+robotNum);

        Object robotjsonStr = redis.opsForHash().get(VarParam.RedPacketM.ROBOT_MAP, robotNum+"");
        if (null == robotjsonStr) ExceptionCast.castFail("机器人：登录：未获取到robot，robotNum：" + robotNum);
        log.info("robotjsonStr:"+robotjsonStr);
        VsRobot robot = JSON.parseObject(String.valueOf(robotjsonStr), VsRobot.class);
        //获取账号后base64编码
        String account = robot.getPlatAccount();
        String deAccount = new String(Base64Utils.encode(account.getBytes())).trim();
        //获取密码后先解密，在编码
        String platPassword = robot.getPlatPassword();
        String dePassword = new String(Base64Utils.encode(DigestUtils.md5DigestAsHex(platPassword.trim().getBytes()).getBytes()));
        CloseableHttpClient client = this.getClient(robotNum);

        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("account",deAccount);
        bodyMap.put("password",dePassword);
        bodyMap.put("dynamicPassword",varCode+"");
        bodyMap.put("type","agentLogin");
        bodyMap.put("rmNum",imageCode);
        //日志里不打印密码
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(Map.class, "account","type","rmNum");
        log.info("机器人：登录发送参数："+JSON.toJSONString(bodyMap,filter));
        MyHttpResult result = SendRequest.sendPost(client,VarParam.RedPacketM.LOGIN_URL, headMap, bodyMap, VarParam.ENCODING,true);

        String jsonStr = result.getResultInfo();
        log.info("机器人：响应结果："+jsonStr);
        LoginResultVo loginResultVo = JSON.parseObject(jsonStr, LoginResultVo.class);
        if (VarParam.SUCCESS_CODE != result.getStatusCode() || loginResultVo.getSuccess()== false) {
            ExceptionCast.castFail(loginResultVo.getMsg());
        }
        log.info("登录成功：响应json："+jsonStr);
        log.info("机器人：登录结束");

        //更新下数据库的登录次数
        robotService.update(
                new UpdateWrapper<VsRobot>()
                        .eq("id", robot.getId())
                        .set("login_time", robot.getLoseTimes() + 1)
                        .set("robot_info", robot.getRobotStatus() == VarParam.YES ? "登录成功，机器人：运行中" : "登录成功：请开启机器人")
        );
        redis.delete(VarParam.RedPacketM.ROBOT_MAP);
        this.getRobots();
        //登录完以后再调红包队列
        exeQueue();
        return ResponseResult.SUCCESS_MES("登录成功");
    }

    /**
     * 查询 memeberId和现金余额
     */
    public QueryResultVo queryInfo(TaskAtom taskAtom,VsRobot robot) throws Exception{
        int robotNum = robot.getRobotNum();
        log.info("机器人：查询账户信息开始：userName:"+taskAtom.getUsername()+" robotNum:"+robotNum);
        CloseableHttpClient client = clientMap.get(robotNum);
        //头
        Map<String, String> headMap = new HashMap<>();
        //体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("type","queryManDeposit");
        bodyMap.put("account",taskAtom.getUsername());
        log.info("机器人：查询账户信息：发送参数："+JSON.toJSONString(bodyMap));
        String htmlCode = null;
        try {
            MyHttpResult result = SendRequest.sendGet(client,VarParam.RedPacketM.QUERY_URL, headMap, bodyMap, VarParam.ENCODING,true);
            htmlCode = result.getResultInfo();
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
            log.info("机器人：查询账户信息：响应结果：账号:"+taskAtom.getUsername()+",memberId："+memberId+",现金余额：" + balance+"元");
            return new QueryResultVo(memberId,balance);
        } catch (Exception e) {
            //这里如果出现异常说明cookie有问题，机器人就处理离线状态
            robot.setRobotStatus(VarParam.NO);//通知该机器人的循环停止
            robot.setLoseTimes(robot.getLoseTimes() + 1);
            robot.setRobotInfo("查询账户信息异常，请联系管理员检查");
            log.info("查询账户信息异常:更新数据库robot"+JSON.toJSONString(robot));
            boolean update = robotService.update(
                    new UpdateWrapper<VsRobot>()
                            .eq("id", robot.getId())
                            .set("robot_status", robot.getRobotStatus())
                            .set("login_time", robot.getLoseTimes())
                            .set("robot_info", robot.getRobotInfo())

            );
            if (!update) ExceptionCast.castFail("查询账户信息异常,更新数据库robot状态失败:"+JSON.toJSONString(robot));
            redis.delete(VarParam.RedPacketM.ROBOT_MAP);
            this.getRobots();

            //更新record
            if (null != taskAtom.getRecordId()) {
                VsPayRecord record = recordService.getById(taskAtom.getRecordId());
                if (null == record) ExceptionCast.castFail("查询账户信息异常：该条记录不存在：recordId:"+taskAtom.getRecordId());
                record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_FAIL);
                record.setResultInfo("机器人查询：离线，编号：" + robotNum + "，派送失败，请人工打款");
                record.setPayRemark("机器人查询：离线，编号：" + robotNum + "，派送失败，请人工打款");
                record.setTimeNotice(LocalDateTime.now());
                boolean update1 = recordService.update(
                        new UpdateWrapper<VsPayRecord>()
                                .eq("id", record.getId())
                                .set("pay_status",record.getPayStatus())
                                .set("pay_remark", record.getPayRemark())
                                .set("result_info",record.getResultInfo())
                                .set("time_notice", record.getTimeNotice())
                );
                log.info("查询账户信息异常:更新record："+JSON.toJSONString(record));
                if (!update1) ExceptionCast.castFail("查询账户信息异常:更新record失败："+JSON.toJSONString(record));
            }
            return null;
        }

    }

    /**
     * 打钱
     */
    public PayResultVo pay(String account,String memberId,String payAmount,VsRobot robot,Long recordId)throws Exception{
        log.info("机器人：打款:开始：账号："+account+" memberId:"+memberId+" 打入金额："+payAmount+"  ");
        int robotNum = robot.getRobotNum();
        CloseableHttpClient client = this.getClient(robotNum);
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

        //机器人流水
        VsRobotRecord robotRecord = new VsRobotRecord();
        robotRecord.setId(Long.parseLong(System.currentTimeMillis()+""+(int)(Math.random()*10)));
        robotRecord.setRobotNum(robotNum);
        robotRecord.setPayRecordId(recordId);
        robotRecord.setUserName(account);
        robotRecord.setTotalAmount(new BigDecimal(payAmount).multiply(VarParam.ONE_HUNDRED));
        robotRecord.setReqInfo(JSON.toJSONString(bodyMap));
        boolean save = robotRecordService.save(robotRecord);
        if (!save) ExceptionCast.castFail("机器人：打款：机器人流水：存入失败："+JSON.toJSONString(robotRecord));
        log.info("机器人：打款：机器人流水：存入成功，robotRecordId："+robotRecord.getId());

        String jsonStr = null;
        try {
            MyHttpResult result = SendRequest.sendPost(client,VarParam.RedPacketM.PAY_URL, headMap, bodyMap, VarParam.ENCODING,true);
            jsonStr = result.getResultInfo();
            log.info("机器人：打款：响应结果："+jsonStr);
            PayResultVo payResultVo = JSON.parseObject(jsonStr, PayResultVo.class);
            log.info("机器人：打款：结束：账号："+account+" memberId:"+memberId+" 打入金额："+payAmount+"元");
            boolean update = robotRecordService.update(
                    new UpdateWrapper<VsRobotRecord>()
                            .eq("id", robotRecord.getId())
                            .set("resp_info", jsonStr)
                            .set("record_status", payResultVo.getSuccess() == true ? VarParam.YES : VarParam.NO)
            );
            if (!update)
                ExceptionCast.castFail("机器人：打款：结束：更新机器人流水失败：resp_info:" + jsonStr + " record_status:" + VarParam.YES);
            log.info("机器人：打款：结束：更新机器人流水成功：resp_info:" + jsonStr + " record_status:" + VarParam.YES);
            return payResultVo;
        } catch (Exception e) {
            log.error("机器人：打款异常", e);
            //测试是否是真的掉线 username,recordId
            VsPayRecord payRecord = recordService.getById(recordId);
            if (null == payRecord) {
                log.error("机器人：打款:异常,获取不到payRecord:recordId"+recordId);
                return null;
            }
            //如果掉线，修改机器人状态为不可用
            Thread.sleep(3000);
            QueryResultVo queryResultVo = this.queryInfo(new TaskAtom(null, payRecord.getUserName(), recordId), robot);
            if (null == queryResultVo) {
                robot.setRobotStatus(VarParam.NO);//通知该机器人循环停止
                robot.setLoseTimes(robot.getLoseTimes() + 1);
                robot.setRobotInfo("机器人：打款：异常，请联系管理员检查");
                log.info("机器人：打款：异常:更新数据库robot" + JSON.toJSONString(robot));
                boolean update = robotService.update(
                        new UpdateWrapper<VsRobot>()
                                .eq("id", robot.getId())
                                .set("robot_status", robot.getRobotStatus())
                                .set("login_time", robot.getLoseTimes())
                                .set("robot_info", robot.getRobotInfo())
                );
                if (!update) ExceptionCast.castFail("机器人：打款：异常,更新数据库robot状态失败:" + JSON.toJSONString(robot));
                redis.delete(VarParam.RedPacketM.ROBOT_MAP);
                this.getRobots();
            } else {
                log.info("机器人：打款：异常:机器人未掉线,查询测试成功："+JSON.toJSONString(queryResultVo));
            }

            //更新机器人流水
            boolean update2 = robotRecordService.update(
                    new UpdateWrapper<VsRobotRecord>()
                            .eq("id", robotRecord.getId())
                            .set("resp_info", "机器人：打款：异常")
                            .set("record_status", VarParam.NO)
            );
            if (!update2)
                ExceptionCast.castFail("机器人：打款：结束：更新机器人流水失败：resp_info:" + jsonStr + " record_status:" + VarParam.YES);
            log.info("机器人：打款：结束：更新机器人流水成功：resp_info:" + jsonStr + " record_status:" + VarParam.YES);

            //更新record
            VsPayRecord record = recordService.getById(recordId);
            if (null == record) ExceptionCast.castFail("查询账户信息异常：该条记录不存在：recordId:"+record.getId());
            record.setPayStatus(VarParam.RedPacketM.PAY_STATUS_FAIL);
            record.setResultInfo("机器人：打款：异常，robotNum：" + robotNum + "，派送失败，请人工打款");
            record.setPayRemark("机器人：打款：异常，robotNum：" + robotNum + "，派送失败，请人工打款");
            record.setTimeNotice(LocalDateTime.now());
            boolean update1 = recordService.update(
                    new UpdateWrapper<VsPayRecord>()
                            .eq("id", record.getId())
                            .set("pay_status",record.getPayStatus())
                            .set("pay_remark", record.getPayRemark())
                            .set("result_info",record.getResultInfo())
                            .set("time_notice", record.getTimeNotice())
            );
            log.info("机器人：打款：异常:更新record："+JSON.toJSONString(record));
            if (!update1) ExceptionCast.castFail("机器人：打款：异常:更新record失败："+JSON.toJSONString(record));
            return null;
        }
    }


}
