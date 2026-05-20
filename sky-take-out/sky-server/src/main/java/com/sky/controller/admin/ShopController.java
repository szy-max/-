package com.sky.controller.admin;

import com.sky.mapper.DishMapper;
import com.sky.result.Result;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
public class ShopController {
    @Autowired
    private ShopService shopService;

    /**
     * 查询店铺运营状态
     * @return
     */
    @GetMapping("/status")
    public Result status(){
        log.info("查询店铺运营状态");
        Integer status = shopService.getStatus();
        return Result.success(status);
    }

    /**
     * 更改店铺运营状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable("status") Integer status){
        log.info("更改店铺运营状态{}",status);
        shopService.setStatus(status);
        return Result.success();
    }
}
