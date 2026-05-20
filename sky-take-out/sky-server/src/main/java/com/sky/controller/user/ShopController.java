package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@Slf4j
@RequestMapping("/user/shop")
public class ShopController {
    @Autowired
    private ShopService shopService;

    /**
     * 查询店铺运营状态
     * @return
     */
    @GetMapping("/status")
    public Result getStatus() {
        log.info("查询店铺运营状态");
        Integer status = shopService.getStatus();
        return Result.success(status);
    }
}
