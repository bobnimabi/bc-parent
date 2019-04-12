package com.bc.service.redPacket.exception;

import com.bc.common.response.ResultCode;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

@ToString
public enum RedCode implements ResultCode {
    TIMES_NOT_ENOUGH(false,26000,"请输入账号！"),
    LOTTERY_EXCEPTION(false,26001,"抽红包系统故障"),

    ;



    //操作代码
    @ApiModelProperty(value = "操作是否成功", example = "true", required = true)
    boolean success;

    //操作代码
    @ApiModelProperty(value = "操作代码", example = "22001", required = true)
    int code;
    //提示信息
    @ApiModelProperty(value = "操作提示", example = "操作过于频繁！", required = true)
    String message;
    private RedCode(boolean success, int code, String message){
        this.success = success;
        this.code = code;
        this.message = message;
    }
    private static final ImmutableMap<Integer, RedCode> CACHE;

    static {
        final ImmutableMap.Builder<Integer, RedCode> builder = ImmutableMap.builder();
        for (RedCode commonCode : values()) {
            builder.put(commonCode.code(), commonCode);
        }
        CACHE = builder.build();
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public void setMes(String mes) {
        this.message = mes;
    }
}
