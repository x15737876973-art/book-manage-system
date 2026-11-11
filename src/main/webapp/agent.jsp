<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.agent.AgentConfig" %>
<%@ page import="com.book.entity.User" %>
<%
    /*
     * 智能图书助手页面（轻量 RAG 问答）。
     *
     * 说明：
     *  - 本页面无需登录即可访问（作为公开的服务展示入口）；
     *  - 页面内的所有模型回答均通过 textContent 渲染，不使用 innerHTML，
     *    从根本上规避 XSS；
     *  - API Key 只在服务器端使用，绝不会出现在本页面的 HTML 或 JS 中。
     */
    AgentConfig agentConfig = AgentConfig.getInstance();
    boolean agentReady = agentConfig.isConfigured();
    Object loginUserObj = session.getAttribute("loginUser");
    String displayName = "游客";
    if (loginUserObj instanceof User) {
        User u = (User) loginUserObj;
        displayName = (u.getRealName() != null && !u.getRealName().isEmpty())
                ? u.getRealName() : u.getUsername();
    }
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>智能图书助手 - 图书管理系统</title>
    <link rel="stylesheet" href="<%=ctx%>/css/style.css">
    <link rel="stylesheet" href="<%=ctx%>/css/supplement.css">
    <style>
        * { box-sizing: border-box; }

        .agent-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
            padding: 18px 28px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: 0 2px 12px rgba(0, 0, 0, .12);
        }
        .agent-header h1 { font-size: 20px; margin: 0; font-weight: 600; }
        .agent-header .links { display: flex; gap: 16px; align-items: center; font-size: 14px; }
        .agent-header a { color: #fff; text-decoration: none; opacity: .9; }
        .agent-header a:hover { opacity: 1; text-decoration: underline; }

        .agent-wrap {
            max-width: 900px;
            margin: 24px auto 40px;
            padding: 0 16px;
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .status-bar {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            padding: 10px 14px;
            border-radius: 8px;
            background: #f1f3f9;
            color: #555;
        }
        .status-dot {
            width: 8px; height: 8px; border-radius: 50%;
            background: #ccc; flex: none;
        }
        .status-dot.on { background: #27ae60; }
        .status-dot.off { background: #f39c12; }

        .chat-card {
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 2px 14px rgba(0, 0, 0, .07);
            display: flex;
            flex-direction: column;
            height: 520px;
            overflow: hidden;
        }

        .chat-body {
            flex: 1;
            overflow-y: auto;
            padding: 20px;
            display: flex;
            flex-direction: column;
            gap: 16px;
            scroll-behavior: smooth;
        }

        .msg-row { display: flex; gap: 10px; align-items: flex-start; }
        .msg-row.user { flex-direction: row-reverse; }

        .avatar {
            width: 34px; height: 34px; border-radius: 50%;
            display: flex; align-items: center; justify-content: center;
            font-size: 17px; flex: none;
            background: #eef1fb;
        }
        .msg-row.user .avatar { background: #e8f5e9; }

        .bubble {
            max-width: 74%;
            padding: 11px 15px;
            border-radius: 12px;
            line-height: 1.65;
            font-size: 14.5px;
            white-space: pre-wrap;
            word-break: break-word;
            background: #f7f8fb;
            color: #2c3e50;
            border: 1px solid #eceef5;
        }
        .msg-row.user .bubble {
            background: linear-gradient(135deg, #667eea, #7a6ff0);
            color: #fff;
            border-color: transparent;
        }

        .books-hit { margin-top: 10px; display: flex; flex-direction: column; gap: 8px; }
        .book-pill {
            display: flex; align-items: center; justify-content: space-between;
            gap: 10px; padding: 8px 12px;
            background: #fff; border: 1px solid #e6e9f2;
            border-radius: 8px; font-size: 13px;
        }
        .book-pill .name { font-weight: 600; color: #33405b; }
        .book-pill .meta { color: #7b8494; font-size: 12px; }
        .book-pill .stock-ok { color: #27ae60; font-weight: 600; }
        .book-pill .stock-none { color: #e67e22; font-weight: 600; }

        .note-tip {
            margin-top: 8px; font-size: 12px; color: #a9700c;
            background: #fff8e6; border: 1px solid #ffe3a3;
            padding: 6px 10px; border-radius: 6px;
        }

        .chat-input {
            border-top: 1px solid #eef0f5;
            padding: 14px 16px;
            display: flex;
            gap: 10px;
            background: #fcfcfe;
        }
        .chat-input textarea {
            flex: 1;
            resize: none;
            height: 46px;
            padding: 12px 14px;
            border: 1px solid #dfe3ee;
            border-radius: 10px;
            font-size: 14.5px;
            font-family: inherit;
            outline: none;
            transition: border-color .2s;
        }
        .chat-input textarea:focus { border-color: #667eea; }
        .chat-input button {
            padding: 0 22px;
            height: 46px;
            border: none;
            border-radius: 10px;
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: #fff;
            font-size: 14.5px;
            font-weight: 600;
            cursor: pointer;
            transition: opacity .2s;
        }
        .chat-input button:disabled { opacity: .55; cursor: not-allowed; }

        .quick-asks {
            display: flex; flex-wrap: wrap; gap: 8px;
        }
        .quick-asks button {
            padding: 7px 13px;
            border-radius: 16px;
            border: 1px solid #d9dff0;
            background: #fff;
            color: #5a6683;
            font-size: 13px;
            cursor: pointer;
            transition: all .18s;
        }
        .quick-asks button:hover {
            border-color: #667eea; color: #667eea; background: #f6f7fd;
        }

        .typing { display: inline-flex; gap: 4px; align-items: center; }
        .typing span {
            width: 6px; height: 6px; border-radius: 50%;
            background: #a9b2c9; animation: blink 1.2s infinite;
        }
        .typing span:nth-child(2) { animation-delay: .2s; }
        .typing span:nth-child(3) { animation-delay: .4s; }
        @keyframes blink {
            0%, 60%, 100% { opacity: .25; }
            30% { opacity: 1; }
        }
    </style>
</head>
<body style="margin:0;background:#f4f6fb;">

<div class="agent-header">
    <h1>🤖 智能图书助手</h1>
    <div class="links">
        <span>当前用户：<%=displayName%></span>
        <% if (loginUserObj == null) { %>
            <a href="<%=ctx%>/login.jsp">登录</a>
        <% } else { %>
            <a href="<%=ctx%>/logoutServlet">退出</a>
        <% } %>
        <a href="<%=ctx%>/index.jsp">返回首页</a>
    </div>
</div>

<div class="agent-wrap">

    <!-- 服务状态提示 -->
    <div class="status-bar">
        <% if (agentReady) { %>
            <span class="status-dot on"></span>
            <span>智能问答已就绪（模型：<%=agentConfig.getModel()%>，Key：<%=agentConfig.getMaskedApiKey()%>）</span>
        <% } else { %>
            <span class="status-dot off"></span>
            <span>
                当前为<strong>本地检索模式</strong>：只根据数据库检索结果回答，未调用大模型。
                配置 API Key 后可获得更自然的对话体验。
            </span>
        <% } %>
    </div>

    <!-- 对话区 -->
    <div class="chat-card">
        <div class="chat-body" id="chatBody">
            <div class="msg-row">
                <div class="avatar">🤖</div>
                <div class="bubble">
                    你好，我是智能图书助手 👋

我可以帮你：
· 查询本馆是否有某本书（说出书名或作者即可）
· 按作者 / 出版社 / 类别荐书
· 告诉你某本书的库存和存放位置

注意：我的回答严格基于本馆数据库中的真实馆藏，不会编造不存在的书籍。
                </div>
            </div>
        </div>

        <div class="chat-input">
            <textarea id="questionInput" placeholder="试试问：有《三体》吗？／推荐几本计算机类的书"
                      maxlength="200"></textarea>
            <button id="sendBtn" type="button">发送</button>
        </div>
    </div>

    <!-- 快捷提问 -->
    <div class="quick-asks">
        <button type="button" data-q="本馆有《三体》吗？">有没有《三体》</button>
        <button type="button" data-q="推荐几本计算机类的图书">推荐计算机类图书</button>
        <button type="button" data-q="刘慈欣的书有哪些？">刘慈欣的书</button>
        <button type="button" data-q="有哪些书现在可以借？">现在能借的书</button>
        <button type="button" data-q="有没有关于历史的书？">历史类图书</button>
    </div>
</div>

<script>
(function () {
    var ctx = '<%=ctx%>';
    var chatBody = document.getElementById('chatBody');
    var input = document.getElementById('questionInput');
    var sendBtn = document.getElementById('sendBtn');
    var sending = false;

    /** 安全地创建元素并设置文本（不拼接 innerHTML，杜绝 XSS） */
    function el(tag, className, text) {
        var node = document.createElement(tag);
        if (className) { node.className = className; }
        if (text !== undefined && text !== null) { node.textContent = text; }
        return node;
    }

    /** 滚动到底部 */
    function scrollToBottom() {
        chatBody.scrollTop = chatBody.scrollHeight;
    }

    /** 追加用户消息 */
    function appendUser(text) {
        var row = el('div', 'msg-row user');
        row.appendChild(el('div', 'avatar', '👤'));
        row.appendChild(el('div', 'bubble', text));
        chatBody.appendChild(row);
        scrollToBottom();
    }

    /** 追加"正在思考"占位气泡，返回可替换的节点 */
    function appendTyping() {
        var row = el('div', 'msg-row');
        var avatar = el('div', 'avatar', '🤖');
        var bubble = el('div', 'bubble');
        var typing = el('span', 'typing');
        typing.appendChild(el('span'));
        typing.appendChild(el('span'));
        typing.appendChild(el('span'));
        bubble.appendChild(typing);
        row.appendChild(avatar);
        row.appendChild(bubble);
        chatBody.appendChild(row);
        scrollToBottom();
        return row;
    }

    /** 追加助手回答（文本 + 命中的图书卡片 + 降级提示） */
    function appendAnswer(data) {
        var row = el('div', 'msg-row');
        row.appendChild(el('div', 'avatar', '🤖'));

        var bubble = el('div', 'bubble');
        // 关键：用 textContent 渲染模型返回的纯文本
        bubble.appendChild(document.createTextNode(data.answer || ''));

        if (data.books && data.books.length > 0) {
            var list = el('div', 'books-hit');
            for (var i = 0; i < data.books.length && i < 5; i++) {
                var b = data.books[i];
                var pill = el('div', 'book-pill');
                var left = el('div');
                left.appendChild(el('div', 'name', '《' + (b.bname || '') + '》'));
                left.appendChild(el('div', 'meta',
                    (b.author || '未知作者') + ' · ' + (b.bid || '') +
                    (b.location ? ' · ' + b.location : '')));
                pill.appendChild(left);
                var avail = b.available || 0;
                pill.appendChild(el('span', avail > 0 ? 'stock-ok' : 'stock-none',
                    avail > 0 ? ('可借 ' + avail + ' 本') : '已借完'));
                list.appendChild(pill);
            }
            bubble.appendChild(list);
        }

        if (data.note) {
            bubble.appendChild(el('div', 'note-tip', 'ℹ️ ' + data.note));
        }

        row.appendChild(bubble);
        chatBody.appendChild(row);
        scrollToBottom();
    }

    /** 追加错误提示 */
    function appendError(text) {
        var row = el('div', 'msg-row');
        row.appendChild(el('div', 'avatar', '🤖'));
        var bubble = el('div', 'bubble', '⚠️ ' + text);
        bubble.style.background = '#fff4f4';
        bubble.style.borderColor = '#ffd6d6';
        bubble.style.color = '#c0392b';
        row.appendChild(bubble);
        chatBody.appendChild(row);
        scrollToBottom();
    }

    /** 发送问题 */
    function send(question) {
        question = (question || '').trim();
        if (!question || sending) { return; }

        sending = true;
        sendBtn.disabled = true;
        appendUser(question);
        input.value = '';

        var typingRow = appendTyping();

        var xhr = new XMLHttpRequest();
        xhr.open('POST', ctx + '/agentQueryServlet', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
        xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

        xhr.onreadystatechange = function () {
            if (xhr.readyState !== 4) { return; }

            // 移除"正在思考"
            if (typingRow && typingRow.parentNode) {
                typingRow.parentNode.removeChild(typingRow);
            }

            sending = false;
            sendBtn.disabled = false;

            if (xhr.status === 200) {
                try {
                    var data = JSON.parse(xhr.responseText);
                    if (data.success) {
                        appendAnswer(data);
                    } else {
                        appendError(data.answer || '请求失败');
                    }
                } catch (e) {
                    appendError('返回内容解析失败：' + e.message);
                }
            } else if (xhr.status === 401) {
                appendError('登录状态已失效，请刷新页面后重新登录');
            } else {
                appendError('请求失败（HTTP ' + xhr.status + '），请稍后重试');
            }
            input.focus();
        };

        xhr.onerror = function () {
            if (typingRow && typingRow.parentNode) {
                typingRow.parentNode.removeChild(typingRow);
            }
            sending = false;
            sendBtn.disabled = false;
            appendError('网络异常，请检查网络后重试');
        };

        xhr.send('question=' + encodeURIComponent(question));
    }

    // ---------- 事件绑定 ----------
    sendBtn.addEventListener('click', function () { send(input.value); });

    input.addEventListener('keydown', function (e) {
        // Enter 发送，Shift+Enter 换行
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            send(input.value);
        }
    });

    var quickBtns = document.querySelectorAll('.quick-asks button');
    for (var i = 0; i < quickBtns.length; i++) {
        quickBtns[i].addEventListener('click', function () {
            send(this.getAttribute('data-q'));
        });
    }

    input.focus();
})();
</script>
</body>
</html>