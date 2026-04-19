package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.GroupChatDTO;
import com.daily.dailychineseculture.dto.GroupChatMemberDTO;
import com.daily.dailychineseculture.dto.MessageDTO;
import com.daily.dailychineseculture.entity.GroupChat;
import com.daily.dailychineseculture.mapper.GroupChatMapper;
import com.daily.dailychineseculture.mapper.ClassMapper;
import com.daily.dailychineseculture.mapper.VolunteerManageMapper;
import com.daily.dailychineseculture.service.GroupChatService;
import com.daily.dailychineseculture.websocket.GroupChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Service
public class GroupChatServiceImpl implements GroupChatService {

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private VolunteerManageMapper volunteerManageMapper;

    @Autowired
    private GroupChatWebSocketHandler webSocketHandler;

    @Override
    public Result<Map<String, Object>> getAvailableMembers(Integer chatId, Long userId) {
        try {
            // 检查当前用户是否是群聊管理员
            if (!groupChatMapper.isGroupAdmin(chatId, userId)) {
                return Result.error("无权限，只有管理员可以查看可添加成员");
            }

            // 获取群聊信息
            Map<String, Object> groupInfo = groupChatMapper.getGroupChatInfo(chatId);
            if (groupInfo == null) {
                return Result.error("群聊不存在");
            }

            String groupType = (String) groupInfo.get("type");
            Integer classId = (Integer) groupInfo.get("class_id");
            Integer bigGroupId = (Integer) groupInfo.get("big_group_id");
            Integer smallGroupId = (Integer) groupInfo.get("small_group_id");

            // 获取该范围内的所有学员
            List<Map<String, Object>> students = null;
            if ("班级群".equals(groupType)) {
                students = volunteerManageMapper.getClassMembers(classId);
            } else if ("大组群".equals(groupType)) {
                students = volunteerManageMapper.getBigGroupMembers(bigGroupId);
            } else if ("小组群".equals(groupType)) {
                students = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
            }

            // 获取该范围内的所有志愿者
            List<Map<String, Object>> volunteers = new ArrayList<>();
            if ("班级群".equals(groupType)) {
                // 班级群：获取班级、大组、小组的所有志愿者
                volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("class", classId));
                List<Map<String, Object>> bigGroups = volunteerManageMapper.getAssignableBigGroups(classId);
                for (Map<String, Object> bg : bigGroups) {
                    Integer bgId = (Integer) bg.get("targetId");
                    volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("big_group", bgId));
                    List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bgId);
                    for (Map<String, Object> sg : smallGroups) {
                        Integer sgId = (Integer) sg.get("targetId");
                        volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("small_group", sgId));
                    }
                }
            } else if ("大组群".equals(groupType)) {
                // 大组群：获取大组、小组的所有志愿者，以及大组所属班级的所有志愿者
                // 获取大组所属的班级
                Map<String, Object> bigGroupInfo = volunteerManageMapper.getBigGroupInfo(bigGroupId);
                if (bigGroupInfo != null) {
                    Integer bgClassId = (Integer) bigGroupInfo.get("class_id");
                    if (bgClassId != null) {
                        // 获取班级的所有志愿者
                        volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("class", bgClassId));
                    }
                }
                // 获取大组的所有志愿者
                volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("big_group", bigGroupId));
                List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
                for (Map<String, Object> sg : smallGroups) {
                    Integer sgId = (Integer) sg.get("targetId");
                    volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("small_group", sgId));
                }
            } else if ("小组群".equals(groupType)) {
                // 小组群：获取小组的所有志愿者，以及小组所属大组、班级的所有志愿者
                // 获取小组所属的大组和班级
                Map<String, Object> smallGroupInfo = volunteerManageMapper.getSmallGroupInfo(smallGroupId);
                if (smallGroupInfo != null) {
                    Integer sgBigGroupId = (Integer) smallGroupInfo.get("big_group_id");
                    Integer sgClassId = (Integer) smallGroupInfo.get("class_id");
                    if (sgClassId != null) {
                        // 获取班级的所有志愿者
                        volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("class", sgClassId));
                    }
                    if (sgBigGroupId != null) {
                        // 获取大组的所有志愿者
                        volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("big_group", sgBigGroupId));
                    }
                }
                // 获取小组的所有志愿者
                volunteers.addAll(volunteerManageMapper.getCurrentVolunteers("small_group", smallGroupId));
            }

            // 去重处理
            Set<Long> userIds = new HashSet<>();
            List<Map<String, Object>> availableMembers = new ArrayList<>();

            // 处理学员
            if (students != null) {
                for (Map<String, Object> student : students) {
                    Long studentId = ((Number) student.get("userId")).longValue();
                    if (!userIds.contains(studentId)) {
                        userIds.add(studentId);
                        // 检查是否已在群中
                        int exists = groupChatMapper.checkMemberExists(chatId, studentId);
                        student.put("inGroup", exists > 0);
                        student.put("type", "student");
                        availableMembers.add(student);
                    }
                }
            }

            // 处理志愿者
            if (!volunteers.isEmpty()) {
                for (Map<String, Object> volunteer : volunteers) {
                    Long volunteerId = ((Number) volunteer.get("userId")).longValue();
                    if (!userIds.contains(volunteerId)) {
                        userIds.add(volunteerId);
                        // 检查是否已在群中
                        int exists = groupChatMapper.checkMemberExists(chatId, volunteerId);
                        volunteer.put("inGroup", exists > 0);
                        volunteer.put("type", "volunteer");
                        availableMembers.add(volunteer);
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("members", availableMembers);
            result.put("groupInfo", groupInfo);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取可添加成员失败：" + e.getMessage());
        }
    }

    @Override
    public Result batchAddGroupMembers(Integer chatId, List<Long> userIds, String role, Long currentUserId) {
        try {
            // 检查当前用户是否是群聊管理员
            if (!groupChatMapper.isGroupAdmin(chatId, currentUserId)) {
                return Result.error("无权限，只有管理员可以批量添加群成员");
            }

            // 检查群聊是否存在
            Map<String, Object> groupInfo = groupChatMapper.getGroupChatInfo(chatId);
            if (groupInfo == null) {
                return Result.error("群聊不存在");
            }

            // 批量添加成员
            int successCount = 0;
            for (Long userId : userIds) {
                // 检查用户是否已在群中
                if (groupChatMapper.checkMemberExists(chatId, userId) == 0) {
                    int result = groupChatMapper.addGroupMember(chatId, userId, role);
                    if (result > 0) {
                        successCount++;
                    }
                }
            }

            return Result.success("成功添加 " + successCount + " 人");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量添加群成员失败：" + e.getMessage());
        }
    }

    @Override
    public Result createClassGroupChat(Integer campId, Integer classId, String className, Long userId) {
        try {
            if (!checkCreateClassGroupPermission(userId, classId)) {
                return Result.error("无权限创建班级群");
            }
            if (checkClassGroupChatExists(classId)) {
                return Result.success("班级群已存在");
            }

            groupChatMapper.createGroupChat(className, "班级群", "班级群聊", campId, classId, null, null);
            GroupChat groupChat = groupChatMapper.getGroupChatByTypeAndIds("班级群", classId, null, null);
            if (groupChat == null) {
                return Result.error("获取群ID失败");
            }
            Integer chatId = groupChat.getChatId();
            groupChatMapper.updateClassChatId(classId, chatId);
            Set<Long> adminUserIds = new HashSet<>();
            List<Map<String, Object>> classManagers = volunteerManageMapper.getCurrentVolunteers("class", classId);
            if (classManagers != null && !classManagers.isEmpty()) {
                for (Map<String, Object> m : classManagers) {
                    Long uid = ((Number) m.get("userId")).longValue();
                    adminUserIds.add(uid);
                }
            }

            List<Map<String, Object>> bigGroups = volunteerManageMapper.getAssignableBigGroups(classId);
            if (bigGroups != null && !bigGroups.isEmpty()) {
                for (Map<String, Object> bg : bigGroups) {
                    Integer bgId = (Integer) bg.get("targetId");
                    List<Map<String, Object>> bgManagers = volunteerManageMapper.getCurrentVolunteers("big_group",
                            bgId);
                    if (bgManagers != null && !bgManagers.isEmpty()) {
                        for (Map<String, Object> m : bgManagers) {
                            Long uid = ((Number) m.get("userId")).longValue();
                            adminUserIds.add(uid);
                        }
                    }

                    List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bgId);
                    if (smallGroups != null && !smallGroups.isEmpty()) {
                        for (Map<String, Object> sg : smallGroups) {
                            Integer sgId = (Integer) sg.get("targetId");
                            List<Map<String, Object>> sgManagers = volunteerManageMapper
                                    .getCurrentVolunteers("small_group", sgId);
                            if (sgManagers != null && !sgManagers.isEmpty()) {
                                for (Map<String, Object> m : sgManagers) {
                                    Long uid = ((Number) m.get("userId")).longValue();
                                    adminUserIds.add(uid);
                                }
                            }
                        }
                    }
                }
            }

            List<Map<String, Object>> students = volunteerManageMapper.getClassMembers(classId);
            if (students != null && !students.isEmpty()) {
                for (Map<String, Object> student : students) {
                    Object uidObj = student.get("userId");
                    if (uidObj == null)
                        uidObj = student.get("user_id");
                    if (uidObj == null)
                        continue;
                    Long uid = ((Number) uidObj).longValue();
                    if (groupChatMapper.checkMemberExists(chatId, uid) == 0) {
                        String role = adminUserIds.contains(uid) ? "admin" : "member";
                        groupChatMapper.addGroupMember(chatId, uid, role);
                    }
                }
            }

            for (Long adminId : adminUserIds) {
                if (groupChatMapper.checkMemberExists(chatId, adminId) == 0) {
                    groupChatMapper.addGroupMember(chatId, adminId, "admin");
                }
            }

            return Result.success("班级群创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("班级群创建异常：" + e.getMessage());
        }
    }

    @Override
    public Result createBigGroupChat(Integer campId, Integer classId, Integer bigGroupId, String bigGroupName,
            Long userId) {
        try {
            if (!checkCreateBigGroupPermission(userId, bigGroupId, classId)) {
                return Result.error("无权限");
            }
            if (checkBigGroupChatExists(bigGroupId)) {
                return Result.success("已存在");
            }

            groupChatMapper.createGroupChat(bigGroupName, "大组群", "大组群聊", campId, classId, bigGroupId, null);
            GroupChat groupChat = groupChatMapper.getGroupChatByTypeAndIds("大组群", classId, bigGroupId, null);
            if (groupChat == null)
                return Result.error("获取群ID失败");
            Integer chatId = groupChat.getChatId();
            groupChatMapper.updateBigGroupChatId(bigGroupId, chatId);
            Set<Long> adminUserIds = new HashSet<>();
            List<Map<String, Object>> bgManagers = volunteerManageMapper.getCurrentVolunteers("big_group", bigGroupId);
            for (Map<String, Object> m : bgManagers) {
                Long uid = ((Number) m.get("userId")).longValue();
                adminUserIds.add(uid);
            }
            List<Map<String, Object>> classManagers = volunteerManageMapper.getCurrentVolunteers("class", classId);
            for (Map<String, Object> m : classManagers) {
                Long uid = ((Number) m.get("userId")).longValue();
                adminUserIds.add(uid);
            }
            List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
            for (Map<String, Object> sg : smallGroups) {
                Integer sgId = (Integer) sg.get("targetId");
                List<Map<String, Object>> sgManagers = volunteerManageMapper.getCurrentVolunteers("small_group", sgId);
                for (Map<String, Object> m : sgManagers) {
                    Long uid = ((Number) m.get("userId")).longValue();
                    adminUserIds.add(uid);
                }
            }

            List<Map<String, Object>> students = volunteerManageMapper.getBigGroupMembers(bigGroupId);
            if (students != null && !students.isEmpty()) {
                for (Map<String, Object> s : students) {
                    Object uidObj = s.get("userId");
                    if (uidObj == null)
                        continue;
                    Long uid = ((Number) uidObj).longValue();
                    if (groupChatMapper.checkMemberExists(chatId, uid) == 0) {
                        String role = adminUserIds.contains(uid) ? "admin" : "member";
                        groupChatMapper.addGroupMember(chatId, uid, role);
                    }
                }
            }

            for (Long adminId : adminUserIds) {
                if (groupChatMapper.checkMemberExists(chatId, adminId) == 0) {
                    groupChatMapper.addGroupMember(chatId, adminId, "admin");
                }
            }

            return Result.success("大组群创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("大组群创建异常");
        }
    }

    @Override
    public Result createSmallGroupChat(Integer campId, Integer classId, Integer bigGroupId, Integer smallGroupId,
            String smallGroupName, Long userId) {
        try {
            if (!checkCreateSmallGroupPermission(userId, smallGroupId, bigGroupId, classId)) {
                return Result.error("无权限");
            }
            if (checkSmallGroupChatExists(smallGroupId)) {
                return Result.success("已存在");
            }

            if (bigGroupId == null || classId == null) {
                Map<String, Object> info = volunteerManageMapper.getSmallGroupInfo(smallGroupId);
                if (info != null) {
                    bigGroupId = (Integer) info.get("big_group_id");
                    classId = (Integer) info.get("class_id");
                }
            }

            groupChatMapper.createGroupChat(smallGroupName, "小组群", "小组群聊", campId, classId, bigGroupId, smallGroupId);
            GroupChat groupChat = groupChatMapper.getGroupChatByTypeAndIds("小组群", classId, bigGroupId, smallGroupId);
            if (groupChat == null)
                return Result.error("获取群ID失败");
            Integer chatId = groupChat.getChatId();
            groupChatMapper.updateSmallGroupChatId(smallGroupId, chatId);
            Set<Long> adminUserIds = new HashSet<>();
            List<Map<String, Object>> sgManagers = volunteerManageMapper.getCurrentVolunteers("small_group",
                    smallGroupId);
            for (Map<String, Object> m : sgManagers) {
                Long uid = ((Number) m.get("userId")).longValue();
                adminUserIds.add(uid);
            }
            List<Map<String, Object>> bgManagers = volunteerManageMapper.getCurrentVolunteers("big_group", bigGroupId);
            for (Map<String, Object> m : bgManagers) {
                Long uid = ((Number) m.get("userId")).longValue();
                adminUserIds.add(uid);
            }
            List<Map<String, Object>> classManagers = volunteerManageMapper.getCurrentVolunteers("class", classId);
            for (Map<String, Object> m : classManagers) {
                Long uid = ((Number) m.get("userId")).longValue();
                adminUserIds.add(uid);
            }

            List<Map<String, Object>> students = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
            if (students != null && !students.isEmpty()) {
                for (Map<String, Object> s : students) {
                    Object uidObj = s.get("userId");
                    if (uidObj == null)
                        continue;
                    Long uid = ((Number) uidObj).longValue();
                    if (groupChatMapper.checkMemberExists(chatId, uid) == 0) {
                        String role = adminUserIds.contains(uid) ? "admin" : "member";
                        groupChatMapper.addGroupMember(chatId, uid, role);
                    }
                }
            }

            for (Long adminId : adminUserIds) {
                if (groupChatMapper.checkMemberExists(chatId, adminId) == 0) {
                    groupChatMapper.addGroupMember(chatId, adminId, "admin");
                }
            }

            return Result.success("小组群创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("小组群创建异常");
        }
    }

    private void addManagersToChat(Integer chatId, String type, Integer targetId) {
        try {
            if (chatId == null || targetId == null)
                return;
            List<Map<String, Object>> managers = volunteerManageMapper.getCurrentVolunteers(type, targetId);
            if (managers == null || managers.isEmpty())
                return;

            for (Map<String, Object> m : managers) {
                Object uidObj = m.get("userId");
                if (uidObj == null)
                    continue;
                Long uid = ((Number) uidObj).longValue();
                if (groupChatMapper.checkMemberExists(chatId, uid) == 0) {
                    groupChatMapper.addGroupMember(chatId, uid, "admin");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result autoCreateGroupChats(Integer campId, Integer targetId, String dutyType, Long userId) {
        try {
            if ("学班".equals(dutyType) || "检班".equals(dutyType)) {
                Integer classId = targetId;
                String className = classMapper.getClassNameById(classId);
                if (className == null)
                    return Result.error("班级不存在");

                if (!groupChatMapper.existsClassGroup(classId))
                    createClassGroupChat(campId, classId, className, userId);

                List<Map<String, Object>> bigGroups = volunteerManageMapper.getAssignableBigGroups(classId);
                for (Map<String, Object> bg : bigGroups) {
                    Integer bgId = (Integer) bg.get("targetId");
                    String bgName = (String) bg.get("targetName");
                    if (!groupChatMapper.existsBigGroup(bgId))
                        createBigGroupChat(campId, classId, bgId, bgName, userId);

                    List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bgId);
                    for (Map<String, Object> sg : smallGroups) {
                        Integer sgId = (Integer) sg.get("targetId");
                        String sgName = (String) sg.get("targetName");
                        if (!groupChatMapper.existsSmallGroup(sgId))
                            createSmallGroupChat(campId, classId, bgId, sgId, sgName, userId);
                    }
                }
            } else if ("学委".equals(dutyType) || "检委".equals(dutyType)) {
                Integer bgId = targetId;
                Map<String, Object> info = volunteerManageMapper.getBigGroupInfo(bgId);
                if (info == null)
                    return Result.error("大组不存在");
                Integer classId = (Integer) info.get("class_id");
                String name = (String) info.get("bigGroupName");

                if (!groupChatMapper.existsBigGroup(bgId))
                    createBigGroupChat(campId, classId, bgId, name, userId);

                List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bgId);
                for (Map<String, Object> sg : smallGroups) {
                    Integer sgId = (Integer) sg.get("targetId");
                    String sgName = (String) sg.get("targetName");
                    if (!groupChatMapper.existsSmallGroup(sgId))
                        createSmallGroupChat(campId, classId, bgId, sgId, sgName, userId);
                }
            } else if ("学组".equals(dutyType) || "检组".equals(dutyType)) {
                Integer sgId = targetId;
                Map<String, Object> info = volunteerManageMapper.getSmallGroupInfo(sgId);
                if (info == null)
                    return Result.error("小组不存在");
                Integer bgId = (Integer) info.get("big_group_id");
                Integer classId = (Integer) info.get("class_id");
                String name = (String) info.get("smallGroupName");

                if (!groupChatMapper.existsSmallGroup(sgId))
                    createSmallGroupChat(campId, classId, bgId, sgId, name, userId);
            }

            return Result.success("群已创建完成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success("群已创建完成");
        }
    }

    @Override
    public Result<List<GroupChatDTO>> getGroupChatsByScope(Long userId, String dutyType, Integer targetId) {
        try {
            List<GroupChatDTO> list = groupChatMapper.getGroupChatsByScope(userId, dutyType, targetId);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(List.of());
        }
    }

    @Override
    public Result<List<GroupChatMemberDTO>> getGroupMembers(Integer chatId) {
        return Result.success(groupChatMapper.getGroupMembers(chatId));
    }

    @Override
    public Result addGroupMember(Integer chatId, Long userId, String role, Long currentUserId) {
        if (!groupChatMapper.isGroupAdmin(chatId, currentUserId)) {
            return Result.error("无权限，只有管理员可以添加群成员");
        }
        if (groupChatMapper.checkMemberExists(chatId, userId) > 0)
            return Result.error("已存在");
        groupChatMapper.addGroupMember(chatId, userId, role);
        return Result.success("添加成功");
    }

    @Override
    public Result removeGroupMember(Integer chatId, Long userId, Long currentUserId) {
        if (!groupChatMapper.isGroupAdmin(chatId, currentUserId)) {
            return Result.error("无权限，只有管理员可以移除群成员");
        }
        groupChatMapper.removeGroupMember(chatId, userId);
        return Result.success("移除成功");
    }

    @Override
    public Result updateMemberRole(Integer chatId, Long userId, String role, Long currentUserId) {
        if (!groupChatMapper.isGroupAdmin(chatId, currentUserId)) {
            return Result.error("无权限，只有管理员可以更新群成员角色");
        }
        groupChatMapper.updateMemberRole(chatId, userId, role);
        return Result.success("更新成功");
    }

    @Override
    public Result sendMessage(Integer chatId, Long senderId, String content, String messageType,
                              String voiceUrl, Integer voiceDuration, String recipientType, Long recipientId) {

        if ("voice".equals(messageType) && voiceDuration != null && voiceDuration > 60) {
            return Result.error("语音最长60秒");
        }

        String realType = "all".equals(recipientType) ? "group" : "private";
        Long recv = "group".equals(realType) ? null : recipientId;

        groupChatMapper.sendMessage(chatId, senderId, recv, content, messageType, voiceUrl, voiceDuration);

        try {
            List<MessageDTO> messages = groupChatMapper.getGroupMessages(chatId, senderId, 1, 0);
            if (!messages.isEmpty()) {
                MessageDTO message = messages.get(0);
                Map<String, Object> response = new HashMap<>();
                response.put("type", "message");
                response.put("message", message);

                if ("group".equals(realType)) {
                    webSocketHandler.broadcastMessage(chatId, response);
                } else {
                    webSocketHandler.sendToUser(recipientId, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.success("发送成功");
    }

    @Override
    public Result getMessageHistory(Integer chatId, Long userId, Integer page, Integer limit) {
        int offset = (page - 1) * limit;
        List<MessageDTO> messages = groupChatMapper.getGroupMessages(chatId, userId, limit, offset);
        return Result.success(messages);
    }

    @Override
    public Result<List<MessageDTO>> getGroupMessages(Integer chatId, Long userId, Integer limit, Integer offset) {
        return Result.success(groupChatMapper.getGroupMessages(chatId, userId, limit, offset));
    }

    @Override
    public Result<List<GroupChatDTO>> getUserGroupChats(Long userId, Integer limit, Integer offset) {
        return Result.success(groupChatMapper.getUserGroupChats(userId, limit, offset));
    }

    @Override
    public Result updateGroupInfo(Integer chatId, String name, String content, Long userId) {
        groupChatMapper.updateGroupInfo(chatId, name, content);
        return Result.success("更新成功");
    }

    @Override
    public Result<GroupChatDTO> getGroupInfo(Integer chatId) {
        return Result.success(groupChatMapper.getGroupInfo(chatId));
    }

    @Override
    public Result createClassAllGroupChats(Integer campId, Integer classId, String className, Long userId) {
        return autoCreateGroupChats(campId, classId, "学班", userId);
    }

    @Override
    public Result<Integer> getUnreadMessageCount(Integer chatId, Long userId) {
        try {
            int count = groupChatMapper.getUnreadMessageCount(chatId, userId);
            return Result.success(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取未读消息数失败");
        }
    }

    @Override
    public Result markAllMessagesAsRead(Integer chatId, Long userId) {
        try {
            int result = groupChatMapper.markGroupMessageAsRead(chatId, userId);
            if (result > 0) {
                return Result.success("标记所有消息已读成功");
            } else {
                return Result.success("没有未读消息");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记已读失败");
        }
    }

    @Override
    public Result markMessageAsRead(Integer messageId, Long userId) {
        try {
            int result = groupChatMapper.markMessageAsRead(messageId, userId);
            if (result > 0) {
                return Result.success("标记已读成功");
            } else {
                return Result.error("标记已读失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记已读失败");
        }
    }

    @Override
    public boolean checkClassGroupChatExists(Integer classId) {
        return groupChatMapper.existsClassGroup(classId);
    }

    @Override
    public boolean checkBigGroupChatExists(Integer bigGroupId) {
        return groupChatMapper.existsBigGroup(bigGroupId);
    }

    @Override
    public boolean checkSmallGroupChatExists(Integer smallGroupId) {
        return groupChatMapper.existsSmallGroup(smallGroupId);
    }

    public boolean checkMemberManagePermission(Long userId) {
        return true;
    }

    public boolean checkGroupManagePermission(Long userId, Integer chatId) {
        return true;
    }

    public boolean checkCreateClassGroupPermission(Long userId, Integer classId) {
        return true;
    }

    public boolean checkCreateBigGroupPermission(Long userId, Integer bigGroupId, Integer classId) {
        return true;
    }

    public boolean checkCreateSmallGroupPermission(Long userId, Integer smallGroupId, Integer bigGroupId,
            Integer classId) {
        return true;
    }
    @Override
    public Result revokeMessage(Integer messageId, Long userId) {
        // 调用mapper方法撤回消息
        int result = groupChatMapper.revokeMessage(messageId, userId);
        if (result > 0) {
            return Result.success("撤回成功");
        } else {
            return Result.error("撤回失败，可能无权限或消息不存在");
        }
    }
}