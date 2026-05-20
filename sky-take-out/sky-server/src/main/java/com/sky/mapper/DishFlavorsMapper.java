package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface DishFlavorsMapper {


    void save(List<DishFlavor> flavors);

    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getById(Integer id);

    void delete(List<Integer> ids);
}
