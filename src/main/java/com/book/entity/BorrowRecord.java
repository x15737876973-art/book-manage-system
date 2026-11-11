package com.book.entity;

import java.util.Date;

/**
 * 借阅记录实体类
 */
public class BorrowRecord {
    private Integer id;
    private Integer userId;
    private String username; // 用于显示
    private String realName; // 用于显示读者姓名
    private Integer bookId;
    private String bookName; // 用于显示
    private String bookTitle; // 用于显示图书标题
    private String bookAuthor; // 用于显示图书作者
    private String bookBid; // 用于显示
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private String status; // borrowed, returned, overdue
    private Integer renewCount;
    private Double fineAmount;
    private Boolean isPaid;
    private Date createTime;
    private Integer overdueDays; // 逾期天数
    
    public BorrowRecord() {}
    
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
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
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
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    
    public String getBookBid() {
        return bookBid;
    }
    
    public void setBookBid(String bookBid) {
        this.bookBid = bookBid;
    }
    
    public Date getBorrowDate() {
        return borrowDate;
    }
    
    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public Date getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getRenewCount() {
        return renewCount;
    }
    
    public void setRenewCount(Integer renewCount) {
        this.renewCount = renewCount;
    }
    
    public Double getFineAmount() {
        return fineAmount;
    }
    
    public void setFineAmount(Double fineAmount) {
        this.fineAmount = fineAmount;
    }
    
    public Boolean getIsPaid() {
        return isPaid;
    }
    
    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Integer getOverdueDays() {
        return overdueDays;
    }
    
    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }
    
    // 添加userName属性的getter方法，返回realName
    public String getUserName() {
        return realName;
    }
}
