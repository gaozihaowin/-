package com.daily.dailychineseculture;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.controller.CampController;
import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.service.CampService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 热门课程推荐接口测试类
 */
@SpringBootTest
public class HotCourseRecommendationTest {

    @Autowired
    private CampController campController;

    @Autowired
    private CampService campService;

    @Test
    public void testGetHotCourses() {
        // 测试获取热门课程推荐接口
        Result<List<CampVO>> result = campController.getHotCourses();
        
        // 验证响应
        assertNotNull(result);
        assertEquals(200, result.getCode().intValue());
        assertNotNull(result.getData());
        
        // 验证数据格式
        List<CampVO> courses = result.getData();
        assertTrue(courses.size() <= 5, "热门课程数量应该不超过5个");
        
        System.out.println("=== 热门课程推荐接口测试结果 ===");
        System.out.println("响应码: " + result.getCode());
        System.out.println("响应消息: " + result.getMsg());
        System.out.println("课程数量: " + courses.size());
        
        // 打印课程信息
        for (int i = 0; i < courses.size(); i++) {
            CampVO course = courses.get(i);
            System.out.println((i + 1) + ". " + course.getTitle() + 
                             " (ID: " + course.getId() + 
                             ", 类型: " + course.getType() + 
                             ", 期数: " + course.getTerm() + 
                             ", 人数: " + course.getCount() + ")");
        }
    }

    @Test
    public void testCampServiceDirectly() {
        // 直接测试服务层
        List<CampVO> hotCourses = campService.getHotCourses();
        
        assertNotNull(hotCourses);
        assertTrue(hotCourses.size() <= 5);
        
        System.out.println("=== 服务层直接调用测试 ===");
        System.out.println("获取到 " + hotCourses.size() + " 个热门课程推荐");
        
        for (CampVO course : hotCourses) {
            System.out.println("- " + course.getTitle() + 
                             " (" + course.getType() + " " + course.getTerm() + ")");
        }
    }
}