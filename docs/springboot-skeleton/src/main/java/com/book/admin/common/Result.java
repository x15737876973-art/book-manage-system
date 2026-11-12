package com.book.admin.common;

import java.io.Serializable;

/**
 * 统一响应结构。
 *
 * <h3>为什么需要它</h3>
 * <p>如果每个接口直接返回业务数据，前端要为每种返回写特殊处理；
 * 异常时返回的又是 Spring 默认错误 JSON，格式不统一。</p>
 *
 * <p>包装成 {@code {code, message, data}} 后：</p>
 * <ul>
 *   <li>前端只需判断 {@code code == 200} 区分成功失败；</li>
 *   <li>错误信息统一在 {@code message}；</li>
 *   <li>真实数据在 {@code data}，结构稳定。</li>
 * </ul>
 *
 * <pre>
 * 成功：{"code":200,"message":"成功","data":[...]}
 * 失败：{"code":400,"message":"书名不能为空","data":null}
 * </pre>
 *
 * @param <T> 业务数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功（带数据） */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "成功", data);
    }

    /** 成功（无数据） */
    public static <T> Result<T> success() {
        return new Result<>(200, "成功", null);
    }

    /** 失败（默认 500） */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /** 失败（自定义状态码） */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}