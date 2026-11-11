package com.book.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 密码加密工具类（统一密码处理入口）。
 *
 * <h3>为什么用 BCrypt？</h3>
 * <p>MD5 / SHA-1 / SHA-256 属于"快速哈希"，GPU 每秒可尝试数十亿次，配合彩虹表
 * 可在极短时间内还原出明文，<b>不能用于密码存储</b>。BCrypt 的设计目标是"慢"：</p>
 * <ul>
 *   <li><b>自带随机盐</b>：相同密码每次加密结果都不同，彩虹表失效；</li>
 *   <li><b>可调计算强度（cost）</b>：cost 每 +1，计算量翻倍，可随硬件升级持续加码；</li>
 *   <li><b>抗 GPU/ASIC 暴力破解</b>：需要大量内存访问，难以并行加速。</li>
 * </ul>
 *
 * <h3>实现来源</h3>
 * <p>底层调用 <b>jBCrypt 0.4</b>（{@code org.mindrot:jbcrypt}），这是 BCrypt 算法的
 * Java 官方参考实现，与 OpenBSD bcrypt、Spring Security 的
 * {@code BCryptPasswordEncoder} 完全兼容，可互相校验。
 * 密码学算法务必使用成熟实现，不要手写。</p>
 *
 * <h3>密文格式</h3>
 * <p>{@code $2a$10$<22字符盐><31字符哈希>}，固定 60 个字符。
 * 数据库 {@code users.password} 字段需为 VARCHAR(60) 以上（原 VARCHAR(100) 足够）。</p>
 *
 * <h3>使用方式</h3>
 * <pre>
 *   // 注册 / 改密：加密后入库
 *   String hash = BCryptUtil.hashpw(plainPassword);
 *
 *   // 登录：前端明文与数据库密文比对
 *   boolean ok = BCryptUtil.checkpw(plainPassword, hashFromDb);
 * </pre>
 *
 * <h3>关于历史明文密码的兼容</h3>
 * <p>项目早期版本曾以明文存储密码。为平滑升级，{@link #checkpw(String, String)} 做了
 * 两级校验：先按 BCrypt 校验；若数据库里仍是旧明文，则退化为一次<b>常量时间</b>的
 * 明文比较，并在成功后由调用方（登录逻辑）自动把该用户的密码升级为 BCrypt 密文。
 * 新建用户、修改密码、重置密码一律写入 BCrypt 密文，不会再产生新的明文。</p>
 *
 * @author book-manage-system
 */
public final class BCryptUtil {

    /** 默认计算强度：2^10 = 1024 轮，单次约 50~100ms，安全与体验的平衡点。 */
    public static final int DEFAULT_COST = 10;

    /** BCrypt 密文固定长度 */
    public static final int HASH_LENGTH = 60;

    private static final int MIN_COST = 4;
    private static final int MAX_COST = 31;

    private BCryptUtil() {
    }

    // ======================================================================
    // 对外 API
    // ======================================================================

    /**
     * 加密明文密码（默认 cost=10）。
     *
     * @param plainPassword 明文密码
     * @return 60 字符的 BCrypt 密文
     * @throws IllegalArgumentException 明文为 null 时抛出
     */
    public static String hashpw(String plainPassword) {
        return hashpw(plainPassword, DEFAULT_COST);
    }

    /**
     * 用指定 cost 加密明文密码。
     *
     * @param plainPassword 明文
     * @param cost          计算强度，4~31，推荐 10~12
     * @return 60 字符密文
     * @throws IllegalArgumentException 明文为 null 或 cost 越界时抛出
     */
    public static String hashpw(String plainPassword, int cost) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("密码不能为 null");
        }
        if (cost < MIN_COST || cost > MAX_COST) {
            throw new IllegalArgumentException("BCrypt cost 必须在 " + MIN_COST + "~" + MAX_COST + " 之间");
        }
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt(cost));
    }

    /**
     * 使用指定盐加密（测试与迁移场景使用）。
     *
     * @param plainPassword 明文
     * @param salt          形如 {@code $2a$10$<22字符>} 的盐串
     * @return 60 字符密文
     */
    public static String hashpw(String plainPassword, String salt) {
        if (plainPassword == null || salt == null) {
            throw new IllegalArgumentException("密码与盐都不能为 null");
        }
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * 校验密码。<b>兼容历史明文</b>：若数据库里存的不是 BCrypt 密文，
     * 则按明文做常量时间比较，使老账号仍可登录（登录逻辑会顺带升级其密文）。
     *
     * @param plainPassword 用户输入的明文
     * @param stored        数据库中存储的值（BCrypt 密文或历史明文）
     * @return true 表示密码正确
     */
    public static boolean checkpw(String plainPassword, String stored) {
        if (plainPassword == null || stored == null || stored.isEmpty()) {
            return false;
        }
        if (isBcryptHash(stored)) {
            try {
                return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, stored);
            } catch (Exception e) {
                // 密文损坏时按校验失败处理，避免抛出到页面
                return false;
            }
        }
        // 历史明文：使用常量时间比较，避免时序侧信道
        return constantTimeEquals(plainPassword.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 判断存储值是否已经是 BCrypt 密文（用于判断是否需要升级）。
     */
    public static boolean isBcryptHash(String value) {
        return value != null && value.length() == HASH_LENGTH
                && value.charAt(0) == '$' && value.charAt(1) == '2'
                && (value.charAt(2) == 'a' || value.charAt(2) == 'b' || value.charAt(2) == 'y')
                && value.charAt(3) == '$' && value.charAt(6) == '$';
    }

    /**
     * 生成一个随机密码（用于"重置密码"等场景，避免硬编码固定密码）。
     *
     * @param length 长度，建议 8~16
     * @return 随机密码字符串
     */
    public static String randomPassword(int length) {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    // ======================================================================
    // 内部工具
    // ======================================================================

    /** 常量时间字节比较，避免因提前 return 泄露长度信息 */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }
}