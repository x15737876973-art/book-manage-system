package com.book.admin.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <h3>解决的问题</h3>
 * <p>没有它时，任何未捕获异常都会被 Spring 转成 500 错误页，
 * 前端拿到无法解析的 HTML，用户看到"服务器错误"。</p>
 *
 * <p>{@code @RestControllerAdvice} 拦截所有 Controller 抛出的异常，
 * 统一转换成 {@link Result} 结构返回。</p>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li><b>业务异常</b>：返回友好中文提示（预期内的情况）；</li>
 *   <li><b>参数校验异常</b>：把字段错误拼成可读文案；</li>
 *   <li><b>未知异常</b>：堆栈只写日志，对外只返回通用文案，
 *       避免泄露 SQL、类名等技术细节。</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：调用方参数不合法或业务规则不满足 */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Result<Void> handleBusiness(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /** 请求体 JSON 校验失败（配合 @Valid 使用） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败: {}", message);
        return Result.fail(400, message.isEmpty() ? "参数校验失败" : message);
    }

    /** 表单绑定失败 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.fail(400, message.isEmpty() ? "参数格式错误" : message);
    }

    /** 兜底：任何未预期异常，堆栈只写日志 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}