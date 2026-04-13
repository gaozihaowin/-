package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.AssetCheckDTO;
import com.daily.dailychineseculture.dto.HandoverReq;
import com.daily.dailychineseculture.mapper.DutyAssignmentMapper;
import com.daily.dailychineseculture.mapper.GroupChatMapper;
import com.daily.dailychineseculture.mapper.UserMapper;
import com.daily.dailychineseculture.service.DutyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DutyServiceImpl implements DutyService {

    @Resource
    private DutyAssignmentMapper dutyAssignmentMapper;

    @Resource
    private GroupChatMapper groupChatMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public AssetCheckDTO checkAssets(Long userId) {
        AssetCheckDTO dto = new AssetCheckDTO();
        Integer dutyCount = dutyAssignmentMapper.countByUserId(userId);
        Integer chatAdminCount = groupChatMapper.countChatAdminByUserId(userId);
        dto.setDutyCount(dutyCount != null ? dutyCount : 0);
        dto.setChatAdminCount(chatAdminCount != null ? chatAdminCount : 0);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeHandover(Long oldUserId, Long newUserId) {
        List<Map<String, Object>> oldAdminChats = groupChatMapper.listChatAdminByUserId(oldUserId);
        for (Map<String, Object> chat : oldAdminChats) {
            Integer chatId = (Integer) chat.get("chat_id");
            int existCount = groupChatMapper.checkMemberExists(chatId, newUserId);
            if (existCount <= 0) {
                groupChatMapper.addGroupMember(chatId, newUserId, "admin");
            } else {
                groupChatMapper.updateMemberRole(chatId, newUserId, "admin");
            }
            groupChatMapper.updateMemberRole(chatId, oldUserId, "member");
        }
        dutyAssignmentMapper.transferAllAssignments(oldUserId, newUserId);
    }
}