package com.sky.task;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrdersTask {
    @Autowired
    private OrdersMapper ordersMapper;
    @Scheduled(cron = "0 * * * * *")
    public void cancelOrders(){
        log.info("处理超时未支付订单");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = ordersMapper.searchOutTimeOrders(Orders.PENDING_PAYMENT,time);
        for (Orders orders : ordersList){
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelTime(LocalDateTime.now());
            orders.setCancelReason("超时未支付，系统自动取消");
            ordersMapper.update(orders);
            }
        }
    @Scheduled(cron = "0 0 1 * * *")
    public void completeOrders(){
        log.info("处理商家忘更新完成状态订单");
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> ordersList = ordersMapper.searchOutTimeOrders(Orders.DELIVERY_IN_PROGRESS, time);
        for (Orders orders : ordersList) {
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
            ordersMapper.update(orders);
        }
    }
}
