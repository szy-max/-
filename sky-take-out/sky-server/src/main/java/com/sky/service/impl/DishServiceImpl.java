package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.AliyunOSSOUtil;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;
    @Autowired
    private AliyunOSSOUtil aliyunOSSOUtil;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public String upload(MultipartFile file) {
        try {
            return aliyunOSSOUtil.upload(file.getBytes(), file.getOriginalFilename());
        } catch (Exception e) {
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }
    }

    @Transactional
    @Override
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(StatusConstant.DISABLE);
        dishMapper.save(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && !flavors.isEmpty()){
            for(DishFlavor flavor:flavors){
                flavor.setDishId(dish.getId());
            }
            dishFlavorsMapper.save(flavors);
        }
    }

    @Override
    public PageResult getpage(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> dishList = (Page<DishVO>) dishMapper.getpage(dishPageQueryDTO);
        return new PageResult(dishList.getTotal(),dishList.getResult());
    }

    @Override
    public void updateStatus(Integer status, Integer id) {
        if (status == StatusConstant.DISABLE) {
            List<SetmealDish> dishList = dishMapper.getSetmealDishById(id);
            for (SetmealDish setmealDish : dishList) {
                Setmeal setmeal = setmealMapper.getById(setmealDish.getSetmealId());
                if (setmeal.getStatus() == StatusConstant.ENABLE) {
                    throw new DeletionNotAllowedException(MessageConstant.DISH_BE_DISABLE_BY_SETMEAL);
                }
            }
        }
            Dish dish = new Dish();
            dish.setStatus(status);
            dish.setId(Long.valueOf(id));
            dishMapper.updateStatus(dish);
        }


    @Transactional
    @Override
    public DishVO getDishById(Integer id) {
        DishVO dishVO = dishMapper.getDishById(id);
        List<DishFlavor>  flavors = dishFlavorsMapper.getById(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Transactional
    @Override
    public void delete(List<Integer> ids) {
          ids.forEach( id -> {
            DishVO dishVO = dishMapper.getDishById(id);
            if(StatusConstant.ENABLE == dishVO.getStatus()){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            List<SetmealDish> dishList = dishMapper.getSetmealDishById(id);
            if(dishList != null && dishList.size() > 0){
                throw new SetmealEnableFailedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        });
        dishMapper.delete(ids);
        dishFlavorsMapper.delete(ids);
    }

    @Transactional
    @Override
    public List<DishVO> list(Integer categoryId) {
        String key = "dish_" + categoryId;
        //先在redis中查询
        log.info("查询reids中菜品");
        List<DishVO> list = (List)redisTemplate.opsForValue().get(key);
        if(list != null && list.size() > 0){
            return list;
        }
        log.info("查询MySQL中菜品");
        List<DishVO> dishVOList = dishMapper.list(categoryId);
        dishVOList.forEach(dishVO -> {
            List<DishFlavor> dishFlavors = dishFlavorsMapper.getById(Integer.valueOf(dishVO.getId().toString()));
            dishVO.setFlavors(dishFlavors);
        });
        redisTemplate.opsForValue().set(key,dishVOList);
        return dishVOList;
    }

    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        List<Integer> list = new ArrayList();
        list.add(Integer.valueOf(dishDTO.getId().toString()));
        dishFlavorsMapper.delete(list);
        if(dishDTO.getFlavors()!=null && !dishDTO.getFlavors().isEmpty()){
            for(DishFlavor flavor : dishDTO.getFlavors()) {
               flavor.setDishId(dish.getId());
            }
            dishFlavorsMapper.save(dishDTO.getFlavors());
        }
    }
}
