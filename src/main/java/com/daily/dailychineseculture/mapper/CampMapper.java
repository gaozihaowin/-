package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.CampListItemDTO;
import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampTypeOptionDTO;
import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.dto.CampInfoDTO;
import com.daily.dailychineseculture.entity.Camp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CampMapper {

    @Select("SELECT camp_id, type_id, name, intro, start_time, end_time, status " +
            "FROM t_camp " +
            "ORDER BY start_time DESC " +
            "LIMIT 5")
    List<Camp> selectHotCamps();

    List<CampVO> selectHotCourses();

    @Select("SELECT * FROM t_camp WHERE camp_id = #{campId}")
    Camp selectById(Integer campId);

    @Select("SELECT * FROM t_camp")
    List<Camp> selectAll();

    @Select("SELECT camp_id, name, status, start_time, enroll_count " +
            "FROM t_camp " +
            "ORDER BY start_time DESC " +
            "LIMIT 5")
    List<Camp> selectRecentCamps();

    int countCampList(@Param("keyword") String keyword, @Param("status") Integer status, @Param("typeId") Integer typeId);

    List<CampListItemDTO> selectCampList(
        @Param("keyword") String keyword,
        @Param("status") Integer status,
        @Param("typeId") Integer typeId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    List<CampTypeOptionDTO> selectAllCampTypes();

    List<CampOptionDTO> selectCampOptions();

    List<Camp> selectByTypeId(@Param("typeId") Integer typeId);

    @Select("SELECT c.camp_id, c.term, c.name as title, c.intro, c.enroll_count, c.tag, " +
            "ct.level as campType, ct.level_name as campName " +
            "FROM t_camp c " +
            "LEFT JOIN t_camp_type ct ON c.type_id = ct.type_id " +
            "WHERE c.camp_id = #{campId}")
    CampInfoDTO selectCampInfo(@Param("campId") Integer campId);

    int insertCamp(Camp camp);

    int updateCamp(Camp camp);

    @Select("SELECT * FROM t_camp WHERE camp_id = #{campId}")
    Camp selectCampForEnroll(@Param("campId") Integer campId);

    @Update("UPDATE t_camp SET enroll_count = enroll_count + 1 WHERE camp_id = #{campId}")
    int incrementEnrollCount(@Param("campId") Integer campId);
}
