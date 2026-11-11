package com.book.agent;

import com.book.entity.Book;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * DeepSeek 大模型客户端（纯 JDK 实现，无需额外 HTTP 依赖）。
 *
 * <h3>接口说明</h3>
 * <p>DeepSeek 的开放 API 完全兼容 OpenAI 的 {@code /chat/completions} 协议：</p>
 * <pre>
 *   POST https://api.deepseek.com/chat/completions
 *   Authorization: Bearer sk-xxxxxxxx
 *   Content-Type: application/json
 *
 *   {
 *     "model": "deepseek-chat",
 *     "messages": [
 *       {"role": "system", "content": "..."},
 *       {"role": "user",   "content": "..."}
 *     ],
 *     "temperature": 0.3,
 *     "stream": false
 *   }
 * </pre>
 *
 * <h3>返回示例</h3>
 * <pre>
 *   {
 *     "choices": [
 *       {"message": {"role": "assistant", "content": "根据馆藏资料……"}}
 *     ],
 *     "usage": {"prompt_tokens": 520, "completion_tokens": 130, "total_tokens": 650}
 *   }
 * </pre>
 *
 * <h3>设计取舍</h3>
 * <ul>
 *   <li><b>温度取 0.3</b>：图书查询属于事实型问答，低温度能让输出更稳定、更少发散；</li>
 *   <li><b>不开启流式</b>：实现简单，适合课堂/课程设计场景；如需打字机效果可改
 *       {@code stream=true} 并逐行解析 SSE；</li>
 *   <li><b>超时与错误兜底</b>：任何网络或接口异常都转成 {@link AgentException}，
 *       由上层决定降级为本地检索结果，不让页面报 500。</li>
 * </ul>
 */
public class DeepSeekClient {

    private final AgentConfig config;

    public DeepSeekClient() {
        this(AgentConfig.getInstance());
    }

    public DeepSeekClient(AgentConfig config) {
        this.config = config;
    }

    /** 是否已配置可用的 API Key */
    public boolean isAvailable() {
        return config.isConfigured();
    }

    /**
     * 调用 DeepSeek 完成一次问答。
     *
     * @param systemPrompt 系统指令（角色与规则约束）
     * @param userPrompt   用户内容（含检索到的图书事实）
     * @return 模型回复文本
     * @throws AgentException 未配置 Key、网络异常、接口返回错误、解析失败等情况
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            throw new AgentException("未配置大模型 API Key，无法调用大模型。"
                    + "请在 agent.properties 或环境变量 DEEPSEEK_API_KEY 中配置后重试。");
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(config.getChatCompletionsUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(config.getTimeoutSeconds() * 1000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            conn.setRequestProperty("Accept", "application/json");

            String body = buildRequestBody(systemPrompt, userPrompt);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            String responseText = readBody(status >= 400 ? conn.getErrorStream() : conn.getInputStream());

            if (status != 200) {
                throw new AgentException("大模型接口返回错误（HTTP " + status + "）："
                        + friendlyApiError(status, responseText));
            }
            return parseContent(responseText);

        } catch (AgentException e) {
            throw e;
        } catch (java.net.SocketTimeoutException e) {
            throw new AgentException("调用大模型超时，请稍后重试");
        } catch (java.net.UnknownHostException e) {
            throw new AgentException("无法连接大模型服务，请检查网络或代理设置，以及 agent.base.url 是否正确");
        } catch (Exception e) {
            throw new AgentException("调用大模型失败：" + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // ======================================================================
    // 便捷方法：直接基于图书列表提问
    // ======================================================================

    /**
     * 一步完成问答：自动构造 Prompt 并调用模型。
     *
     * @param books    检索到的图书（可为空）
     * @param question 用户问题
     */
    public String askWithBooks(List<Book> books, String question) {
        return chat(PromptBuilder.systemPrompt(), PromptBuilder.userPrompt(books, question));
    }

    // ======================================================================
    // 内部实现
    // ======================================================================

    /** 手工拼 JSON（避免引入 Jackson 等额外依赖；内容会做完整转义） */
    private String buildRequestBody(String systemPrompt, String userPrompt) {
        return "{"
                + "\"model\":" + jsonString(config.getModel()) + ","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":" + jsonString(systemPrompt) + "},"
                + "{\"role\":\"user\",\"content\":" + jsonString(userPrompt) + "}"
                + "],"
                + "\"temperature\":0.3,"
                + "\"max_tokens\":1024,"
                + "\"stream\":false"
                + "}";
    }

    /** JSON 字符串转义（含控制字符），防止 Prompt 中的引号破坏请求体 */
    private static String jsonString(String raw) {
        if (raw == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder(raw.length() + 32);
        sb.append('"');
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private String readBody(InputStream in) throws Exception {
        if (in == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 从返回 JSON 中提取 choices[0].message.content。
     *
     * <p>不引入 JSON 库，用轻量解析：定位 {@code "content":} 后取出字符串，
     * 并处理转义。DeepSeek 的返回结构固定，这种方式足够可靠。</p>
     */
    private String parseContent(String json) {
        if (json == null || json.isEmpty()) {
            throw new AgentException("大模型返回内容为空");
        }
        int idx = json.indexOf("\"content\"");
        if (idx < 0) {
            // 可能是错误响应
            if (json.contains("\"error\"")) {
                throw new AgentException("大模型返回错误：" + extractSimple(json, "\"message\""));
            }
            throw new AgentException("无法解析大模型返回内容");
        }
        // 跳过 "content" 与其后的冒号和空白
        int colon = json.indexOf(':', idx + 9);
        if (colon < 0) {
            throw new AgentException("无法解析大模型返回内容");
        }
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        if (start >= json.length() || json.charAt(start) != '"') {
            throw new AgentException("无法解析大模型返回内容");
        }
        return unescapeJson(json, start + 1);
    }

    /** 从指定位置开始解析 JSON 字符串，直到未转义的引号 */
    private String unescapeJson(String json, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                break;
            }
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(++i);
                switch (next) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'u':
                        if (i + 4 < json.length()) {
                            try {
                                sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append("\\u");
                            }
                        }
                        break;
                    default:
                        sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    private String extractSimple(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) {
            return "";
        }
        int colon = json.indexOf(':', idx + key.length());
        if (colon < 0) {
            return "";
        }
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        if (start < json.length() && json.charAt(start) == '"') {
            return unescapeJson(json, start + 1);
        }
        return "";
    }

    /** 把常见状态码翻译成用户能看懂的话 */
    private String friendlyApiError(int status, String body) {
        switch (status) {
            case 401:
                return "API Key 无效或已过期，请到对应平台重新生成";
            case 402:
                return "账户余额不足，请到 DeepSeek 平台充值后再试";
            case 429:
                return "请求过于频繁，请稍等片刻再试";
            case 500:
            case 502:
            case 503:
                return "大模型服务暂时不可用，请稍后重试";
            default:
                String detail = extractSimple(body, "\"message\"");
                return detail.isEmpty() ? "请检查配置后重试" : detail;
        }
    }

    /**
     * 智能问答模块的业务异常。
     */
    public static class AgentException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public AgentException(String message) {
            super(message);
        }

        public AgentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}