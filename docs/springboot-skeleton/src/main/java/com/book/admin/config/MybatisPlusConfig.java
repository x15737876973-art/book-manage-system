package com.book.admin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置。
 *
 * <h3>分页插件为什么必须手动注册</h3>
 * <p>MyBatis-Plus 的分页<b>不是</b>简单地在 SQL 末尾拼 {@code LIMIT}，
 * 它需要知道当前用的是什么数据库（MySQL / Oracle / PostgreSQL 的分页语法各不相同），
 * 才能生成正确的分页语句。因此必须显式注册 {@link PaginationInnerInterceptor}
 * 并告诉它方言类型。</p>
 *
 * <p><b>没有这一步的典型症状</b>：调用 {@code selectPage} 返回的是全部数据，
 * 分页参数被忽略——这是新手最常见的坑之一。</p>
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页最大条数上限，防止一次查询过多数据
        pagination.setMaxLimit(500L);
        // 超过最大页码时是否回到首页（false 表示返回空列表）
        pagination.setOverflow(false);

        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}