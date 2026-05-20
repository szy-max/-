package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DishService {
    String upload(MultipartFile file);

    void save(DishDTO dishDTO);

    PageResult getpage(DishPageQueryDTO dishPageQueryDTO);

    void updateStatus(Integer status, Integer id);

    DishVO getDishById(Integer id);

    void delete(List<Integer> ids);

    List<DishVO> list(Integer categoryId);

    void update(DishDTO dishDTO);

}
