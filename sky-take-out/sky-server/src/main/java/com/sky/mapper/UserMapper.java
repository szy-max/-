package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import com.sky.vo.UserLoginVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface UserMapper {

    @Options(useGeneratedKeys = true ,keyProperty = "id")
    @Insert("insert into user(openid, create_time) values (#{openid}, #{createTime})")
    void save(User user);

    @Select("select * from user where openid = #{openid}")
    User selece(String openid);

    @Select("select * from user where id = #{id}")
    User getById(Long id);

    @Select("select count(*) from user where create_time between #{begin} and #{end}")
    String sumUser(LocalDate begin, LocalDate end);
}
