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

    /** DeepSeek 官方 API 基础地址（兼容 OpenAI 协议） */
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

    /** 按优先级解析 API Key */
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
        key = key.trim();
        // 排除模板占位值
        if (key.startsWith("在这里") || key.contains("your-api-key") || key.equals("sk-xxx")) {
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

    /** 拼接完整接口地址，例如 https://api.deepseek.com/chat/completions */
    public String getChatCompletionsUrl() {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        if (base.endsWith("/v1")) {
            return base + "/chat/completions";
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