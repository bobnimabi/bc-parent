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
        public static final String REDPACKET_M_PRE = "RedPackM:";
        //红包活动redis-key
        public static final String ACTIVE_KEY = REDPACKET_M_PRE + "Active:";
        //奖品
        public static final String PRIZE_KEY = REDPACKET_M_PRE + "Prize";
        //转换规则
        public static final String TRANSFORM_KEY = REDPACKET_M_PRE + "Transform";
        //默认主题
        public static final String DEFAULT_THEME = REDPACKET_M_PRE + "DefaultTheme";

        /**
         * 项目参数
         */
        //红包活动的id
        public static final int AWARD_ACTIVE_ID = 1;
        //库存数量的最小值
        public static final int PRIZE_STORE_NUM_MIN = 0;

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
        //支付状态：已派送
        public static final int PAY_STATUS_TWO = 2;
        //客户端类型：1pc
        public static final int CLIENT_TYPE_ONE = 1;
        //客户端类型：2mobile
        public static final int CLIENT_TYPE_TWO = 2;
    }
}
