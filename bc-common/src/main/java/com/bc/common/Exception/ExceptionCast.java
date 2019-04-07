package com.bc.common.Exception;


import com.bc.common.response.CommonCode;
import com.bc.common.response.ResultCode;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-09-14 17:31
 **/
public class ExceptionCast {

    public static void cast(ResultCode resultCode){
        throw new CustomException(resultCode);
    }

    public static void castFail(String mes){
        CommonCode fail = CommonCode.FAIL;
        fail.setMes(mes);
        throw new CustomException(fail);
    }

}
