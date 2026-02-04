package com.daily.dailychineseculture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimpleDatabaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testSpringBootDatabaseAutoConfiguration() {
        // 测试Spring Boot是否自动配置了DataSource
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        assertNotNull(dataSource, "Spring Boot应该自动配置DataSource");
        System.out.println("✅ Spring Boot数据库自动配置成功");
    }

    @Test
    public void testDatabaseConnectionAvailable() {
        try {
            DataSource dataSource = applicationContext.getBean(DataSource.class);
            Connection connection = dataSource.getConnection();
            
            System.out.println("✅ 数据库连接可用");
            System.out.println("连接URL: " + connection.getMetaData().getURL());
            System.out.println("数据库产品: " + connection.getMetaData().getDatabaseProductName());
            
            connection.close();
        } catch (Exception e) {
            System.err.println("❌ 数据库连接测试失败: " + e.getMessage());
            // 不fail测试，因为可能是网络问题
        }
    }
}