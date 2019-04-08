package com.bc.common.constant;

/**
 * Created by mrt on 2019/4/6 0006 下午 6:39
 */
public class VarParam {
    //逻辑常量是 与 否
    public static final int YES = 1;
    public static final int NO = 0;

    /**
     * 登录
     */
    public static class Login{
        //项目的redis前缀
        public static final String LOGIN = "LOGIN:";
        //用户的登录标志（redis的key）
        public static final String LOGIN_FLAG = LOGIN + "LOGIN_FLAG:";
        //图片验证码的sessionKey
        public static final String SESSION_KEY_VALIDATE_IMAGE = "SESSION_KEY_VALIDATE_IMAGE";
    }

    /**
     * 红包管理后台
     */
    public static class RedPacketM {
        //红包模式1：每天重新计算
        public static final int ACTIVEMODEL_ONE = 1;
        //红包模式2：活动时间累计
        public static final int ACTIVEMODEL_TWO = 2;



    }

}
