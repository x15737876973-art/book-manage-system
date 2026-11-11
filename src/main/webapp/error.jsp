<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="false" %>
<%
    /*
     * 统一错误页。
     *
     * 设计要点：
     *  1. 只展示"对用户有意义"的提示，绝不输出 exception.getStackTrace()；
     *  2. 具体的技术细节写入服务器日志，便于管理员排查；
     *  3. 根据状态码给出不同的图标与文案；
     *  4. 所有动态内容都经过转义，避免把异常信息当 HTML 执行。
     */
    response.setStatus(200);

    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
    Throwable errorException = (Throwable) request.getAttribute("javax.servlet.error.exception");
    String friendlyError = (String) request.getAttribute("error");   // Servlet 主动设置的中文提示

    // 记录到服务器日志（不展示给用户）
    if (errorException != null) {
        getServletContext().log("页面异常: " + requestUri, errorException);
    }

    String title;
    String icon;
    String description;
    String hint;

    if (friendlyError != null && !friendlyError.isEmpty()) {
        title = "操作未完成";
        icon = "⚠️";
        description = friendlyError;
        hint = "请根据提示修改后重试；如果反复出现，请联系系统管理员。";
    } else if (statusCode != null && statusCode == 404) {
        title = "页面不存在";
        icon = "🔍";
        description = "您访问的页面或资源不存在，可能已经被删除或地址输入有误。";
        hint = "请检查地址是否正确，或返回首页重新进入。";
    } else if (statusCode != null && statusCode == 403) {
        title = "无访问权限";
        icon = "🔒";
        description = "您当前的身份无权访问该页面。";
        hint = "请确认已使用正确的账号身份登录（管理员 / 读者）。";
    } else if (statusCode != null && statusCode == 400) {
        title = "请求参数有误";
        icon = "❓";
        description = "提交的数据格式不正确，服务器无法处理。";
        hint = "请检查填写内容后重新提交。";
    } else if (statusCode != null && (statusCode == 500 || statusCode == 502 || statusCode == 503)) {
        title = "服务器开小差了";
        icon = "🛠️";
        description = "服务端处理请求时出现异常。";
        hint = "请稍后重试；若持续出现，请联系管理员检查数据库与服务状态。";
    } else {
        title = "操作失败";
        icon = "⚠️";
        description = "处理您的请求时遇到了问题。";
        hint = "请稍后重试，或联系系统管理员。";
    }

    // 简单转义，避免把异常文本当 HTML 渲染
    java.util.function.Function<String, String> esc = s -> {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    };

    String ctx = request.getContextPath();
    Object loginUser = session.getAttribute("loginUser");
    String role = (String) session.getAttribute("loginRole");
    String homeUrl = ctx + "/index.jsp";
    if ("admin".equals(role)) {
        homeUrl = ctx + "/admin/indexServlet";
    } else if ("reader".equals(role)) {
        homeUrl = ctx + "/reader/indexServlet";
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=esc.apply(title)%> - 图书管理系统</title>
    <style>
        * { box-sizing: border-box; }
        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #eef1fb 0%, #f7f4fb 100%);
            font-family: system-ui, -apple-system, "Microsoft YaHei", sans-serif;
            padding: 24px;
        }
        .card {
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 8px 32px rgba(76, 90, 160, .12);
            padding: 44px 40px;
            max-width: 560px;
            width: 100%;
            text-align: center;
        }
        .icon {
            font-size: 56px;
            line-height: 1;
            margin-bottom: 18px;
        }
        h1 {
            font-size: 22px;
            color: #2c3e50;
            margin: 0 0 14px;
            font-weight: 600;
        }
        .desc {
            color: #4a5568;
            font-size: 15px;
            line-height: 1.75;
            background: #f7f8fc;
            border-radius: 10px;
            padding: 14px 18px;
            margin: 0 0 16px;
            word-break: break-word;
            text-align: left;
        }
        .hint {
            color: #8a93a6;
            font-size: 13.5px;
            line-height: 1.7;
            margin-bottom: 26px;
        }
        .actions {
            display: flex;
            gap: 12px;
            justify-content: center;
            flex-wrap: wrap;
        }
        .btn {
            display: inline-block;
            padding: 11px 24px;
            border-radius: 9px;
            text-decoration: none;
            font-size: 14.5px;
            font-weight: 500;
            transition: all .2s;
            border: 1px solid transparent;
        }
        .btn-primary {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: #fff;
        }
        .btn-primary:hover { transform: translateY(-1px); box-shadow: 0 6px 18px rgba(102, 126, 234, .35); }
        .btn-ghost {
            background: #fff;
            color: #5a6683;
            border-color: #dde2ef;
        }
        .btn-ghost:hover { border-color: #667eea; color: #667eea; }
        .code-tip {
            margin-top: 22px;
            font-size: 12px;
            color: #b6bdcb;
        }
    </style>
</head>
<body>
<div class="card">
    <div class="icon"><%=icon%></div>
    <h1><%=esc.apply(title)%></h1>

    <div class="desc"><%=esc.apply(description)%></div>
    <div class="hint"><%=esc.apply(hint)%></div>

    <div class="actions">
        <a class="btn btn-primary" href="<%=homeUrl%>">🏠 返回首页</a>
        <% if (loginUser == null) { %>
            <a class="btn btn-ghost" href="<%=ctx%>/login.jsp">🔐 去登录</a>
        <% } else { %>
            <a class="btn btn-ghost" href="<%=ctx%>/logoutServlet">退出登录</a>
        <% } %>
        <a class="btn btn-ghost" href="javascript:history.back()">← 返回上一页</a>
    </div>

    <% if (statusCode != null) { %>
        <div class="code-tip">错误码：<%=statusCode%></div>
    <% } %>
</div>
</body>
</html>