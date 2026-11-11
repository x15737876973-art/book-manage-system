package com.book.util;

/**
 * 业务异常：用于表示"可以展示给用户"的预期内错误。
 *
 * <p>与程序 Bug（空指针、SQL 语法错误等）区分开：</p>
 * <ul>
 *   <li>{@code AppException} → 展示友好的中文提示，不打印堆栈；</li>
 *   <li>其他异常 → 统一交给错误页，仅展示通用信息，堆栈只写日志。</li>
 * </ul>
 *
 * <p>在 Servlet 中可以这样使用：</p>
 * <pre>
 *   if (book == null) {
 *       throw new AppException("图书不存在或已被删除");
 *   }
 * </pre>
 */
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 面向用户的提示信息 */
    private final String userMessage;

    public AppException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    public AppException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage != null ? userMessage : "操作失败，请稍后重试";
    }
}