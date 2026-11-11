package com.book.agent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 智能问答（RAG）模块配置读取器。
 *
 * ==========================================================================
 * 【如何申请 DeepSeek API Key】
 * ==========================================================================
 * 1. 打开 DeepSeek 开放平台：https://platform.deepseek.com
 * 2. 注册 / 登录账号（可用手机号或邮箱）。
 * 3. 左侧菜单进入「API keys」页面，点击「创建 API key」。
 * 4. 给 Key 起个名字（例如 book-manage-system），点击创建。
 * 5. <b>立刻复制</b>弹出的形如 {@code sk-xxxxxxxxxxxxxxxx} 的字符串——
 *    它只显示一次，关闭窗口后无法再次查看，只能重新创建。
 * 6. 该 Key 就是本模块要用的凭证，请勿提交到 Git 仓库、勿写进 README、
 *    勿截图发到网上。
 *
 * ==========================================================================
 * 【如何配置到本项目】
 * ==========================================================================
 * <b>方式一（推荐）：环境变量</b>
 * <pre>
 *   Windows（系统属性 → 环境变量 → 新建用户变量）：
 *      变量名: DEEPSEEK_API_KEY
 *      变量值: sk-xxxxxxxxxxxxxxxx
 *   配置后需重启 IDEA / Tomcat 才会生效。
 * </pre>
 *
 * <b>方式二：配置文件</b>
 * <ol>
 *   <li>把 {@code src/main/resources/agent.properties.example} 复制为同目录下的
 *       {@code agent.properties}；</li>
 *   <li>把其中的 {@code agent.api.key=} 改成你的 Key；</li>
 *   <li>{@code agent.properties} 已被 {@code .gitignore} 忽略，不会进入仓库；</li>
 *   <li>修改后需重新编译（重新部署 war / 重启 Tomcat）。</li>
 * </ol>
 *
 * <b>读取优先级</b>：环境变量 {@code DEEPSEEK_API_KEY}
 * &gt; 系统属性 {@code -Ddeepseek.api.key=xxx}
 * &gt; {@code agent.properties}。
 *
 * ==========================================================================
 * 【不配置 Key 会怎样】
 * ==========================================================================
 * 页面仍然可用，但智能问答会退化为<b>本地检索模式</b>：
 * 只根据数据库检索到的图书列表做规则性回答，不调用大模型。
 * 这样即使没有 Key 也能演示与调试，不会报错。
 *
 * @see com.book.agent.DeepSeekClient
 */
public final class AgentConfig {

    private static final String CONFIG_FILE = "agent.properties";

    /** 环境变量名 */
    public static final String ENV_API_KEY = "DEEPSEEK_API_KEY";
    /** 系统属性名 */
    public static final String SYS_API_KEY = "deepseek.api.key";

    /**
     * 默认 API 基础地址。
     *
     * <p>本模块按 <b>OpenAI 兼容协议</b>（{@code POST /chat/completions}）调用，
     * 因此任何提供该协议的服务都能直接使用，只需改 {@code agent.base.url}：</p>
     * <ul>
     *   <li>DeepSeek：{@code https://api.deepseek.com}</li>
     *   <li>千问AI平台（maas.qianwenai.com）：{@code https://maas.qianwenai.com/compatible-mode/v1}</li>
     *   <li>本地 Ollama：{@code http://localhost:11434/v1}</li>
     *   <li>vLLM / One-API 等自建网关：填其 OpenAI 兼容入口即可</li>
     * </ul>
     *
     * <p>程序会自动处理结尾斜杠，并在缺少 {@code /chat/completions} 时补齐。</p>
     */
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    /** 默认对话模型：deepseek-chat 便宜快速；deepseek-reasoner 推理更强但更慢更贵 */
    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;
    /** 从数据库检索的图书条数上限 */
    private static final int DEFAULT_TOP_K = 8;

    private static volatile AgentConfig instance;

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int timeoutSeconds;
    private final int topK;
    private final String configError;

    private AgentConfig(Properties props) {
        this.apiKey = resolveApiKey(props);
        this.baseUrl = getString(props, "agent.base.url", DEFAULT_BASE_URL);
        this.model = getString(props, "agent.model", DEFAULT_MODEL);
        this.timeoutSeconds = getInt(props, "agent.timeout.seconds", DEFAULT_TIMEOUT_SECONDS);
        this.topK = getInt(props, "agent.top.k", DEFAULT_TOP_K);
        this.configError = null;
    }

    private AgentConfig(String error) {
        this.apiKey = null;
        this.baseUrl = DEFAULT_BASE_URL;
        this.model = DEFAULT_MODEL;
        this.timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        this.topK = DEFAULT_TOP_K;
        this.configError = error;
    }

