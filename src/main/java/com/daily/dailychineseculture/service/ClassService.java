package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.BigGroupDTO;
import com.daily.dailychineseculture.dto.ClassAssignResultDTO;
import com.daily.dailychineseculture.dto.ClassDTO;
import com.daily.dailychineseculture.dto.SmallGroupDTO;

import java.util.List;
import java.util.Map;

public interface ClassService {

    List<ClassDTO> getAllClasses();

    List<BigGroupDTO> getAllBigGroups();

    List<SmallGroupDTO> getAllSmallGroups();

    Result autoAssign(Integer campId, Integer perClassNum);

    List<Map<String, Object>> getUnassignedStudents(Integer campId);

    List<ClassDTO> getClassesByCampId(Integer campId);

    Result<ClassAssignResultDTO> assignStudentsByClassCount(Integer campId, Integer classCount);

    List<Map<String, Object>> getAllStudentsByCampId(Integer campId);

    Result<Void> moveStudentToClass(Long userId, Integer campId, Integer newClassId);

    Map<String, Object> getStudentDetail(Long userId, Integer campId);

    Result<Void> resetAssignment(Integer campId);
}
