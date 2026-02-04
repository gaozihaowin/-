package com.daily.dailychineseculture;

import com.daily.dailychineseculture.service.IdGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class IdGeneratorServiceTest {

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Test
    public void testGenerateUserId() {
        // 生成第一个用户ID
        Long userId1 = idGeneratorService.generateUserId();
        assertNotNull(userId1);
        System.out.println("生成的第一个用户ID: " + userId1);
        
        // 生成第二个用户ID
        Long userId2 = idGeneratorService.generateUserId();
        assertNotNull(userId2);
        System.out.println("生成的第二个用户ID: " + userId2);
        
        // 验证两个ID不同且递增
        assertNotEquals(userId1, userId2);
        assertTrue(userId2 > userId1);
        
        // 验证ID格式符合预期 (年份+6位序号)
        String yearStr = String.valueOf(java.time.LocalDate.now().getYear());
        String id1Str = userId1.toString();
        String id2Str = userId2.toString();
        
        assertTrue(id1Str.startsWith(yearStr));
        assertTrue(id2Str.startsWith(yearStr));
        assertEquals(yearStr.length() + 6, id1Str.length());
        assertEquals(yearStr.length() + 6, id2Str.length());
    }
}