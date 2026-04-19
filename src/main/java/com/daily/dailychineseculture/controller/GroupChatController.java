package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.GroupChatDTO;
import com.daily.dailychineseculture.dto.GroupChatMemberDTO;
import com.daily.dailychineseculture.dto.GroupChatRequestDTO;
import com.daily.dailychineseculture.dto.MessageDTO;
import com.daily.dailychineseculture.service.GroupChatService;
import com.daily.dailychineseculture.util.JwtUtils;
import com.daily.dailychineseculture.websocket.GroupChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/group-chat")
public class GroupChatController {

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private GroupChatWebSocketHandler webSocketHandler;

    @PostMapping("/create/class")
    public Result createClassGroupChat(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.createClassGroupChat(request.getCampId(), request.getClassId(), request.getClassName(),
                userId);
    }

    @PostMapping("/create/big-group")
    public Result createBigGroupChat(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.createBigGroupChat(request.getCampId(), request.getClassId(), request.getBigGroupId(),
                request.getBigGroupName(), userId);
    }

    @PostMapping("/create/small-group")
    public Result createSmallGroupChat(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.createSmallGroupChat(request.getCampId(), request.getClassId(), request.getBigGroupId(),
                request.getSmallGroupId(), request.getSmallGroupName(), userId);
    }

    @GetMapping("/members")
    public Result<List<GroupChatMemberDTO>> getGroupMembers(@RequestParam("chatId") Integer chatId) {
        return groupChatService.getGroupMembers(chatId);
    }

    @PostMapping("/member/add")
    public Result addGroupMember(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.addGroupMember(request.getChatId(), request.getUserId(), request.getRole(),
                currentUserId);
    }

    @PostMapping("/member/batch-add")
    public Result batchAddGroupMembers(@RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        try {
            // 验证token
            Long currentUserId = jwtUtils.getUserIdFromToken(token);

            // 类型安全转换
            Integer chatId = null;
            Object chatIdObj = request.get("chatId");
            if (chatIdObj != null) {
                if (chatIdObj instanceof String) {
                    chatId = Integer.parseInt((String) chatIdObj);
                } else if (chatIdObj instanceof Number) {
                    chatId = ((Number) chatIdObj).intValue();
                }
            }

            List<Long> userIds = new ArrayList<>();
            Object userIdsObj = request.get("userIds");
            if (userIdsObj instanceof List) {
                for (Object item : (List<?>) userIdsObj) {
                    if (item instanceof String) {
                        userIds.add(Long.parseLong((String) item));
                    } else if (item instanceof Number) {
                        userIds.add(((Number) item).longValue());
                    }
                }
            }

            String role = (String) request.get("role");
            if (role == null) {
                role = "member";
            }

            return groupChatService.batchAddGroupMembers(chatId, userIds, role, currentUserId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("参数格式错误：" + e.getMessage());
        }
    }

    @DeleteMapping("/member/remove")
    public Result removeGroupMember(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.removeGroupMember(request.getChatId(), request.getUserId(), currentUserId);
    }

    @PutMapping("/member/role")
    public Result updateMemberRole(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.updateMemberRole(request.getChatId(), request.getUserId(), request.getRole(),
                currentUserId);
    }

    @PutMapping("/message/read")
    public Result markMessageAsRead(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.markMessageAsRead(request.getMessageId(), userId);
    }

    @GetMapping("/messages")
    public Result<List<MessageDTO>> getGroupMessages(@RequestHeader("Authorization") String token,
            @RequestParam("chatId") Integer chatId,
            @RequestParam("limit") Integer limit,
            @RequestParam("offset") Integer offset) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getGroupMessages(chatId, userId, limit, offset);
    }

