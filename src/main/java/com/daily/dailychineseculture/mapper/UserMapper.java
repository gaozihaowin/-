package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {

    /**
     * 查询所有用户
     */
    @Select("SELECT * FROM t_user")
    List<User> selectAll();

    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM t_user WHERE user_id = #{userId}")
    User selectById(Long userId);

    /**
     * 根据账号查询用户
     */
    @Select("SELECT * FROM t_user WHERE account = #{account}")
    User selectByAccount(String account);

    /**
     * 插入用户
     */
    @Insert("INSERT INTO t_user(user_id, account, password, gender, create_time, status) " +
            "VALUES(#{userId}, #{account}, #{password}, #{gender}, #{createTime}, #{status})")
    int insert(User user);

    /**
     * 更新用户
     */
    @Update("UPDATE t_user SET account=#{account}, password=#{password}, avatar=#{avatar}, " +
            "phone=#{phone}, region=#{region}, birthday=#{birthday}, profession=#{profession}, gender=#{gender} " +
            "WHERE user_id=#{userId}")
    int update(User user);

    /**
     * 删除用户
     */
    @Delete("DELETE FROM t_user WHERE user_id = #{userId}")
    int deleteById(Long userId);
}