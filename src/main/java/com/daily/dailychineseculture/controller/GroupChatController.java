package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.GroupChatDTO;
import com.daily.dailychineseculture.dto.GroupChatMemberDTO;
import com.daily.dailychineseculture.dto.GroupChatRequestDTO;
import com.daily.dailychineseculture.dto.MessageDTO;
import com.daily.dailychineseculture.service.GroupChatService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/group-chat")
public class GroupChatController {

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/create/class")
    public Result createClassGroupChat(@RequestHeader("Authorization") String token,
                                       @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.createClassGroupChat(request.getCampId(), request.getClassId(), request.getClassName(), userId);
    }

    @PostMapping("/create/big-group")
    public Result createBigGroupChat(@RequestHeader("Authorization") String token,
                                     @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.createBigGroupChat(request.getCampId(), request.getClassId(), request.getBigGroupId(), request.getBigGroupName(), userId);
    }

    @PostMapping("/create/small-group")
    public Result createSmallGroupChat(@RequestHeader("Authorization") String token,
                                       @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.createSmallGroupChat(request.getCampId(), request.getClassId(), request.getBigGroupId(), request.getSmallGroupId(), request.getSmallGroupName(), userId);
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
        return groupChatService.addGroupMember(request.getChatId(), request.getUserId(), request.getRole(), currentUserId);
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
        return groupChatService.updateMemberRole(request.getChatId(), request.getUserId(), request.getRole(), currentUserId);
    }

    @PostMapping("/message/send")
    public Result sendMessage(@RequestHeader("Authorization") String token,
                              @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.sendMessage(request.getChatId(), userId, request.getContent(), request.getRecipientType(), request.getRecipientId());
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
        return groupChatService.updateGroupInfo(request.getChatId(), request.getClassName(), request.getContent(), userId);
    }

    @DeleteMapping("/delete")
    public Result deleteGroupChat(@RequestHeader("Authorization") String token,
                                  @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.deleteGroupChat(request.getChatId(), userId);
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
        return groupChatService.autoCreateGroupChats(request.getCampId(), request.getTargetId(), request.getDutyType(), userId);
    }

    @PostMapping("/create/class-all")
    public Result createClassAllGroupChats(@RequestHeader("Authorization") String token,
                                           @RequestBody GroupChatRequestDTO request) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.createClassAllGroupChats(request.getCampId(), request.getClassId(), request.getClassName(), userId);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadMessageCount(@RequestHeader("Authorization") String token, @RequestParam("chatId") Integer chatId) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getUnreadMessageCount(chatId, userId);
    }

    @GetMapping("/user-groups")
    public Result<List<GroupChatDTO>> getUserGroups(
            @RequestHeader("Authorization") String token,
            @RequestParam("limit") Integer limit,
            @RequestParam("offset") Integer offset
    ) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getUserGroupChats(userId, limit, offset);
    }

    @GetMapping("/available-members")
    public Result<Map<String, Object>> getAvailableMembers(
            @RequestHeader("Authorization") String token,
            @RequestParam("chatId") Integer chatId
    ) {
        // 验证token
        Long userId = jwtUtils.getUserIdFromToken(token);
        return groupChatService.getAvailableMembers(chatId, userId);
    }
}