    @GetMapping("/list")
    public Result<List<GroupChatDTO>> getUserGroupChats(@RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam("dutyType") String dutyType,
            @RequestParam("targetId") Integer targetId) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getGroupChatsByScope(userId, dutyType, targetId);
    }

    @PutMapping("/info")
    public Result updateGroupInfo(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.updateGroupInfo(request.getChatId(), request.getClassName(), request.getContent(),
                userId);
    }

    @GetMapping("/info")
    public Result<GroupChatDTO> getGroupInfo(@RequestParam("chatId") Integer chatId) {
        return groupChatService.getGroupInfo(chatId);
    }

    @PostMapping("/auto-create")
    public Result autoCreateGroupChats(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.autoCreateGroupChats(request.getCampId(), request.getTargetId(), request.getDutyType(),
                userId);
    }

    @PostMapping("/create/class-all")
    public Result createClassAllGroupChats(@RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.createClassAllGroupChats(request.getCampId(), request.getClassId(),
                request.getClassName(), userId);
    }

    @GetMapping("/message/history")
    public Result getMessageHistory(@RequestHeader("Authorization") String token,
            @RequestParam Integer chatId,
            @RequestParam Integer page,
            @RequestParam Integer limit) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getMessageHistory(chatId, userId, page, limit);
    }

    /**
     * 获取群聊未读消息数
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadMessageCount(
            @RequestHeader("Authorization") String token,
            @RequestParam("chatId") Integer chatId) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getUnreadMessageCount(chatId, userId);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/mark-read")
    public Result markMessageAsRead(
            @RequestHeader("Authorization") String token,
            @RequestParam("messageId") Integer messageId) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.markMessageAsRead(messageId, userId);
    }

    /**
     * 标记群聊所有消息为已读
     */
    @PutMapping("/mark-all-read")
    public Result markGroupMessageAsRead(
            @RequestHeader("Authorization") String token,
            @RequestParam("chatId") Integer chatId) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        groupChatService.markAllMessagesAsRead(chatId, userId);
        return Result.success("标记所有消息已读成功");
    }
    


    @GetMapping("/user-groups")
    public Result<List<GroupChatDTO>> getUserGroups(
            @RequestHeader("Authorization") String token,
            @RequestParam("limit") Integer limit,
            @RequestParam("offset") Integer offset) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getUserGroupChats(userId, limit, offset);
    }

    @GetMapping("/available-members")
    public Result<Map<String, Object>> getAvailableMembers(
            @RequestHeader("Authorization") String token,
            @RequestParam("chatId") Integer chatId) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getAvailableMembers(chatId, userId);
    }

    /**
     * 上传语音（兼容原有WebConfig，不创建子文件夹，第一条不失败）
     */
    @PostMapping("/upload-voice")
    public Result<Map<String, Object>> uploadVoice(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {

        // 验证token
        jwtUtils.getUserIdFromToken(token);

        try {
            // 生成唯一文件名 + 后缀
            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".mp3";
            String fileName = UUID.randomUUID() + extension;

            // 项目根目录
            String projectRoot = System.getProperty("user.dir");

            // 绝对路径（Windows、Linux 都兼容）
            String savePath = projectRoot
                    + File.separator + "uploads"
                    + File.separator + "audio"
                    + File.separator + fileName;

            File dest = new File(savePath);

            // 自动创建目录
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            file.transferTo(dest);

            // 返回可播放地址
            String fileUrl = "/uploads/audio/" + fileName;
            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            return Result.success(data);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/message/send")
    public Result sendMessage(@RequestHeader("Authorization") String token,
                              @RequestBody GroupChatRequestDTO request) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.sendMessage(
                request.getChatId(),
                userId,
                request.getContent(),
                request.getMessageType() != null ? request.getMessageType() : "text",
                request.getVoiceUrl(),
                request.getVoiceDuration(),
                request.getRecipientType(),
                request.getRecipientId());
    }

    /**
     * 撤回消息
     */
    @PutMapping("/message/revoke")
    public Result revokeMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody GroupChatRequestDTO request) {

        Long userId = jwtUtils.getUserIdFromToken(token);
        Integer messageId = request.getMessageId();

        // 执行数据库撤回（status=2）
        groupChatService.revokeMessage(messageId, userId);

        // 关键：发送 WebSocket 广播通知所有人（必须加！）
        webSocketHandler.broadcastRevoke(messageId, userId);

        return Result.success("撤回成功");
    }
}