package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void save(Dish dish);

    List<DishVO> getpage(DishPageQueryDTO dishPageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    @Update("update dish set status = #{status}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void updateStatus(Dish dish);

    @Select("select * , category.name as category_name from dish left join category on dish.category_id = category.id where dish.id = #{id}")
    DishVO getDishById(Integer id);

    void delete(List<Integer> ids);


    List<DishVO> list(Integer categoryId);

    @AutoFill(value = OperationType.UPDATE)

    void update(Dish dish);

    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select a.name, a.image, a.description, b.copies from dish a join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId} and a.status = #{status}" )
    List<DishItemVO> getBySetmealId(Long setmealId, Integer status);

    @Select("select setmeal_dish.* from setmeal_dish join dish on setmeal_dish.dish_id = dish.id where setmeal_dish.dish_id = #{id}")
    List<SetmealDish> getSetmealDishById(Integer id);

    Integer countStatus(Integer enable);
}
