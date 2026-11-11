package com.book.entity;

import java.util.Date;

/**
 * 图书实体类
 */
public class Book {
    private Integer id;
    private String bid;
    private String isbn;
    private String bname;
    private String author;
    private String publisher;
    private Date publishDate;
    private Double price;
    private Integer categoryId;
    private String categoryName; // 用于显示
    private Integer stock;
    private Integer availableStock;
    private String location;
    private String description;
    private Date createTime;
    
    public Book() {}
    
    public Book(String bid, String bname, String author, Integer stock) {
        this.bid = bid;
        this.bname = bname;
        this.author = author;
        this.stock = stock;
        this.availableStock = stock;
    }
    
    // Getters and Setters
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
    
    // getTitle() 作为 getBname() 的别名
    public String getTitle() {
        return bname;
    }
    
    public void setTitle(String title) {
        this.bname = title;
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
    
    public Date getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
