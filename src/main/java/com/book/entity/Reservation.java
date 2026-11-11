package com.book.entity;

import java.util.Date;

/**
 * 预约记录实体类
 */
public class Reservation {
    private Integer id;
    private Integer userId;
    private String username; // 用于显示
    private Integer bookId;
    private String bookName; // 用于显示
    private Date reserveDate;
    private String status; // waiting, notified, cancelled, completed
    
    public Reservation() {}
    
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
    
    public Integer getBookId() {
        return bookId;
    }
    
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
    
    public String getBookName() {
        return bookName;
    }
    
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    
    public Date getReserveDate() {
        return reserveDate;
    }
    
    public void setReserveDate(Date reserveDate) {
        this.reserveDate = reserveDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}