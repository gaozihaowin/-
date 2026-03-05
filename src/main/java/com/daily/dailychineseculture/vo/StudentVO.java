package com.daily.dailychineseculture.vo; // 修正包名

import com.daily.dailychineseculture.entity.User;
import lombok.Data;
import java.util.Date;

/**
 * 学员视图对象（Web端展示专用）
 */
@Data
public class StudentVO {
    // ========== 从 t_user 表复用（仅展示字段） ==========
    private Long userId;          // 用户ID（关联User的userId）
    private String nickname;      // 昵称（学员姓名）
    private String phone;         // 手机号
    private String region;        // 地域（对应User的region）
    private Date birthday;        // 生日（用于计算年龄）
    private String profession;    // 职业（对应User的profession）
    private String avatar;        // 头像（可选）
    private Integer gender;       // 性别（可选）

    // ========== 从 t_camp_enrollment 表新增（报名信息） ==========
    private Long campId;          // 报名的课程ID
    private Integer applyStatus;  // 报名状态（1待审核/2审核通过/3驳回）
    private Date enrollTime;      // 报名时间

    // ========== 从 t_class 表新增（班级信息） ==========
    private Long classId;         // 分配的班级ID（0=未分班）
    private String className;     // 班级名称（如：致良知69期-1班）

    // ========== 辅助字段（计算得出，不存储） ==========
    private Integer age;          // 年龄（通过birthday计算）

    // 快速将User转换为StudentVO（避免重复赋值）
    public static StudentVO fromUser(User user) {
        StudentVO vo = new StudentVO();
        vo.setUserId(user.getUserId());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setRegion(user.getRegion());
        vo.setBirthday(user.getBirthday());
        vo.setProfession(user.getProfession());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());

        // 修正年龄计算，避免弃用警告
        if (user.getBirthday() != null) {
            java.time.LocalDate birthday = user.getBirthday().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            int age = java.time.Period.between(birthday, java.time.LocalDate.now()).getYears();
            vo.setAge(age);
        }
        return vo;
    }
}