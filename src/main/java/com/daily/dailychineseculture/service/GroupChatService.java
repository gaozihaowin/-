package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.GroupChatDTO;
import com.daily.dailychineseculture.dto.GroupChatMemberDTO;
import com.daily.dailychineseculture.dto.MessageDTO;
import java.util.List;
import java.util.Map;

/**
 * 群聊服务接口
 */
public interface GroupChatService {

    /**
     * 为班级创建群聊
     * @param campId 营期ID
     * @param classId 班级ID
     * @param className 班级名称
     * @param userId 用户ID
     * @return 创建结果
     */
    Result createClassGroupChat(Integer campId, Integer classId, String className, Long userId);

    /**
     * 为大组创建群聊
     * @param campId 营期ID
     * @param classId 班级ID
     * @param bigGroupId 大组ID
     * @param bigGroupName 大组名称
     * @param userId 用户ID
     * @return 创建结果
     */
    Result createBigGroupChat(Integer campId, Integer classId, Integer bigGroupId, String bigGroupName, Long userId);

    /**
     * 为小组创建群聊
     * @param campId 营期ID
     * @param classId 班级ID
     * @param bigGroupId 大组ID
     * @param smallGroupId 小组ID
     * @param smallGroupName 小组名称
     * @param userId 用户ID
     * @return 创建结果
     */
    Result createSmallGroupChat(Integer campId, Integer classId, Integer bigGroupId, Integer smallGroupId, String smallGroupName, Long userId);

    /**
     * 获取群聊成员
     * @param chatId 群聊ID
     * @return 成员列表
     */
    Result<List<GroupChatMemberDTO>> getGroupMembers(Integer chatId);

    /**
     * 添加群聊成员
     * @param chatId 群聊ID
     * @param userId 用户ID
     * @param role 角色
     * @param currentUserId 当前用户ID
     * @return 添加结果
     */
    Result addGroupMember(Integer chatId, Long userId, String role, Long currentUserId);

    /**
     * 移除群成员
     * @param chatId 群聊ID
     * @param userId 用户ID
     * @param currentUserId 当前用户ID
     * @return 操作结果
     */
    Result removeGroupMember(Integer chatId, Long userId, Long currentUserId);

    /**
     * 更新群成员角色
     * @param chatId 群聊ID
     * @param userId 用户ID
     * @param role 角色
     * @param currentUserId 当前用户ID
     * @return 操作结果
     */
    Result updateMemberRole(Integer chatId, Long userId, String role, Long currentUserId);

    /**
     * 发送消息
     * @param chatId 群聊ID
     * @param senderId 发送者ID
     * @param content 消息内容
     * @param recipientType 接收者类型
     * @param recipientId 接收者ID
     * @return 发送结果
     */
    Result sendMessage(Integer chatId, Long senderId, String content, String recipientType, Long recipientId);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result markMessageAsRead(Integer messageId, Long userId);

    /**
     * 标记消息为已读
     * @param chatId 群聊ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result markGroupMessageAsRead(Integer chatId, Long userId);

    /**
     * 获取群聊消息
     * @param chatId 群聊ID
     * @param userId 用户ID
     * @param limit 限制条数
     * @param offset 偏移量
     * @return 消息列表
     */
    Result<List<MessageDTO>> getGroupMessages(Integer chatId, Long userId, Integer limit, Integer offset);

    /**
     * 获取用户参与的群聊
     * @param userId 用户ID
     * @param limit 限制条数
     * @param offset 偏移量
     * @return 群聊列表
     */
    Result<List<GroupChatDTO>> getUserGroupChats(Long userId, Integer limit, Integer offset);

    /**
     * 更新群聊信息
     * @param chatId 群聊ID
     * @param name 群聊名称
     * @param content 群聊内容
     * @param userId 用户ID
     * @return 更新结果
     */
    Result updateGroupInfo(Integer chatId, String name, String content, Long userId);

    /**
     * 删除群聊
     * @param chatId 群聊ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Result deleteGroupChat(Integer chatId, Long userId);

    /**
     * 获取群聊信息
     * @param chatId 群聊ID
     * @return 群聊信息
     */
    Result<GroupChatDTO> getGroupInfo(Integer chatId);

    /**
     * 自动为营期创建群聊并分配成员（包括班级、大组、小组）
     * @param campId 营期ID
     * @param targetId 目标ID（班级ID、大组ID或小组ID）
     * @param dutyType 岗位类型
     * @param userId 用户ID
     * @return 创建结果
     */
    Result autoCreateGroupChats(Integer campId, Integer targetId, String dutyType, Long userId);

    /**
     * 根据管理范围获取群聊列表
     * @param userId 用户ID
     * @param dutyType 岗位类型
     * @param targetId 目标ID
     * @return 群聊列表
     */
    Result<List<GroupChatDTO>> getGroupChatsByScope(Long userId, String dutyType, Integer targetId);

    /**
     * 为班级创建所有群聊（班级群、大组群、小组群）
     * @param campId 营期ID
     * @param classId 班级ID
     * @param className 班级名称
     * @param userId 用户ID
     * @return 创建结果
     */
    Result createClassAllGroupChats(Integer campId, Integer classId, String className, Long userId);

    /**
     * 获取群聊未读消息数量
     * @param chatId 群聊ID
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Result<Integer> getUnreadMessageCount(Integer chatId, Long userId);

    /**
     * 检查是否存在班级群
     * @param classId 班级ID
     * @return 是否存在
     */
    boolean checkClassGroupChatExists(Integer classId);

    /**
     * 检查是否存在大组群
     * @param bigGroupId 大组ID
     * @return 是否存在
     */
    boolean checkBigGroupChatExists(Integer bigGroupId);

    /**
     * 检查是否存在小组群
     * @param smallGroupId 小组ID
     * @return 是否存在
     */
    boolean checkSmallGroupChatExists(Integer smallGroupId);

    /**
     * 获取可添加的群成员列表
     * @param chatId 群聊ID
     * @param userId 当前用户ID
     * @return 可添加的成员列表
     */
    Result<Map<String, Object>> getAvailableMembers(Integer chatId, Long userId);

    /**
     * 批量添加群成员
     * @param chatId 群聊ID
     * @param userIds 用户ID列表
     * @param role 角色
     * @param currentUserId 当前用户ID
     * @return 添加结果
     */
    Result batchAddGroupMembers(Integer chatId, List<Long> userIds, String role, Long currentUserId);

}