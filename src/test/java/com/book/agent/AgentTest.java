package com.book.agent;

import com.book.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 智能问答模块单元测试。
 *
 * <p>不依赖网络与数据库，纯逻辑验证：</p>
 * <ul>
 *   <li><b>关键词提取</b>：中文分词的正确性与边界</li>
 *   <li><b>Prompt 构造</b>：检索结果注入 + 防幻觉约束</li>
 *   <li><b>配置校验</b>：API Key 格式判定与 URL 拼接</li>
 * </ul>
 */
@DisplayName("智能问答模块")
class AgentTest {

    // ======================================================================
    @Nested
    @DisplayName("关键词提取")
    class Keywords {

        @ParameterizedTest(name = "\"{0}\" -> 命中 \"{1}\"")
        @CsvSource({
                "有《三体》吗？, 三体",
                "红楼梦, 红楼梦",
                "有《高等数学》吗, 高等数学",
                "刘慈欣的书有哪些？, 刘慈欣",
                "推荐几本计算机类的图书, 计算机类",
                "不存在的书, 不存在"
        })
        @DisplayName("能从自然语言问句中提取出书名/作者")
        void extractsMeaningfulKeywords(String question, String expected) {
            List<String> keywords = BookKnowledgeBase.extractKeywords(question);
            assertTrue(keywords.stream().anyMatch(k -> k.contains(expected)),
                    "应提取到 keyword 包含 \"" + expected + "\"，实际提取到: " + keywords);
        }

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"你好", "谢谢", "嗯", "请问"})
        @DisplayName("纯寒暄语提取不到有效关键词（用于触发推荐兜底）")
        void greetingHasNoKeywords(String greeting) {
            assertTrue(BookKnowledgeBase.extractKeywords(greeting).isEmpty(),
                    "寒暄语不应产生检索关键词: " + greeting);
        }

        @Test
        @DisplayName("null 与空白输入返回空列表，不抛异常")
        void handlesNullAndBlank() {
            assertTrue(BookKnowledgeBase.extractKeywords(null).isEmpty());
            assertTrue(BookKnowledgeBase.extractKeywords("").isEmpty());
            assertTrue(BookKnowledgeBase.extractKeywords("   ").isEmpty());
        }

        @Test
        @DisplayName("提取结果去重")
        void deduplicates() {
            List<String> keywords = BookKnowledgeBase.extractKeywords("三体 三体 三体");
            assertEquals(1, keywords.size(), "重复关键词应去重");
        }

        @Test
        @DisplayName("提取结果不超过上限")
        void limitsKeywordCount() {
            List<String> keywords = BookKnowledgeBase.extractKeywords(
                    "红楼梦 三国演义 西游记 水浒传 史记 资治通鉴 二十四史");
            assertTrue(keywords.size() <= 5, "最多保留 5 个关键词，实际: " + keywords.size());
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("Prompt 构造与防幻觉")
    class Prompt {

        private Book sampleBook() {
            Book b = new Book();
            b.setBid("B001");
            b.setBname("红楼梦");
            b.setAuthor("曹雪芹");
            b.setPublisher("人民文学出版社");
            b.setIsbn("978-7-02-013757-5");
            b.setCategoryName("文学");
            b.setPrice(59.7);
            b.setStock(5);
            b.setAvailableStock(2);
            b.setLocation("B区-1排-1架");
            return b;
        }

        @Test
        @DisplayName("System Prompt 包含禁止编造的硬约束")
        void systemPromptForbidsHallucination() {
            String sys = PromptBuilder.systemPrompt();
            assertTrue(sys.contains("严禁编造"), "必须明确禁止编造");
            assertTrue(sys.contains("只能依据"), "必须限定只能依据馆藏资料");
        }

        @Test
        @DisplayName("有检索结果时，全部关键信息写入 Prompt")
        void userPromptContainsBookFacts() {
            String prompt = PromptBuilder.userPrompt(
                    Collections.singletonList(sampleBook()), "有红楼梦吗");

            assertTrue(prompt.contains("红楼梦"), "书名");
            assertTrue(prompt.contains("曹雪芹"), "作者");
            assertTrue(prompt.contains("可借：2本"), "可借数量");
            assertTrue(prompt.contains("B区-1排-1架"), "存放位置");
            assertTrue(prompt.contains("有红楼梦吗"), "用户原问题");
        }

        @Test
        @DisplayName("检索为空时注入固定话术，且不携带用户问题（杜绝顺着问句编书）")
        void emptyResultUsesFixedScript() {
            String prompt = PromptBuilder.userPrompt(Collections.emptyList(), "有《时间简史》吗？");

            assertTrue(prompt.contains("本馆暂未收录"), "应注入固定话术");
            assertFalse(prompt.contains("时间简史"),
                    "空结果时不应把用户问题原文传给模型，防止模型顺着问句编造");
        }

        @Test
        @DisplayName("prompt 不因字段为 null 而抛异常")
        void handlesNullFields() {
            Book empty = new Book();
            empty.setBname("只有书名");
            String prompt = PromptBuilder.userPrompt(Collections.singletonList(empty), "测试");
            assertNotNull(prompt);
            assertTrue(prompt.contains("只有书名"));
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("配置校验")
    class Config {

        @Test
        @DisplayName("URL 拼接：各种 base url 都能正确补全 /chat/completions")
        void buildsChatCompletionsUrl() throws Exception {
            assertUrl("https://api.deepseek.com", "https://api.deepseek.com/chat/completions");
            assertUrl("https://api.deepseek.com/", "https://api.deepseek.com/chat/completions");
            assertUrl("https://api.deepseek.com/v1", "https://api.deepseek.com/v1/chat/completions");
            assertUrl("https://maas.qianwenai.com/compatible-mode/v1",
                    "https://maas.qianwenai.com/compatible-mode/v1/chat/completions");
            assertUrl("http://localhost:11434/v1",
                    "http://localhost:11434/v1/chat/completions");
        }

        private void assertUrl(String baseUrl, String expected) throws Exception {
            AgentConfig cfg = newConfigWithBaseUrl(baseUrl);
            assertEquals(expected, cfg.getChatCompletionsUrl(), "base url: " + baseUrl);
        }

        @Test
        @DisplayName("已含 /chat/completions 时不会重复拼接")
        void doesNotDoubleAppend() throws Exception {
            AgentConfig cfg = newConfigWithBaseUrl("https://api.deepseek.com/chat/completions");
            assertEquals("https://api.deepseek.com/chat/completions", cfg.getChatCompletionsUrl());
        }

        /** 通过反射构造不同 base url 的配置实例 */
        private AgentConfig newConfigWithBaseUrl(String baseUrl) throws Exception {
            java.util.Properties p = new java.util.Properties();
            p.setProperty("agent.base.url", baseUrl);
            java.lang.reflect.Constructor<AgentConfig> ctor =
                    AgentConfig.class.getDeclaredConstructor(java.util.Properties.class);
            ctor.setAccessible(true);
            return ctor.newInstance(p);
        }
    }
}