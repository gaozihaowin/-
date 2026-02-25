package com.daily.dailychineseculture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * 数据库结构验证测试
 */
@SpringBootTest
public class DatabaseStructureTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testCampTableStructure() {
        // 查询t_camp表的列信息
        String sql = "DESCRIBE t_camp";
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        System.out.println("=== t_camp 表结构 ===");
        for (Map<String, Object> column : columns) {
            System.out.println("字段名: " + column.get("Field") + 
                             ", 类型: " + column.get("Type") + 
                             ", 是否为空: " + column.get("Null") +
                             ", 默认值: " + column.get("Default"));
        }
        
        // 查询几条实际数据
        String dataSql = "SELECT * FROM t_camp LIMIT 3";
        List<Map<String, Object>> data = jdbcTemplate.queryForList(dataSql);
        
        System.out.println("\n=== t_camp 表数据示例 ===");
        for (Map<String, Object> row : data) {
            System.out.println("记录: " + row);
        }
    }
}