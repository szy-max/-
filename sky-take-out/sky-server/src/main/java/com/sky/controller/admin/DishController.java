package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    //清理缓存
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping()
    public Result addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.save(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result getpage(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询{}", dishPageQueryDTO);
        return Result.success(dishService.getpage(dishPageQueryDTO));
    }

    /**
     * 修改菜品状态
     * @param status
     * @param id
     * @return
     */
    @Transactional
    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status, @RequestParam Integer id) {
        log.info("修改菜品状态{}{}", id, status);
        dishService.updateStatus(status,id);
        DishVO dish = dishService.getDishById(id);
        String key = "dish_" + dish.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getDishById(@PathVariable Integer id) {
        log.info("根据id查询菜品{}", id);
        return Result.success(dishService.getDishById(id));
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping()
    public Result deleteDishById(@RequestParam List<Integer> ids) {
        log.info("批量删除菜品{}", ids);
        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result list(Integer categoryId) {
        log.info("根据分类id查询菜品{}", categoryId);
        return Result.success(dishService.list(categoryId));
    }

    /**
     * 更新菜品信息
     * @param dishDTO
     * @return
     */
    @Transactional
    @PutMapping()
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        log.info("更新菜品信息{}", dishDTO);
        DishVO oldDish = dishService.getDishById(Integer.valueOf(dishDTO.getId().toString()));
        String key = "dish_" + oldDish.getCategoryId();
        redisTemplate.delete(key);
        dishService.update(dishDTO);
        key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }
}
