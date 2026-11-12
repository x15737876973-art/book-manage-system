package com.book.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book.admin.entity.Book;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书 Mapper。
 *
 * <h3>为什么只需要写一个接口、零 SQL</h3>
 * <p>{@link BaseMapper} 已内置 17 个常用方法，泛型参数 {@code Book} 告诉它操作哪张表：</p>
 * <pre>
 *   insert(entity)                     新增
 *   deleteById(id)                     按主键删除
 *   updateById(entity)                 按主键更新（只更新非 null 字段）
 *   selectById(id)                     按主键查询
 *   selectList(wrapper)                条件查询列表
 *   selectPage(page, wrapper)          分页查询
 *   selectCount(wrapper)               条件统计
 * </pre>
 *
 * <p><b>对比你的 Servlet 版</b>：那边每个 DAO 方法都要手写 SQL 字符串、
 * 手动绑定参数、手动映射结果集；这里一行 SQL 都不用写。</p>
 *
 * <p>需要复杂 SQL（多表 JOIN、聚合）时，仍可在本接口里用 {@code @Select} 写自定义语句。</p>
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {
    // BaseMapper 已提供全部基础 CRUD，此处无需额外声明
}