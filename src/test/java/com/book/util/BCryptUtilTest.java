package com.book.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BCryptUtil} 单元测试。
 *
 * <p>覆盖点：</p>
 * <ul>
 *   <li>加密产物格式与长度符合 BCrypt 规范</li>
 *   <li>同一密码多次加密结果不同（随机盐）</li>
 *   <li>正确密码通过、错误密码拒绝</li>
 *   <li>null / 空串等边界输入不抛异常</li>
 *   <li>中文、长密码等特殊字符正确往返</li>
 *   <li>兼容历史明文密码（升级期过渡）</li>
 *   <li>cost 参数越界时快速失败</li>
 * </ul>
 */
@DisplayName("BCryptUtil 密码加密")
class BCryptUtilTest {

    @Nested
    @DisplayName("加密")
    class Hash {

        @Test
        @DisplayName("密文为 60 字符且符合 $2a$ 格式")
        void producesStandardFormat() {
            String hash = BCryptUtil.hashpw("123456");
            assertEquals(60, hash.length(), "BCrypt 密文固定 60 字符");
            assertTrue(hash.startsWith("$2a$"), "应以 $2a$ 开头");
            assertTrue(BCryptUtil.isBcryptHash(hash), "应被识别为合法密文");
        }

        @Test
        @DisplayName("相同密码两次加密结果不同（随机盐）")
        void usesRandomSalt() {
            String h1 = BCryptUtil.hashpw("123456");
            String h2 = BCryptUtil.hashpw("123456");
            assertNotEquals(h1, h2, "随机盐应保证同一密码每次结果不同");
            // 但两者都能校验通过
            assertTrue(BCryptUtil.checkpw("123456", h1));
            assertTrue(BCryptUtil.checkpw("123456", h2));
        }

        @Test
        @DisplayName("密文中不包含明文密码")
        void doesNotContainPlaintext() {
            String hash = BCryptUtil.hashpw("secretPassword");
            assertFalse(hash.contains("secretPassword"), "密文不应包含明文");
        }

        @ParameterizedTest(name = "密码=\"{0}\"")
        @ValueSource(strings = {
                "a",
                "123456",
                "中文密码测试",
                "P@ssw0rd!#$%^&*",
                "带 空 格 的 密 码",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        })
        @DisplayName("各种字符的密码都能正确往返")
        void roundTripVariousPasswords(String plain) {
            String hash = BCryptUtil.hashpw(plain);
            assertTrue(BCryptUtil.checkpw(plain, hash), "正确密码应校验通过");
            assertFalse(BCryptUtil.checkpw(plain + "x", hash), "加字符后应校验失败");
        }

        @Test
        @DisplayName("指定 cost 时生效")
        void respectsCost() {
            String hash = BCryptUtil.hashpw("123456", 4);
            assertTrue(hash.startsWith("$2a$04$"), "cost 应写入密文前缀");
            assertTrue(BCryptUtil.checkpw("123456", hash));
        }

        @Test
        @DisplayName("明文为 null 时抛 IllegalArgumentException")
        void rejectsNullPassword() {
            assertThrows(IllegalArgumentException.class, () -> BCryptUtil.hashpw(null));
        }

        @ParameterizedTest(name = "cost={0}")
        @ValueSource(ints = {0, 3, 32, -1})
        @DisplayName("cost 越界时快速失败")
        void rejectsInvalidCost(int cost) {
            assertThrows(IllegalArgumentException.class, () -> BCryptUtil.hashpw("x", cost));
        }
    }

    @Nested
    @DisplayName("校验")
    class Check {

        @Test
        @DisplayName("错误密码被拒绝")
        void rejectsWrongPassword() {
            String hash = BCryptUtil.hashpw("correct");
            assertFalse(BCryptUtil.checkpw("wrong", hash));
            assertFalse(BCryptUtil.checkpw("Correct", hash), "大小写敏感");
            assertFalse(BCryptUtil.checkpw("correct ", hash), "尾随空格应视为不同");
        }

        @Test
        @DisplayName("null 输入返回 false 而不抛异常")
        void handlesNullsGracefully() {
            assertFalse(BCryptUtil.checkpw(null, "hash"));
            assertFalse(BCryptUtil.checkpw("pwd", null));
            assertFalse(BCryptUtil.checkpw(null, null));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "not-a-hash", "$2a$10$short"})
        @DisplayName("非法密文返回 false 而不抛异常")
        void handlesInvalidHash(String hash) {
            assertFalse(BCryptUtil.checkpw("123456", hash));
        }
    }

    @Nested
    @DisplayName("历史明文兼容（升级期过渡）")
    class LegacyPlaintext {

        @Test
        @DisplayName("数据库仍存明文时可校验通过")
        void acceptsLegacyPlaintext() {
            assertTrue(BCryptUtil.checkpw("123456", "123456"),
                    "旧数据为明文时应仍能登录，登录逻辑会顺带升级为密文");
        }

        @Test
        @DisplayName("历史明文与输入不符时拒绝")
        void rejectsMismatchedLegacyPlaintext() {
            assertFalse(BCryptUtil.checkpw("123456", "654321"));
        }

        @Test
        @DisplayName("明文不会被误判为 BCrypt 密文")
        void plaintextNotDetectedAsHash() {
            assertFalse(BCryptUtil.isBcryptHash("123456"));
            assertFalse(BCryptUtil.isBcryptHash("password"));
            assertFalse(BCryptUtil.isBcryptHash(null));
        }
    }

    @Nested
    @DisplayName("辅助方法")
    class Helpers {

        @Test
        @DisplayName("randomPassword 长度正确且随机")
        void randomPasswordWorks() {
            String p1 = BCryptUtil.randomPassword(12);
            String p2 = BCryptUtil.randomPassword(12);
            assertEquals(12, p1.length());
            assertNotEquals(p1, p2, "两次生成应不同");
        }

        @Test
        @DisplayName("真实的 BCrypt 密文被识别为合法格式")
        void detectsRealHash() {
            assertTrue(BCryptUtil.isBcryptHash(BCryptUtil.hashpw("123456")));
        }

        @Test
        @DisplayName("非密文（明文、空串、长度不符）一律判为 false")
        void rejectsNonHash() {
            assertFalse(BCryptUtil.isBcryptHash("123456"));
            assertFalse(BCryptUtil.isBcryptHash(""));
            assertFalse(BCryptUtil.isBcryptHash(null));
            // 长度不足 60 的统一为 false
            assertFalse(BCryptUtil.isBcryptHash("$2a$10$abcdefghijklmnopqrstuu"));
            // 长度正好 60 但格式合法
            String realHash = BCryptUtil.hashpw("x");
            assertEquals(60, realHash.length());
            assertTrue(BCryptUtil.isBcryptHash(realHash));
        }
    }
}