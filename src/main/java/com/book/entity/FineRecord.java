package com.book.entity;

import java.util.Date;

/**
 * 罚款记录实体类
 */
public class FineRecord {
    private Integer id;
    private Integer userId;
    private String username; // 用于显示
    private Integer borrowRecordId;
    private String bookName; // 用于显示图书名称
    private Double amount;
    private Integer days;
    private String status; // unpaid, paid
    private Date createTime;
    
    public FineRecord() {}
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Integer getBorrowRecordId() {
        return borrowRecordId;
    }
    
    public void setBorrowRecordId(Integer borrowRecordId) {
        this.borrowRecordId = borrowRecordId;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public Integer getDays() {
        return days;
    }
    
    public void setDays(Integer days) {
        this.days = days;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public String getBookName() {
        return bookName;
    }
    
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    
    // 添加userName属性的getter方法，返回username
    public String getUserName() {
        return username;
    }
}