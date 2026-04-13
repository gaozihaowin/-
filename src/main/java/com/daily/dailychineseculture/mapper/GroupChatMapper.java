package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.GroupChatDTO;
import com.daily.dailychineseculture.dto.GroupChatMemberDTO;
import com.daily.dailychineseculture.dto.MessageDTO;
import com.daily.dailychineseculture.entity.GroupChat;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupChatMapper {

    @Insert("INSERT INTO t_group_chat(name, type, content, camp_id, class_id, big_group_id, small_group_id, create_time) " +
            "VALUES(#{name}, #{type}, #{content}, #{campId}, #{classId}, #{bigGroupId}, #{smallGroupId}, NOW())")
    int createGroupChat(@Param("name") String name,
                        @Param("type") String type,
                        @Param("content") String content,
                        @Param("campId") Integer campId,
                        @Param("classId") Integer classId,
                        @Param("bigGroupId") Integer bigGroupId,
                        @Param("smallGroupId") Integer smallGroupId);

    @Select("<script>" +
            "SELECT * FROM t_group_chat " +
            "WHERE type = #{type} " +
            "AND class_id = #{classId} " +
            "<if test='bigGroupId != null'>AND big_group_id = #{bigGroupId} </if>" +
            "<if test='bigGroupId == null'>AND big_group_id IS NULL </if>" +
            "<if test='smallGroupId != null'>AND small_group_id = #{smallGroupId} </if>" +
            "<if test='smallGroupId == null'>AND small_group_id IS NULL </if>" +
            "</script>")
    GroupChat getGroupChatByTypeAndIds(@Param("type") String type,
                                       @Param("classId") Integer classId,
                                       @Param("bigGroupId") Integer bigGroupId,
                                       @Param("smallGroupId") Integer smallGroupId);

    @Insert("INSERT INTO t_group_chat_member(chat_id, user_id, role, join_time) " +
            "VALUES(#{chatId}, #{userId}, #{role}, NOW())")
    int addGroupMember(@Param("chatId") Integer chatId,
                       @Param("userId") Long userId,
                       @Param("role") String role);

    @Select("SELECT gm.chat_id, gm.user_id, COALESCE(u.nickname, u.account) as username, u.avatar, u.phone, gm.role, gm.join_time " +
            "FROM t_group_chat_member gm " +
            "LEFT JOIN t_user u ON gm.user_id = u.user_id " +
            "WHERE gm.chat_id = #{chatId}")
    List<GroupChatMemberDTO> getGroupMembers(@Param("chatId") Integer chatId);

    @Select("SELECT gm.chat_id, gm.user_id, COALESCE(u.nickname, u.account) as username, u.avatar, u.phone, gm.role, gm.join_time " +
            "FROM t_group_chat_member gm " +
            "LEFT JOIN t_user u ON gm.user_id = u.user_id " +
            "WHERE gm.chat_id = #{chatId} AND gm.role = 'admin'")
    List<Map<String, Object>> getGroupAdmins(@Param("chatId") Integer chatId);

    @Update("UPDATE t_group_chat_member SET role = #{role} WHERE chat_id = #{chatId} AND user_id = #{userId}")
    int updateMemberRole(@Param("chatId") Integer chatId,
                         @Param("userId") Long userId,
                         @Param("role") String role);

    @Insert("INSERT INTO t_chat_message(chat_id, sender_id, receiver_id, content, send_time, status) " +
            "VALUES(#{chatId}, #{senderId}, #{receiverId}, #{content}, NOW(), 0)")
    int sendMessage(@Param("chatId") Integer chatId,
                    @Param("senderId") Long senderId,
                    @Param("receiverId") Long receiverId,
                    @Param("content") String content);

    @Update("UPDATE t_chat_message SET status = 1 WHERE message_id = #{messageId} AND receiver_id = #{userId}")
    int markMessageAsRead(@Param("messageId") Integer messageId,
                          @Param("userId") Long userId);

    @Update("UPDATE t_chat_message SET status = 1 WHERE chat_id = #{chatId} AND receiver_id IS NULL AND sender_id != #{userId}")
    int markGroupMessageAsRead(@Param("chatId") Integer chatId,
                               @Param("userId") Long userId);

    @Select("SELECT m.message_id, m.chat_id, m.sender_id, COALESCE(u.nickname, u.account) as senderName, m.content, m.send_time, m.receiver_id " +
            "FROM t_chat_message m " +
            "LEFT JOIN t_user u ON m.sender_id = u.user_id " +
            "WHERE m.chat_id = #{chatId} " +
            "AND (m.receiver_id IS NULL OR m.receiver_id = #{userId} OR m.sender_id = #{userId}) " +
            "ORDER BY m.send_time ASC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<MessageDTO> getGroupMessages(@Param("chatId") Integer chatId,
                                      @Param("userId") Long userId,
                                      @Param("limit") Integer limit,
                                      @Param("offset") Integer offset);

    @Select("SELECT gc.chat_id, gc.name, gc.type, gc.content, gc.camp_id, gc.class_id, gc.big_group_id, gc.small_group_id, gc.create_time " +
            "FROM t_group_chat gc " +
            "JOIN t_group_chat_member gcm ON gc.chat_id = gcm.chat_id " +
            "WHERE gcm.user_id = #{userId} " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<GroupChatDTO> getUserGroupChats(@Param("userId") Long userId,
                                         @Param("limit") Integer limit,
                                         @Param("offset") Integer offset);

    @Select("SELECT COUNT(*) FROM t_group_chat_member WHERE chat_id = #{chatId} AND user_id = #{userId}")
    int checkMemberExists(@Param("chatId") Integer chatId,
                          @Param("userId") Long userId);

    @Delete("DELETE FROM t_group_chat_member WHERE chat_id = #{chatId} AND user_id = #{userId}")
    int removeGroupMember(@Param("chatId") Integer chatId,
                          @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM t_group_chat_member WHERE user_id = #{userId} AND role = 'admin'")
    Integer countChatAdminByUserId(@Param("userId") Long userId);

    @Select("SELECT chat_id FROM t_group_chat_member WHERE user_id = #{userId} AND role = 'admin'")
    List<Map<String, Object>> listChatAdminByUserId(@Param("userId") Long userId);

    @Update("UPDATE t_group_chat SET name = #{name}, content = #{content} WHERE chat_id = #{chatId}")
    int updateGroupInfo(@Param("chatId") Integer chatId,
                        @Param("name") String name,
                        @Param("content") String content);

    @Delete("DELETE FROM t_group_chat WHERE chat_id = #{chatId}")
    int deleteGroupChat(@Param("chatId") Integer chatId);

    @Select("SELECT chat_id, name, type, content, camp_id, class_id, big_group_id, small_group_id, create_time " +
            "FROM t_group_chat WHERE chat_id = #{chatId}")
    GroupChatDTO getGroupInfo(@Param("chatId") Integer chatId);

    // ==============================
    // 🔴 最终修复：群可见范围
    // 学班/检班：看本班所有群
    // 学委/检委：看本大组+班级群
    // 学组/检组：看本小组+大组群+班级群
    // ==============================
    @Select("SELECT " +
            "gc.chat_id, gc.name, gc.type, gc.content, gc.camp_id, " +
            "gc.class_id, gc.big_group_id, gc.small_group_id, gc.create_time, " +
            "(SELECT COUNT(*) FROM t_chat_message m WHERE m.chat_id = gc.chat_id AND m.receiver_id = #{userId} AND m.status = 0) as unreadCount " +
            "FROM t_group_chat gc " +
            "WHERE 1=1 " +
            "AND ( " +
            "   ( #{dutyType} IN ('学班','检班') AND gc.class_id = #{targetId} ) " +
            "   OR " +
            "   ( #{dutyType} IN ('学委','检委') AND ( " +
            "       gc.big_group_id = #{targetId} " +
            "       OR (gc.class_id = (SELECT class_id FROM t_big_group WHERE big_group_id = #{targetId}) AND gc.type = '班级群') " +
            "   )) " +
            "   OR " +
            "   ( #{dutyType} IN ('学组','检组') AND ( " +
            "       gc.small_group_id = #{targetId} " +
            "       OR (gc.big_group_id = (SELECT big_group_id FROM t_small_group WHERE small_group_id = #{targetId}) AND gc.type = '大组群') " +
            "       OR (gc.class_id = (SELECT class_id FROM t_small_group WHERE small_group_id = #{targetId}) AND gc.type = '班级群') " +
            "   )) " +
            ") " +
            "ORDER BY gc.create_time DESC")
    List<GroupChatDTO> getGroupChatsByScope(@Param("userId") Long userId,
                                            @Param("dutyType") String dutyType,
                                            @Param("targetId") Integer targetId);

    @Select("SELECT COUNT(*) FROM t_chat_message WHERE chat_id = #{chatId} AND receiver_id = #{userId} AND status = 0")
    int getUnreadMessageCount(@Param("chatId") Integer chatId, @Param("userId") Long userId);

    @Select("SELECT * FROM t_group_chat WHERE type = '班级群' AND class_id = #{classId}")
    GroupChat getClassGroupChat(@Param("classId") Integer classId);

    @Select("SELECT * FROM t_group_chat WHERE type = '大组群' AND big_group_id = #{bigGroupId}")
    GroupChat getBigGroupChat(@Param("bigGroupId") Integer bigGroupId);

    @Select("SELECT * FROM t_group_chat WHERE type = '小组群' AND small_group_id = #{smallGroupId}")
    GroupChat getSmallGroupChat(@Param("smallGroupId") Integer smallGroupId);

    @Select("SELECT gc.chat_id, gc.name, gc.type, gc.content, gc.camp_id, gc.class_id, gc.big_group_id, gc.small_group_id, gc.create_time " +
            "FROM t_group_chat gc " +
            "WHERE gc.class_id = #{classId}")
    List<GroupChat> getClassGroupChats(@Param("classId") Integer classId);

    @Select("SELECT gc.chat_id, gc.name, gc.type, gc.content, gc.camp_id, gc.class_id, gc.big_group_id, gc.small_group_id, gc.create_time " +
            "FROM t_group_chat gc " +
            "WHERE gc.big_group_id = #{bigGroupId}")
    List<GroupChat> getBigGroupChats(@Param("bigGroupId") Integer bigGroupId);

    @Select("SELECT gc.chat_id, gc.name, gc.type, gc.content, gc.camp_id, gc.class_id, gc.big_group_id, gc.small_group_id, gc.create_time " +
            "FROM t_group_chat gc " +
            "WHERE gc.small_group_id = #{smallGroupId}")
    List<GroupChat> getSmallGroupChats(@Param("smallGroupId") Integer smallGroupId);

    @Select("SELECT COUNT(*) > 0 FROM t_group_chat WHERE type = '班级群' AND class_id = #{classId}")
    boolean existsClassGroup(@Param("classId") Integer classId);

    @Select("SELECT COUNT(*) > 0 FROM t_group_chat WHERE type = '大组群' AND big_group_id = #{bigGroupId}")
    boolean existsBigGroup(@Param("bigGroupId") Integer bigGroupId);

    @Select("SELECT COUNT(*) > 0 FROM t_group_chat WHERE type = '小组群' AND small_group_id = #{smallGroupId}")
    boolean existsSmallGroup(@Param("smallGroupId") Integer smallGroupId);

    @Select("SELECT COUNT(*) > 0 FROM t_group_chat_member WHERE chat_id = #{chatId} AND user_id = #{userId} AND role = 'admin'")
    boolean isGroupAdmin(@Param("chatId") Integer chatId, @Param("userId") Long userId);

    @Select("SELECT chat_id, name, type, content, camp_id, class_id, big_group_id, small_group_id, create_time " +
            "FROM t_group_chat WHERE chat_id = #{chatId}")
    Map<String, Object> getGroupChatInfo(@Param("chatId") Integer chatId);
}