package com.daily.dailychineseculture.common;

import lombok.Data;

/**
 * 统一响应结果类
 * @param <T> 数据泛型
 */
@Data
public class Result<T> {
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 私有构造函数
     */
    private Result() {}
    
    /**
     * 私有构造函数
     */
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    /**
     * 成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    
    /**
     * 成功响应带消息
     * @param msg 响应消息
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> successMsg(String msg) {
        return new Result<>(200, msg, null);
    }
    
    /**
     * 错误响应
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
    
    /**
     * 自定义状态码的响应
     * @param code 状态码
     * @param msg 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> build(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
}