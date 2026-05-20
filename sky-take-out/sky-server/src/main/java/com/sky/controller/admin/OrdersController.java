package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/admin/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @GetMapping("/conditionSearch")
    public Result searchOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("搜索订单：{}", ordersPageQueryDTO);
        PageResult pageResult = ordersService.searchOrders(ordersPageQueryDTO);
        log.info("搜索订单成功，共{}条", pageResult.getTotal());
        return Result.success(pageResult);
    }

    @GetMapping("/details/{id}")
    public Result details(@PathVariable Long id) {
        log.info("查询订单详情：id={}", id);
        OrderVO orderDetail = ordersService.getOrderDetail(id);
        log.info("查询订单详情成功：{}", orderDetail);
        return Result.success(orderDetail);
    }

    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单：{}", ordersCancelDTO);
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersCancelDTO, orders);
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        ordersService.setStatus(orders);
        log.info("订单取消成功：id={}", ordersCancelDTO.getId());
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result statistics() {
        log.info("获取订单统计信息");
        OrderStatisticsVO orderStatisticsVO = ordersService.statistics();
        log.info("获取订单统计信息成功：{}", orderStatisticsVO);
        return Result.success(orderStatisticsVO);
    }

    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id) {
        log.info("完成订单：id={}", id);
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        ordersService.setStatus(orders);
        log.info("订单完成成功：id={}", id);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id) {
        log.info("开始配送订单：id={}", id);
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        ordersService.setStatus(orders);
        log.info("订单配送成功：id={}", id);
        return Result.success();
    }

    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("确认订单：{}", ordersConfirmDTO);
        ordersConfirmDTO.setStatus(Orders.CONFIRMED);
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersConfirmDTO, orders);
        ordersService.setStatus(orders);
        log.info("订单确认成功：id={}", ordersConfirmDTO.getId());
        return Result.success();
    }

    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒绝订单：{}", ordersRejectionDTO);
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersRejectionDTO, orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        ordersService.setStatus(orders);
        log.info("订单拒绝成功：id={}", ordersRejectionDTO.getId());
        return Result.success();
    }
}