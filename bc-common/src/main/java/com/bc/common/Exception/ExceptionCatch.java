package com.bc.common.Exception;

import com.bc.common.response.CommonCode;
import com.bc.common.response.ResponseResult;
import com.bc.common.response.ResultCode;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.access.AccessDeniedException;

/**
 * 统一异常捕获类
 * @author Administrator
 * @version 1.0
 * @create 2018-09-14 17:32
 **/
@ControllerAdvice//控制器增强
@Slf4j
public class ExceptionCatch {


    //定义map，配置异常类型所对应的错误代码
    private static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;
    //定义map的builder对象，去构建ImmutableMap
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    /**
     * 自定义异常：捕获CustomException此类异常
     */

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException customException){
        //记录日志
        ResultCode resultCode = customException.getResultCode();
        log.error("错误信息："+resultCode.code()+":"+resultCode.message());
        log.error("错误信息：",customException);
        return new ResponseResult(resultCode);
    }

    /**
     * springsecurity : 方法权限异常
     */

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseResult accessDeniedException(AccessDeniedException exception){
        //记录日志
        CommonCode failCode = CommonCode.FAIL;
        failCode.setMes("权限不足");
        log.error("错误信息：", exception);
        return new ResponseResult(failCode);
    }

    /**
     * hibernate Validate：处理所有接口数据参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseResult handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        //记录日志
        CommonCode invalidParamCode = CommonCode.INVALID_PARAM;
        invalidParamCode.setMes(e.getMessage());
        log.error("错误信息：", e);
        return new ResponseResult(invalidParamCode);
    }

    //捕获Exception此类异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception exception){
        exception.printStackTrace();
        //记录日志
        log.error("错误信息：", exception);
        if(EXCEPTIONS == null){
            EXCEPTIONS = builder.build();//EXCEPTIONS构建成功
        }
        //从EXCEPTIONS中找异常类型所对应的错误代码，如果找到了将错误代码响应给用户，如果找不到给用户响应99999异常
        ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        if(resultCode !=null){
            return new ResponseResult(resultCode);
        }else{
            //返回99999异常
            CommonCode serverError = CommonCode.SERVER_ERROR;
            serverError.setMes(exception.getMessage());
            return new ResponseResult(serverError);
        }
    }

    static {
        //定义异常类型所对应的错误代码
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
    }
}
