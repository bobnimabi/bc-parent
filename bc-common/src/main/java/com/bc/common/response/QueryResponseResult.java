package com.bc.common.response;

import lombok.Data;
import lombok.ToString;
//接口响应返回状态吗+数据列表
@Data
@ToString
public class QueryResponseResult<T> extends ResponseResult {

    QueryResult<T> queryResult;

    public QueryResponseResult(ResultCode resultCode,QueryResult queryResult){
        super(resultCode);
       this.queryResult = queryResult;
    }

}
