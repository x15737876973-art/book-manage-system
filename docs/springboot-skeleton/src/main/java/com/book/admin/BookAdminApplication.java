package com.book.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用入口。
 *
 * <h3>三个注解的作用</h3>
 * <ul>
 *   <li>{@code @SpringBootApplication} = {@code @Configuration} + {@code @EnableAutoConfiguration}
 *       + {@code @ComponentScan}。它会自动扫描本类所在包及子包下的所有组件（@Controller/@Service 等），
 *       并根据 classpath 中的依赖自动配置（比如看到 MySQL 驱动就自动配数据源）。</li>
 *   <li>{@code @MapperScan} 指定 MyBatis 的 Mapper 接口所在包。
 *       加了它以后，Mapper 接口上就不必逐个写 {@code @Mapper} 注解。</li>
 * </ul>
 *
 * <p>启动后内嵌 Tomcat 监听 8080 端口，无需额外安装 Tomcat。</p>
 */
@SpringBootApplication
@MapperScan("com.book.admin.mapper")
public class BookAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookAdminApplication.class, args);
    }
}