package com.daily.dailychineseculture.common;

import lombok.Data;

/**
 * 统一响应结果封装类
 */
@Data
public class ResponseResult<T> {
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public ResponseResult() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ResponseResult(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    public ResponseResult(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }
    
    /**
     * 成功响应
     */
    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(200, "操作成功");
    }
    
    /**
     * 成功响应带数据
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, "操作成功", data);
    }

    /**
     * 成功响应带消息
     */
    public static <T> ResponseResult<T> successMsg(String message) {
        return new ResponseResult<>(200, message, null);
    }

    /**
     * 成功响应带消息和数据
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(200, message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> ResponseResult<T> error(String message) {
        return new ResponseResult<>(500, message);
    }
    
    /**
     * 失败响应带状态码
     */
    public static <T> ResponseResult<T> error(Integer code, String message) {
        return new ResponseResult<>(code, message);
    }
}