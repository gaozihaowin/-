package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.*;

/**
 * 用户序列表Mapper
 */
@Mapper
public interface UserSeqMapper {

    /**
     * 获取指定年份的当前序号
     */
    @Select("SELECT current_seq FROM t_user_seq WHERE year = #{year}")
    Integer getCurrentSeq(Integer year);

    /**
     * 插入新年份的序号记录
     */
    @Insert("INSERT INTO t_user_seq(year, current_seq) VALUES(#{year}, #{currentSeq})")
    int insertYearSeq(Integer year, Integer currentSeq);

    /**
     * 更新指定年份的序号
     */
    @Update("UPDATE t_user_seq SET current_seq = #{currentSeq} WHERE year = #{year}")
    int updateCurrentSeq(Integer year, Integer currentSeq);
}