package com.daily.dailychineseculture.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseResult<String> handleBusinessException(BusinessException e) {
        return ResponseResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return ResponseResult.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    /**
     * 处理数据库唯一索引冲突异常
     */
    public ResponseResult<String> handleDuplicateKeyException(DuplicateKeyException e) {
        return ResponseResult.error(409, "该营期的当前天数或日期已被占用，请刷新页面获取最新排课进度！");
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseResult.error("系统内部错误: " + e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<String> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        return ResponseResult.error("运行时错误: " + e.getMessage());
    }
}