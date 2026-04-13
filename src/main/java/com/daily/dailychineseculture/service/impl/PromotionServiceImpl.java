package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampEnrollmentDTO;
import com.daily.dailychineseculture.dto.CampTypeDTO;
import com.daily.dailychineseculture.dto.PromotionCheckResultDTO;
import com.daily.dailychineseculture.entity.Camp;
import com.daily.dailychineseculture.mapper.CampEnrollmentMapper;
import com.daily.dailychineseculture.mapper.CampMapper;
import com.daily.dailychineseculture.mapper.CampTypeMapper;
import com.daily.dailychineseculture.mapper.HomeworkMapper;
import com.daily.dailychineseculture.service.CertificateService;
import com.daily.dailychineseculture.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private CampEnrollmentMapper enrollmentMapper;

    @Autowired
    private CampMapper campMapper;

    @Autowired
    private CampTypeMapper campTypeMapper;

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private CertificateService certificateService;

    private static final int MISSED_THRESHOLD = 3;

    @Override
    public PromotionCheckResultDTO checkPromotionEligibility(Long userId, Integer campId) {
        PromotionCheckResultDTO result = new PromotionCheckResultDTO();
        result.setUserId(userId);
        result.setCurrentCampId(campId);

        CampEnrollmentDTO enrollment = enrollmentMapper.selectByUserIdAndCampId(userId, campId);
        if (enrollment == null) {
            result.setEligible(false);
            result.setReason("用户未报名此营期");
            return result;
        }

        Camp currentCamp = campMapper.selectById(campId);
        if (currentCamp == null) {
            result.setEligible(false);
            result.setReason("营期不存在");
            return result;
        }

        result.setCurrentCampName(currentCamp.getName());
        result.setProgress(enrollment.getProgress() != null ? enrollment.getProgress() : 0);

        CampTypeDTO currentType = campTypeMapper.selectCampTypeById(currentCamp.getTypeId());
        if (currentType == null) {
            result.setEligible(false);
            result.setReason("营期类型不存在");
            return result;
        }

        result.setCurrentTypeId(currentType.getTypeId());
        result.setCurrentTypeName(currentType.getLevelName());

        CampTypeDTO nextType = campTypeMapper.selectNextPromotionType(currentType.getLevel());
        if (nextType == null) {
            result.setEligible(false);
            result.setReason("已到达最高营期");
            return result;
        }

        result.setNextTypeId(nextType.getTypeId());
        result.setNextTypeName(nextType.getLevelName());

        List<Camp> nextCamps = campMapper.selectByTypeId(nextType.getTypeId());
        List<PromotionCheckResultDTO.NextCampDTO> availableCamps = new ArrayList<>();
        for (Camp c : nextCamps) {
            PromotionCheckResultDTO.NextCampDTO dto = new PromotionCheckResultDTO.NextCampDTO();
            dto.setCampId(c.getCampId());
            dto.setName(c.getName());
            dto.setTerm(String.valueOf(c.getTerm()));
            availableCamps.add(dto);
        }
        result.setAvailableCamps(availableCamps);

        Integer totalPlans = homeworkMapper.countTotalPlansByCamp(campId);
        result.setTotalPlans(totalPlans != null ? totalPlans : 0);

        Integer submittedHomework = homeworkMapper.countSubmittedHomeworkByUserAndCamp(userId, campId);
        result.setSubmittedHomework(submittedHomework != null ? submittedHomework : 0);

        Integer missedConsecutive = homeworkMapper.countMissedConsecutiveDays(userId, campId, MISSED_THRESHOLD);
        result.setMissedConsecutive(missedConsecutive != null ? missedConsecutive : 0);

        if (result.getProgress() < 100) {
            result.setEligible(false);
            result.setReason("学习进度未达100%");
            return result;
        }

        if (missedConsecutive != null && missedConsecutive >= MISSED_THRESHOLD) {
            result.setEligible(false);
            result.setReason("连续" + MISSED_THRESHOLD + "天未提交功课，不符合升班条件");
            return result;
        }

        result.setEligible(true);
        result.setReason("符合升班条件");
        return result;
    }

    @Override
    @Transactional
    public boolean promoteStudent(Long userId, Integer currentCampId, Integer targetCampId) {
        CampEnrollmentDTO enrollment = enrollmentMapper.selectByUserIdAndCampId(userId, currentCampId);
        if (enrollment == null) {
            return false;
        }

        Camp currentCamp = campMapper.selectById(currentCampId);
        if (currentCamp == null) {
            return false;
        }

        CampTypeDTO currentType = campTypeMapper.selectCampTypeById(currentCamp.getTypeId());

        enrollmentMapper.updateCompletionStatus(userId, currentCampId, 1);

        if (currentType != null) {
            certificateService.issueCompletionCertificate(userId, currentType.getLevelName());
        }

        enrollmentMapper.insertEnrollment(userId, targetCampId);

        return true;
    }

    @Override
    @Transactional
    public int batchCheckAndMarkCompletion(Integer campId) {
        List<CampEnrollmentDTO> enrollments = enrollmentMapper.selectEnrollmentsByCampId(campId);
        int count = 0;

        Camp camp = campMapper.selectById(campId);
        CampTypeDTO campType = camp != null ? campTypeMapper.selectCampTypeById(camp.getTypeId()) : null;

        for (CampEnrollmentDTO enrollment : enrollments) {
            if (enrollment.getIsCompleted() != null && enrollment.getIsCompleted() == 1) {
                continue;
            }

            PromotionCheckResultDTO result = checkPromotionEligibility(enrollment.getUserId(), campId);
            if (result.isEligible()) {
                enrollmentMapper.updateCompletionStatus(enrollment.getUserId(), campId, 1);
                if (campType != null) {
                    certificateService.issueCompletionCertificate(enrollment.getUserId(), campType.getLevelName());
                }
                count++;
            }
        }

        return count;
    }
}
