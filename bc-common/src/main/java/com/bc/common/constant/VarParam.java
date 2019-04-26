package com.bc.common.constant;

import java.math.BigDecimal;

/**
 * Created by mrt on 2019/4/6 0006 下午 6:39
 */
public class VarParam {
    //逻辑常量是 与 否
    public static final int YES = 1;
    public static final int NO = 0;

    //金额转换需要分->元
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    //字符编码
    public static final String ENCODING = "utf-8";
    //响应成功状态码
    public static final int SUCCESS_CODE = 200;
    /**
     * 登录
     */
    public static class Login{
        //项目的redis前缀
        public static final String LOGIN_PRE = "Login:";
        //用户的登录标志（redis的key）
        public static final String LOGIN_FLAG_PRE = LOGIN_PRE + "Login_flag:";
        //图片验证码的sessionKey
        public static final String SESSION_KEY_VALIDATE_IMAGE = "Session_key_validate_image";
    }

    /**
     * 红包管理后台
     */
    public static class RedPacketM {
        /**
         * redis-key
         */
        //项目前缀
        public static final String REDPACKET_M_PRE = "redP:";
        //红包活动redis-key
        public static final String ACTIVE_KEY = REDPACKET_M_PRE + "active:";
        //奖品
        public static final String PRIZE_KEY = REDPACKET_M_PRE + "prize";
        //转换规则
        public static final String TRANSFORM_KEY = REDPACKET_M_PRE + "transform";
        //默认主题
        public static final String DEFAULT_THEME = REDPACKET_M_PRE + "defaultTheme";
        //机器人调整任务队列
        public static final String JUST_TASK_QUEUE = REDPACKET_M_PRE + "justQueue";
        //机器人执行任务队列
        public static final String TASK_QUEUE = REDPACKET_M_PRE + "taskQueue";
        //玩家需等待时间
        public static final String PLAYER_WAIT = REDPACKET_M_PRE + "wait:";
        //机器人状态
        public static final String ROBOT_MAP = REDPACKET_M_PRE + "robotList";
        //布隆过滤器：key
        public static final String BLOOM_RED = VarParam.RedPacketM.REDPACKET_M_PRE+"bf";

        /**
         * 项目参数
         */
        //红包活动的id
        public static final int AWARD_ACTIVE_ID = 1;
        //库存数量的最小值
        public static final int PRIZE_STORE_NUM_MIN = 0;
        //布隆过滤器：插入数据大小
        public static final int SIZE_RED = 10000;
        //布隆过滤器：错误率
        public static final double FPP_RED = 0.03;
        //打款机器人：图片验证码url
        public static final String CODE_URL = "https://ovwyfq040.prealmd.com/agent/validCode";
        //打款机器人：登录url
        public static String LOGIN_URL = "https://ovwyfq040.prealmd.com/agent/agent";
        //打款机器人：查询url
        public static String QUERY_URL = "https://ovwyfq040.prealmd.com/agent/ComRecordServlet";
        //打款机器人：打款url
        public static String PAY_URL = "https://ovwyfq040.prealmd.com/agent/ComRecordServlet";
        //限制玩家11秒内不能重复打款
        public static final int WAIT_SECOND = 11;
        //机器人：平台密码加密
        public static final String PASS_KEY = "jfxmk!wl.c4yg6gg?#&*%~+=&jv4399";





        /**
         * 条件查询参数
         */
        //会员分页查询：创建时间降序
        public static final int PLAYER_CREATTIME_DESC = 1;
        //会员分页查询：金额降序
        public static final int PLAYER_AMOUNT_DESC = 2;
        //会员分页查询：可抽奖次数降序
        public static final int PLAYER_JOINTIME_DESC = 3;

        /**
         * mysql 状态值
         */
        //奖品类型：1红包
        public static final int PRIZE_TYPE_ONE = 1;
        //奖品类型：2谢谢参与
        public static final int PRIZE_TYPE_TWO = 2;
        //导入会员类型：1覆盖
        public static final int IMPORT_PLAYERS_TYPE_ONE = 1;
        //导入会员类型：2累加
        public static final int IMPORT_PLAYERS_TYPE_TWO = 2;
        //支付状态：待派送
        public static final int PAY_STATUS_ONE = 1;
        //支付状态：派送中
        public static final int PAY_STATUS_TWO = 2;
        //支付状态：已派送
        public static final int PAY_STATUS_THREE = 3;
        //支付状态：派送失败
        public static final int PAY_STATUS_FAIL = 4;
        //客户端类型：1pc
        public static final int CLIENT_TYPE_ONE = 1;
        //客户端类型：2mobile
        public static final int CLIENT_TYPE_TWO = 2;
        //充值类型:存款优惠
        public static final int RECHARGE_TYPE = 2;
        //确认支付者
        public static final String CONFIRM_PAY = "system";

    }
}
