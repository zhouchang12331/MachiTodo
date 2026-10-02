package com.machi.todo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MachiTodo 启动类。
 * 启动后服务默认监听 8080 端口，前端页面即可通过 /api/task/** 访问数据。
 */
@SpringBootApplication
// 扫描 Mapper 接口所在包，这样 TaskMapper 不需要在每个接口上写 @Mapper
@MapperScan("com.machi.todo.mapper")
public class MachiTodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MachiTodoApplication.class, args);
    }
}
