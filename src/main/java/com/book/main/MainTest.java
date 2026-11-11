package com.book.main;

import com.book.dao.BookDao;
import com.book.dao.UserDao;
import com.book.entity.Book;
import com.book.entity.User;

import java.util.List;
import java.util.Scanner;

public class MainTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();
        BookDao bookDao = new BookDao();

        // ========== 登录界面 ==========
        System.out.println("========== 图书管理系统【登录界面】==========");
        System.out.print("请输入账号：");
        String username = scanner.next();
        System.out.print("请输入密码：");
        String password = scanner.next();

        User user = userDao.login(username, password);
        if (user == null) {
            System.out.println("账号或密码错误，登录失败！程序退出");
            scanner.close();
            return;
        }
        String loginRole = user.getRole();
        System.out.println("登录成功！当前身份：" + ("admin".equals(loginRole) ? "系统管理员" : "普通读者"));

        // ========== 数据操作菜单界面 ==========
        menuLoop: while (true) {
            System.out.println("\n========== 数据操作主界面 ==========");
            System.out.println("1. 查询全部图书");
            if ("admin".equals(loginRole)) {
                System.out.println("2. 新增图书");
                System.out.println("3. 修改图书");
                System.out.println("4. 删除图书");
            }
            System.out.println("0. 退出系统");
            System.out.print("请输入功能序号：");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    List<Book> bookList = bookDao.findAll();
                    System.out.println("书号\t书名\t作者\t库存");
                    for (Book b : bookList) {
                        System.out.printf("%s\t%s\t%s\t%d%n", b.getBid(), b.getBname(), b.getAuthor(), b.getStock());
                    }
                    break;
                case 2:
                    if (!"admin".equals(loginRole)) {
                        System.out.println("权限不足！仅管理员可用");
                        break;
                    }
                    System.out.print("输入新书号：");
                    String addBid = scanner.next();
                    System.out.print("输入书名：");
                    String addName = scanner.next();
                    System.out.print("输入作者：");
                    String addAuth = scanner.next();
                    System.out.print("输入库存数量：");
                    int addStock = scanner.nextInt();
                    int addRow = bookDao.addBook(new Book(addBid, addName, addAuth, addStock));
                    System.out.println(addRow > 0 ? "新增图书成功" : "新增失败，书号重复");
                    break;
                case 3:
                    if (!"admin".equals(loginRole)) {
                        System.out.println("权限不足！仅管理员可用");
                        break;
                    }
                    System.out.print("输入要修改的书号：");
                    String upBid = scanner.next();
                    System.out.print("新书名：");
                    String upName = scanner.next();
                    System.out.print("新作者：");
                    String upAuth = scanner.next();
                    System.out.print("新库存：");
                    int upStock = scanner.nextInt();
                    int upRow = bookDao.updateBook(new Book(upBid, upName, upAuth, upStock));
                    System.out.println(upRow > 0 ? "修改成功" : "无匹配图书，修改失败");
                    break;
                case 4:
                    if (!"admin".equals(loginRole)) {
                        System.out.println("权限不足！仅管理员可用");
                        break;
                    }
                    System.out.print("输入待删除书号：");
                    String delBid = scanner.next();
                    int delRow = bookDao.deleteBook(delBid);
                    System.out.println(delRow > 0 ? "删除成功" : "无匹配图书，删除失败");
                    break;
                case 0:
                    System.out.println("系统退出，感谢使用！");
                    break menuLoop;
                default:
                    System.out.println("输入序号错误，请重新选择");
            }
        }
        scanner.close();
    }
}