package com.book.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书实体，对应数据库 books 表。
 *
 * <p><b>为什么手写 getter/setter 而不用 Lombok</b>：Lombok 需要 IDE 装插件并在
 * Maven 里配置注解处理器，新手极易踩坑（编译报"找不到 getXxx()"）。
 * 实体类字段数量固定，手写一次即可，反而更省心。</p>
 *
 * <h3>MyBatis-Plus 注解</h3>
 * <ul>
 *   <li>{@code @TableName("books")}：指定对应的表名</li>
 *   <li>{@code @TableId(type = IdType.AUTO)}：主键使用数据库自增</li>
 *   <li>{@code @TableField(exist = false)}：标注不是数据库列的字段</li>
 * </ul>
 */
@TableName("books")
public class Book {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 图书编号，如 B001 */
    private String bid;

    private String isbn;

    /** 书名 */
    private String bname;

    private String author;

    private String publisher;

    /** 出版日期 */
    private LocalDate publishDate;

    private BigDecimal price;

    private Integer categoryId;

    /** 总库存 */
    private Integer stock;

    /** 可借库存 */
    private Integer availableStock;

    /** 存放位置 */
    private String location;

    private String description;

    /** 入库时间，数据库有默认值 */
    private LocalDateTime createTime;

    /** 分类名称（非数据库字段，连表查询时填充） */
    @TableField(exist = false)
    private String categoryName;

    // ==================== getter / setter ====================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}