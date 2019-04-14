package com.bc.utils;

import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * 
 * 结果封装类 封装响应的头部信息、状态信息、Cookie信息、返回内容
 * 
 * @author lishangzhi 
 *           E-mail:1669852599@qq.com
 * @version v1.0
 *           Time：2015年8月1日 上午9:45:33
 */
@ToString
public class MyHttpResult {
    /**
     * Logger for this class
     */

    private int statusCode;
    private HashMap<String, Header> headerAll;
    private HttpEntity httpEntity;
    private String otherContent;



    /**
     * 获取结果状态码
     * 
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 设置结果状态码
     * 
     * @param statusCode
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * 获取结果头部信息
     * 
     * @return
     */
    public HashMap<String, Header> getHeaders() {
        return headerAll;
    }

    /**
     * 设置结果头部信息
     * 
     * @param headers
     */
    public void setHeaders(Header[] headers) {
        headerAll = new HashMap<String, Header>();
        for (Header header : headers) {
            headerAll.put(header.getName(), header);
        }
    }

    /**
     * 获取响应结果
     * 
     * @return
     */
    public HttpEntity getHttpEntity() {
        return httpEntity;
    }

    /**
     * 设置响应结果
     * 
     * @param httpEntity
     */
    public void setHttpEntity(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
    }
    /**
     * 获取响应json
     */
    public String getResultInfo() throws Exception{
        return IOUtils.toString(this.getHttpEntity().getContent(), "utf-8");
    }

    /**
     * 将服务器返回的结果HttpEntity流转换成String格式的内容
     * 
     * @param encoding
     *            指定的转换编码
     * @return
     */
    public String getHtmlContent(String encoding) {
        // HTML内容
        if (httpEntity != null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            InputStream is = null;
            try {
                if (httpEntity.getContentEncoding() != null
                        && httpEntity.getContentEncoding().getValue().indexOf("gzip") != -1) {
                    // GZIP格式的流解压
                    is = new GZIPInputStream(new BufferedInputStream(httpEntity.getContent()));
                } else {
                    is = new BufferedInputStream(httpEntity.getContent());
                }
                String responseContent = "";
                if (is != null) {
                    byte[] buffer = new byte[1024];
                    int n;
                    while ((n = is.read(buffer)) >= 0) {
                        output.write(buffer, 0, n);
                    }
                    responseContent = output.toString(encoding);
                    // responseContent=new
                    // String(responseContent.getBytes("utf-8"),"gbk");
                }
                return responseContent;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return "";
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * 获取请求中的内容
     */
    public String getHtml(MyHttpResult result, String chart) {

        HttpEntity entity = result.getHttpEntity();
        String resultStr = "";
        try {
            resultStr = EntityUtils.toString(entity, chart);
        } catch (Exception e) {

            // e.printStackTrace();
        } finally {
            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {

                // e.printStackTrace();
            }
        }

        return resultStr;
    }

    /**
     * 关闭HttpEntity流
     */
    public void consume(MyHttpResult result) {
        try {
            HttpEntity entity = result.getHttpEntity();
            // EntityUtils.consume(entity);
            if (entity.isStreaming()) {
                InputStream instream = entity.getContent();
                if (instream != null) {
                    instream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOtherContent() {
        return otherContent;
    }

    public void setOtherContent(String otherContent) {
        this.otherContent = otherContent;
    }
}