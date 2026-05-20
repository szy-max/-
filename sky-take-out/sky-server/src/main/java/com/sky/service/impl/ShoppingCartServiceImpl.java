package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.UserNotLoginException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.DishService;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service

public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Transactional
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        if(BaseContext.getCurrentId() == null){
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart old = shoppingCartMapper.get(shoppingCart);
        if(old != null){
            old.setNumber(old.getNumber() + 1);
            shoppingCartMapper.update(old);
            return;
        }
        shoppingCart.setNumber(1);
        if(shoppingCart.getDishId() != null){
            DishVO dish = dishMapper.getDishById(Integer.valueOf(shoppingCart.getDishId().toString()));
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        }
        if(shoppingCart.getSetmealId() != null){
            Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }
        shoppingCartMapper.add(shoppingCart);
    }

    @Override
    public List<ShoppingCart> list() {
        List<ShoppingCart> list = shoppingCartMapper.list(BaseContext.getCurrentId());
        if(BaseContext.getCurrentId() == null){
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        return list;
    }

    @Transactional
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        if(BaseContext.getCurrentId() == null){
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart = shoppingCartMapper.get(shoppingCart);
        if(shoppingCart.getNumber() == 1){shoppingCartMapper.sub(shoppingCart);return;}
        shoppingCart.setNumber(shoppingCart.getNumber() - 1);
        shoppingCartMapper.update(shoppingCart);
    }

    @Override
    public void clean() {
        if(BaseContext.getCurrentId() == null){
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        shoppingCartMapper.clean(BaseContext.getCurrentId());
    }

}
