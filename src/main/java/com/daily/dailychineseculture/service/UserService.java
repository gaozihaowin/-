package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.UserInfoDTO;
import com.daily.dailychineseculture.dto.VolunteerStatsDTO;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IdGeneratorService idGeneratorService;

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 创建用户
     */
    public User createUser(User user) {
        // 生成用户ID
        Long userId = idGeneratorService.generateUserId();
        user.setUserId(userId);
        user.setCreateTime(new Date());
        user.setStatus(1); // 默认状态为正常
        user.setGender(0); // 默认性别为未知

        userMapper.insert(user);
        return user;
    }

    /**
     * 更新用户
     */
    public User updateUser(User user) {
        int result = userMapper.update(user);
        if (result > 0) {
            return user;
        }
        return null;
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    /**
     * 检查用户是否存在
     * @param username 用户名
     * @return 是否存在
     */
    public boolean checkUserExists(String username) {
        try {
            User user = userMapper.selectByAccount(username);
            return user != null;
        } catch (Exception e) {
            // 如果查询出错，默认返回false
            return false;
        }
    }

    /**
     * 验证用户密码
     * @param username 用户名
     * @param password 密码
     * @return 密码是否正确
     */
    public boolean verifyPassword(String username, String password) {
        try {
            User user = userMapper.selectByAccount(username);
            return user != null && password.equals(user.getPassword());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建新用户
     * @param username 用户名
     * @param password 密码
     * @return 是否创建成功
     */
    public boolean createUser(String username, String password) {
        try {
            User user = new User();
            user.setAccount(username);
            user.setPassword(password);
            user.setCreateTime(new Date());
            user.setStatus(1);
            user.setGender(0);
            user.setOpenid(""); // 微信openid设为空字符串

            // 生成用户ID
            Long userId = idGeneratorService.generateUserId();
            user.setUserId(userId);

            int result = userMapper.insert(user);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户对象
     */
    public User getUserByUsername(String username) {
        return userMapper.selectByAccount(username);
    }

    /**
     * 检查用户信息是否完整
     * @param user 用户对象
     * @return true表示信息完整，false表示信息不完整
     */
    public boolean isUserInfoComplete(User user) {
        if (user == null) {
            return false;
        }

        // 检查手机号是否为空或默认值
        boolean phoneValid = user.getPhone() != null && !user.getPhone().trim().isEmpty();

        // 检查头像是否为空或默认值
        boolean avatarValid = user.getAvatar() != null && !user.getAvatar().trim().isEmpty();

        // 检查性别是否为有效值（非0）
        boolean genderValid = user.getGender() != null && user.getGender() != 0;

        // 检查生日是否设置
        boolean birthdayValid = user.getBirthday() != null;

        // 所有字段都必须有效才算信息完整
        return phoneValid && avatarValid && genderValid && birthdayValid;
    }

    /**
     * 将User实体转换为UserInfoDTO
     * @param user 用户实体
     * @return UserInfoDTO对象
     */
    public UserInfoDTO convertToUserInfoDTO(User user) {
        if (user == null) {
            return null;
        }

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserid(String.valueOf(user.getUserId()));
        userInfoDTO.setUsername(user.getAccount());
        userInfoDTO.setAvatar(user.getAvatar() != null ? user.getAvatar() : "");
        userInfoDTO.setPhone(user.getPhone() != null ? user.getPhone() : "");
        userInfoDTO.setGender(user.getGender() != null ? user.getGender() : 0);

        // 生日格式化处理
        if (user.getBirthday() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            userInfoDTO.setBirthday(sdf.format(user.getBirthday()));
        } else {
            userInfoDTO.setBirthday("");
        }

        return userInfoDTO;
    }

    /**
     * 创建新用户并返回用户对象
     * @param username 用户名
     * @param password 密码
     * @return 用户对象
     */
    public User createUserWithReturn(String username, String password) {
        try {
            System.out.println("开始创建新用户: " + username);

            User user = new User();
            user.setAccount(username);
            user.setPassword(password);
            user.setCreateTime(new Date());
            user.setStatus(1);
            user.setGender(0);

            // 设置默认值避免数据库NOT NULL约束
            user.setAvatar(""); // 空字符串头像
            user.setPhone(""); // 空字符串手机号
            user.setRegion(""); // 空字符串地域
            user.setProfession(""); // 空字符串职业
            user.setBirthday(null); // null生日

            // 生成用户ID
            Long userId = idGeneratorService.generateUserId();
            user.setUserId(userId);

            System.out.println("准备插入用户数据: " + user);

            int result = userMapper.insert(user);
            System.out.println("数据库插入结果: " + result);

            if (result > 0) {
                System.out.println("用户创建成功: " + username + ", userId: " + userId);
                return user;
            } else {
                System.err.println("用户创建失败: 数据库返回影响行数为0");
                return null;
            }
        } catch (Exception e) {
            System.err.println("=== 用户注册异常详情 ===");
            System.err.println("用户名: " + username);
            System.err.println("异常类型: " + e.getClass().getSimpleName());
            System.err.println("异常信息: " + e.getMessage());
            System.err.println("异常堆栈:");
            e.printStackTrace();
            System.err.println("=====================");
            return null;
        }
    }
    /**
     * 查找或创建微信用户
     */
    public User findOrCreateWxUser(String openid, String nickname, String avatar) {
        // 查询用户是否存在
        User user = userMapper.selectByOpenid(openid);

        if (user == null) {
            // 创建新用户
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname);
            user.setAvatar(avatar);

            // 使用标准的ID生成服务
            Long userId = idGeneratorService.generateUserId();
            user.setUserId(userId);

            // 生成默认账户名（使用标准的账号格式）
            String defaultAccount = "wx_" + userId;
            user.setAccount(defaultAccount);

            // 设置默认密码（微信用户可能不需要密码）
            user.setPassword("wx_default_password");

            // 设置默认状态和时间
            user.setCreateTime(new Date());
            user.setStatus(1); // 默认状态为正常
            user.setGender(0); // 默认性别为未知

            // 插入用户
            userMapper.insert(user);
            System.out.println("📝 新微信用户注册成功，ID：" + user.getUserId() + "，昵称：" + nickname);
        } else {
            // 更新用户信息
            if (!nickname.equals(user.getNickname()) || !avatar.equals(user.getAvatar())) {
                user.setNickname(nickname);
                user.setAvatar(avatar);
                userMapper.update(user);
                System.out.println("📝 微信用户信息更新成功，ID：" + user.getUserId());
            }
        }

        return user;
    }

    /**
     * 检查用户是否为志愿者
     */
    public boolean isVolunteer(Long userId) {
        try {
            Integer count = userMapper.countVolunteerAssignments(userId);
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("查询志愿者权限失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新用户信息
     */
    public boolean updateUserInfo(Long userId, String nickname, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname);
        }
        if (avatar != null && !avatar.trim().isEmpty()) {
            user.setAvatar(avatar);
        }

        userMapper.update(user);
        return true;
    }

    /**
     * 获取用户志愿者历史记录
     */
    public com.daily.dailychineseculture.dto.VolunteerHistoryDTO getVolunteerHistory(Long userId) {
        List<Map<String, Object>> historyList = userMapper.getVolunteerHistory(userId);

        com.daily.dailychineseculture.dto.VolunteerHistoryDTO result = new com.daily.dailychineseculture.dto.VolunteerHistoryDTO();
        result.setVolunteerHistory(new ArrayList<>());

        // 获取当前日期（格式：YYYY.MM.DD）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String currentDate = sdf.format(new Date());

        for (Map<String, Object> item : historyList) {
            Integer assignmentId = (Integer) item.get("assignmentId");
            String fullTargetName = (String) item.get("fullTargetName");
            String dutyName = (String) item.get("dutyName");
            String actualStartTime = (String) item.get("actualStartTime");
            String campEndTime = (String) item.get("campEndTime");
            String quitTime = (String) item.get("quitTime");

            Long rawCampEndTime = item.get("rawCampEndTime") != null ?
                    ((Number) item.get("rawCampEndTime")).longValue() : null;

            // 判断营期是否结束
            boolean isCampEnd = rawCampEndTime != null ?
                    System.currentTimeMillis() > rawCampEndTime * 1000 : false;

            // 判断是否主动退出
            boolean isQuit = !"未设置".equals(quitTime);

            // 最终状态：必须同时满足【营期未结束 + 未退出】才是正在参与
            boolean isActive = !isCampEnd && !isQuit;

            // 实时更新数据库：如果营期已结束但志愿者未退出，更新数据库字段
            if (isCampEnd && !isQuit) {
                try {
                    // 将营期结束时间写入数据库
                    int updateResult = userMapper.updateVolunteerEndTimeToCampEnd(assignmentId, userId, campEndTime);
                    if (updateResult > 0) {
                        System.out.println("✅ 实时更新志愿者服务结束时间成功：assignmentId=" + assignmentId + ", campEndTime=" + campEndTime);
                    }
                } catch (Exception e) {
                    System.err.println("❌ 更新志愿者服务结束时间失败：assignmentId=" + assignmentId + ", error=" + e.getMessage());
                }
            }

            // 服务结束时间优先级：主动退出时间 > 营期结束时间 > 当前日期（正在参与）
            String endTime = "";
            if (isActive) {
                endTime = currentDate;
            } else if (isQuit) {
                endTime = quitTime;
            } else if (isCampEnd) {
                endTime = campEndTime;
            } else {
                endTime = currentDate;
            }

            // 在时间拼接处，只显示日期部分（去掉时分秒）
            String displayStartTime = actualStartTime.substring(0, 10); // 只取前10个字符 "2026.02.27"
            String displayEndTime = endTime.substring(0, 10); // 只取前10个字符 "2026.02.27"

            // 时间拼接兜底
            String serviceTime = "未设置";
            if (!"未设置".equals(actualStartTime) && !endTime.isEmpty()) {
                serviceTime = displayStartTime + "-" + displayEndTime;
            }

            com.daily.dailychineseculture.dto.VolunteerHistoryDTO.VolunteerHistoryItem historyItem =
                    new com.daily.dailychineseculture.dto.VolunteerHistoryDTO.VolunteerHistoryItem();
            historyItem.setAssignmentId(assignmentId);
            historyItem.setResponsible(fullTargetName != null ? fullTargetName : "无具体负责对象");
            historyItem.setDuty(dutyName != null ? dutyName : "志愿者");
            historyItem.setServiceTime(serviceTime);
            historyItem.setStatus(isActive ? "正在参与" : "已结束");

            result.getVolunteerHistory().add(historyItem);
        }

        return result;
    }

    /**
     * 退出担当（更新志愿者服务结束时间）
     */
    public boolean quitVolunteerDuty(Long userId, Integer assignmentId) {
        // 检查用户是否有该职责任命
        int exists = userMapper.checkAssignmentExists(assignmentId, userId);
        if (exists == 0) {
            return false; // 职责任命不存在或不属于该用户
        }

        // 更新服务结束时间
        int result = userMapper.updateVolunteerEndTime(assignmentId, userId);
        return result > 0;
    }

    /**
     * 获取志愿者统计信息
     */
    public VolunteerStatsDTO getVolunteerStats(Long userId) {
        VolunteerStatsDTO statsDTO = new VolunteerStatsDTO();

        // 1. 获取参与的营期
        List<Map<String, Object>> campList = userMapper.getVolunteerCamps(userId);
        statsDTO.setEnrollCamps(campList.stream()
                .map(item -> {
                    VolunteerStatsDTO.CampItem campItem = new VolunteerStatsDTO.CampItem();
                    campItem.setCampId((Integer) item.get("campId"));
                    campItem.setCampName((String) item.get("campName"));
                    return campItem;
                })
                .collect(Collectors.toList()));

        // 2. 获取负责的班级
        List<Map<String, Object>> classList = userMapper.getVolunteerClasses(userId);
        statsDTO.setDutyClasses(classList.stream()
                .map(item -> {
                    VolunteerStatsDTO.ClassItem classItem = new VolunteerStatsDTO.ClassItem();
                    classItem.setCampId((Integer) item.get("campId"));
                    classItem.setCampName((String) item.get("campName"));
                    classItem.setClassId((Integer) item.get("classId"));
                    classItem.setClassName((String) item.get("className"));
                    return classItem;
                })
                .collect(Collectors.toList()));

        // 3. 获取负责的大组（包含继承）
        List<Map<String, Object>> bigGroupList = userMapper.getVolunteerBigGroups(userId);
        statsDTO.setDutyBigGroups(bigGroupList.stream()
                .map(item -> {
                    VolunteerStatsDTO.BigGroupItem bigGroupItem = new VolunteerStatsDTO.BigGroupItem();
                    bigGroupItem.setCampId((Integer) item.get("campId"));
                    bigGroupItem.setCampName((String) item.get("campName"));
                    bigGroupItem.setClassId((Integer) item.get("classId"));
                    bigGroupItem.setClassName((String) item.get("className"));
                    bigGroupItem.setBigGroupId((Integer) item.get("bigGroupId"));
                    bigGroupItem.setBigGroupName((String) item.get("bigGroupName"));
                    return bigGroupItem;
                })
                .collect(Collectors.toList()));

        // 4. 获取负责的小组（包含继承）
        List<Map<String, Object>> smallGroupList = userMapper.getVolunteerSmallGroups(userId);
        statsDTO.setDutySmallGroups(smallGroupList.stream()
                .map(item -> {
                    VolunteerStatsDTO.SmallGroupItem smallGroupItem = new VolunteerStatsDTO.SmallGroupItem();
                    smallGroupItem.setCampId((Integer) item.get("campId"));
                    smallGroupItem.setCampName((String) item.get("campName"));
                    smallGroupItem.setClassId((Integer) item.get("classId"));
                    smallGroupItem.setClassName((String) item.get("className"));
                    smallGroupItem.setBigGroupId((Integer) item.get("bigGroupId"));
                    smallGroupItem.setBigGroupName((String) item.get("bigGroupName"));
                    smallGroupItem.setSmallGroupId((Integer) item.get("smallGroupId"));
                    smallGroupItem.setSmallGroupName((String) item.get("smallGroupName"));
                    return smallGroupItem;
                })
                .collect(Collectors.toList()));

        return statsDTO;
    }

    // ========== 以下是新增的分班相关方法（完全对齐队友代码风格） ==========

    /**
     * 查询指定课程下审核通过的未分班学员
     * @param campId 课程/营期ID
     * @return 待分班学员列表
     */
    public List<User> getAuditPassStudents(Long campId) {
        try {
            System.out.println("开始查询待分班学员，campId：" + campId);
            List<User> students = userMapper.selectAuditPassStudents(campId);
            System.out.println("查询到待分班学员数量：" + (students != null ? students.size() : 0));
            return students;
        } catch (Exception e) {
            System.err.println("=== 查询待分班学员异常详情 ===");
            System.err.println("campId: " + campId);
            System.err.println("异常类型: " + e.getClass().getSimpleName());
            System.err.println("异常信息: " + e.getMessage());
            System.err.println("异常堆栈:");
            e.printStackTrace();
            System.err.println("=====================");
            return new ArrayList<>();
        }
    }

    /**
     * 自动分班核心方法（地域优先 + 年龄/职业平衡）
     * @param campId 课程/营期ID
     * @param perClassNum 每班人数上限
     * @return 分班后的学员列表
     */
    @Transactional(rollbackFor = Exception.class) // 事务保证分班操作原子性
    public List<User> autoAssignClass(Long campId, Integer perClassNum) {
        try {
            System.out.println("开始自动分班，campId：" + campId + "，每班人数：" + perClassNum);

            // 1. 获取待分班学员
            List<User> students = getAuditPassStudents(campId);
            if (students.isEmpty()) {
                System.out.println("暂无待分班学员，campId：" + campId);
                return new ArrayList<>();
            }

            // 2. 按地域分组（同地域学员优先分到一起）
            Map<String, List<User>> areaGroup = students.stream()
                    .collect(Collectors.groupingBy(User::getRegion, Collectors.toList()));

            // 3. 计算需要分的班级数量
            int total = students.size();
            int classCount = (int) Math.ceil((double) total / perClassNum);
            System.out.println("总学员数：" + total + "，班级数量：" + classCount);

            // 4. 初始化班级列表
            List<List<User>> classList = new ArrayList<>();
            for (int i = 0; i < classCount; i++) {
                classList.add(new ArrayList<>());
            }

            // 5. 同地域学员优先分配，保证每班人数均分
            for (Map.Entry<String, List<User>> entry : areaGroup.entrySet()) {
                String region = entry.getKey();
                List<User> areaStudents = entry.getValue();
                System.out.println("地域[" + region + "]学员数：" + areaStudents.size());

                for (User student : areaStudents) {
                    // 找到当前人数最少的班级
                    List<User> targetClass = classList.stream()
                            .min(Comparator.comparingInt(List::size))
                            .orElse(classList.get(0));
                    targetClass.add(student);
                }
            }

            // 6. 平衡每个班级的年龄/职业分布
            balanceAgeAndCareer(classList);

            // 7. 给学员分配班级ID并更新数据库
            for (int i = 0; i < classList.size(); i++) {
                long classId = i + 1; // 班级ID从1开始
                List<User> currentClass = classList.get(i);
                System.out.println("班级[" + classId + "]分配学员数：" + currentClass.size());

                for (User student : currentClass) {
                    // 直接调用Mapper更新数据库
                    int updateResult = userMapper.updateEnrollmentClassId(student.getUserId(), campId, classId);
                    if (updateResult > 0) {
                        System.out.println("学员[" + student.getUserId() + "]分班成功，班级ID：" + classId);
                    } else {
                        System.err.println("学员[" + student.getUserId() + "]分班更新失败");
                    }
                }
            }

            System.out.println("自动分班完成，campId：" + campId);
            return students;
        } catch (Exception e) {
            System.err.println("=== 自动分班异常详情 ===");
            System.err.println("campId: " + campId);
            System.err.println("perClassNum: " + perClassNum);
            System.err.println("异常类型: " + e.getClass().getSimpleName());
            System.err.println("异常信息: " + e.getMessage());
            System.err.println("异常堆栈:");
            e.printStackTrace();
            System.err.println("=====================");
            throw e; // 抛出异常触发事务回滚
        }
    }

    /**
     * 平衡班级分布（临时修复：移除年龄依赖，仅做基础人数平衡）
     * @param classList 分班后的班级列表
     */
    private void balanceAgeAndCareer(List<List<User>> classList) {
        try {
            System.out.println("开始平衡班级分布（仅做基础人数平衡）");

            // 核心逻辑：确保每个班级人数差不超过1（避免个别班级人数过多）
            int maxClassSize = 0;
            int minClassSize = Integer.MAX_VALUE;

            // 1. 统计最大/最小班级人数
            for (List<User> cls : classList) {
                int size = cls.size();
                if (size > maxClassSize) maxClassSize = size;
                if (size < minClassSize) minClassSize = size;
            }

            // 2. 若人数差超过1，调整（从人数最多的班级转移学员到最少的）
            if (maxClassSize - minClassSize > 1) {
                // 找到人数最多的班级
                List<User> largestClass = classList.stream()
                        .max(Comparator.comparingInt(List::size))
                        .orElse(null);
                // 找到人数最少的班级
                List<User> smallestClass = classList.stream()
                        .min(Comparator.comparingInt(List::size))
                        .orElse(null);

                if (largestClass != null && smallestClass != null && !largestClass.isEmpty()) {
                    // 转移1名学员（用迭代器避免并发修改）
                    Iterator<User> it = largestClass.iterator();
                    if (it.hasNext()) {
                        User transferUser = it.next();
                        it.remove(); // 用迭代器删除，避免ConcurrentModificationException
                        smallestClass.add(transferUser);
                        System.out.println("转移学员[" + transferUser.getUserId() + "]到人数最少的班级，平衡人数");
                    }
                }
            }

            System.out.println("班级分布平衡完成");
        } catch (Exception e) {
            System.err.println("班级分布平衡异常：" + e.getMessage());
            // 即使平衡失败，也不影响核心分班逻辑
        }
    }
}