    /** 获取单例（类加载时读取一次配置） */
    public static AgentConfig getInstance() {
        if (instance == null) {
            synchronized (AgentConfig.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    /** 强制重新加载（修改配置后便于调试） */
    public static void reload() {
        synchronized (AgentConfig.class) {
            instance = null;
        }
        getInstance();
    }

    private static AgentConfig load() {
        try {
            Properties props = new Properties();
            ClassLoader cl = AgentConfig.class.getClassLoader();
            try (InputStream in = cl.getResourceAsStream(CONFIG_FILE)) {
                if (in != null) {
                    props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            }
            return new AgentConfig(props);
        } catch (Exception e) {
            return new AgentConfig("读取 " + CONFIG_FILE + " 失败：" + e.getMessage());
        }
    }

    /**
     * 按优先级解析 API Key。
     *
     * <p>只做<b>宽松的格式预检</b>，目的是拦住明显的占位值（模板里的说明文字、
     * IDE 自动补全产生的伪值），而不是限制具体平台。不同平台的 Key 格式差异很大：</p>
     * <ul>
     *   <li>DeepSeek：{@code sk-} + 32 位，约 35 字符</li>
     *   <li>千问AI平台：{@code sk-ws-H.xxxx.yyyy.zbase64sign}，可长达 100+ 字符，含点号</li>
     *   <li>OpenAI：{@code sk-proj-...}，可含连字符</li>
     *   <li>本地 Ollama：任意字符串（常为 {@code ollama}）</li>
     * </ul>
     *
     * <p>因此校验规则为：非空、不含空白、不含中文、长度 ≥ 8。
     * 真正的有效性由服务端返回的 401 判定，页面会据此自动降级。</p>
     */
    private static String resolveApiKey(Properties props) {
        String key = System.getenv(ENV_API_KEY);
        if (isBlank(key)) {
            key = System.getProperty(SYS_API_KEY);
        }
        if (isBlank(key)) {
            key = props.getProperty("agent.api.key");
        }
        if (isBlank(key)) {
            return null;
        }
        // 去掉可能从网页复制带上的引号与空白
        key = key.trim().replaceAll("^[\"']|[\"']$", "").trim();
        if (key.isEmpty()) {
            return null;
        }

        // 1) 排除模板里的说明文字 / 占位值
        String lower = key.toLowerCase();
        if (key.startsWith("在这里") || lower.contains("your-api-key")
                || lower.contains("your_api_key")
                || lower.equals("sk-xxx") || lower.equals("sk-xxxxxxxxxxxxxxxx")
                || lower.equals("none") || lower.equals("null")) {
            System.out.println("[AgentConfig] 检测到模板占位值，视为未配置 API Key");
            return null;
        }

        // 2) 含中文 → 一定是说明文字
        for (int i = 0; i < key.length(); i++) {
            if (key.charAt(i) > 0x4E00 && key.charAt(i) < 0x9FFF) {
                System.out.println("[AgentConfig] API Key 含中文字符，视为未配置");
                return null;
            }
        }

        // 3) 含空白 → 复制时带了换行或空格
        if (key.matches(".*\\s.*")) {
            System.out.println("[AgentConfig] API Key 含空白字符，请检查是否复制时多了空格或换行");
            return null;
        }

        // 4) 长度下限：过短的值不可能有效
        //    门槛设得很低（4 字符），因为本地部署（Ollama 等）常用 "ollama" 这类短占位值
        if (key.length() < 4) {
            System.out.println("[AgentConfig] API Key 过短（" + key.length() + " 字符），视为未配置");
            return null;
        }

        return key;
    }

    private static String getString(Properties props, String key, String defaultValue) {
        String v = props.getProperty(key);
        return isBlank(v) ? defaultValue : v.trim();
    }

    private static int getInt(Properties props, String key, int defaultValue) {
        try {
            String v = props.getProperty(key);
            return isBlank(v) ? defaultValue : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ======================================================================
    // 对外只读属性
    // ======================================================================

    /** 是否已配置可用的 API Key */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /** API Key（调用方不应直接打印该值） */
    public String getApiKey() {
        return apiKey;
    }

    /** Key 的脱敏展示，形如 sk-a****xyz，便于页面提示 */
    public String getMaskedApiKey() {
        if (!isConfigured()) {
            return "未配置";
        }
        if (apiKey.length() <= 10) {
            return "sk-****";
        }
        return apiKey.substring(0, 6) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 拼接完整的 Chat Completions 接口地址。
     *
     * <p>兼容各种 base URL 写法（OpenAI 兼容协议统一在末尾拼 {@code /chat/completions}）：</p>
     * <table border="1">
     *   <tr><th>agent.base.url 配置</th><th>实际请求地址</th></tr>
     *   <tr><td>{@code https://api.deepseek.com}</td>
     *       <td>{@code https://api.deepseek.com/chat/completions}</td></tr>
     *   <tr><td>{@code https://api.deepseek.com/v1}</td>
     *       <td>{@code https://api.deepseek.com/v1/chat/completions}</td></tr>
     *   <tr><td>{@code https://maas.qianwenai.com/compatible-mode/v1}</td>
     *       <td>{@code https://maas.qianwenai.com/compatible-mode/v1/chat/completions}</td></tr>
     *   <tr><td>{@code http://localhost:11434/v1}</td>
     *       <td>{@code http://localhost:11434/v1/chat/completions}</td></tr>
     * </table>
     *
     * <p>若用户误把完整地址写进了 base url（已含 {@code /chat/completions}），
     * 不会重复拼接。</p>
     */
    public String getChatCompletionsUrl() {
        String base = baseUrl == null ? "" : baseUrl.trim();
        // 去掉结尾多余的斜杠
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        // 用户可能已经写全了，避免重复拼接
        if (base.endsWith("/chat/completions")) {
            return base;
        }
        return base + "/chat/completions";
    }

    public String getModel() {
        return model;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getTopK() {
        return topK;
    }

    public String getConfigError() {
        return configError;
    }
}