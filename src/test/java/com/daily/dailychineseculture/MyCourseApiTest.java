package com.daily.dailychineseculture;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.controller.CourseController;
import com.daily.dailychineseculture.dto.MyCourseVO;
import com.daily.dailychineseculture.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 我的课程接口测试类
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
@SpringBootTest
public class MyCourseApiTest {

    @Autowired
    private CourseController courseController;

    @Autowired
    private CourseService courseService;

    @Test
    public void testGetMyCoursesLearning() {
        // 测试获取正在学习的课程 (tabType=1)
        Result<List<MyCourseVO>> result = courseController.getMyCourses(1000001L, 1);
        
        assertNotNull(result);
        assertEquals(200, result.getCode().intValue());
        assertNotNull(result.getData());
        
        List<MyCourseVO> courses = result.getData();
        System.out.println("=== 正在学习的课程 ===");
        System.out.println("课程数量: " + courses.size());
        
        for (MyCourseVO course : courses) {
            System.out.println("- " + course.getTitle() + 
                             " (" + course.getType() + " " + course.getTerm() + ")" +
                             " 状态: " + course.getStatusText() + 
                             " 进度: " + course.getProgress() + "%");
        }
    }

    @Test
    public void testGetMyCoursesHistory() {
        // 测试获取历史课程 (tabType=2)
        Result<List<MyCourseVO>> result = courseController.getMyCourses(1000001L, 2);
        
        assertNotNull(result);
        assertEquals(200, result.getCode().intValue());
        
        System.out.println("=== 历史课程 ===");
        List<MyCourseVO> courses = result.getData();
        System.out.println("课程数量: " + courses.size());
        
        for (MyCourseVO course : courses) {
            System.out.println("- " + course.getTitle() + 
                             " (" + course.getType() + " " + course.getTerm() + ")" +
                             " 状态: " + course.getStatusText());
        }
    }

    @Test
    public void testGetMyCoursesCompleted() {
        // 测试获取已结业课程 (tabType=3)
        Result<List<MyCourseVO>> result = courseController.getMyCourses(1000001L, 3);
        
        assertNotNull(result);
        assertEquals(200, result.getCode().intValue());
        
        System.out.println("=== 已结业课程 ===");
        List<MyCourseVO> courses = result.getData();
        System.out.println("课程数量: " + courses.size());
        
        for (MyCourseVO course : courses) {
            System.out.println("- " + course.getTitle() + 
                             " (" + course.getType() + " " + course.getTerm() + ")" +
                             " 状态: " + course.getStatusText() + 
                             " 完成日期: " + course.getUpdateDate());
        }
    }

    @Test
    public void testInvalidParameters() {
        // 测试无效参数
        Result<List<MyCourseVO>> result1 = courseController.getMyCourses(null, 1);
        assertEquals(500, result1.getCode().intValue());
        assertTrue(result1.getMsg().contains("参数错误"));
        
        Result<List<MyCourseVO>> result2 = courseController.getMyCourses(1000001L, 4);
        assertEquals(500, result2.getCode().intValue());
        assertTrue(result2.getMsg().contains("参数错误"));
    }
}