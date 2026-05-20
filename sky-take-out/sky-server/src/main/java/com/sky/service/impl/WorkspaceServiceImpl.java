package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.dto.ReportDTO;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    public BusinessDataVO getBusinessData(ReportDTO reportDTO) {
        LocalDate begin = reportDTO.getBegin();
        LocalDate end = reportDTO.getEnd();
        String newUser = userMapper.sumUser(begin,end);
        Integer totalOrderCount = Integer.valueOf(ordersMapper.count(begin,end,null));
        Integer validOrderCount = Integer.valueOf(ordersMapper.count(begin,end,Orders.COMPLETED));
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != null && totalOrderCount > 0) {
            orderCompletionRate = new BigDecimal(validOrderCount)
                    .divide(new BigDecimal(totalOrderCount), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        String turnover = ordersMapper.sumAmount(begin, end);
        if(turnover == null){
            turnover = "0";
        }
        Double unitPrice = 0.0;
        if (validOrderCount != null && validOrderCount > 0) {
                unitPrice = new BigDecimal(turnover)
                        .divide(new BigDecimal(validOrderCount), 2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

        return new BusinessDataVO(Double.valueOf(turnover),validOrderCount,orderCompletionRate,unitPrice,Integer.valueOf(newUser));
    }



    public OrderOverViewVO getOrderOverView(ReportDTO reportDTO) {
        LocalDate begin = reportDTO.getBegin();
        LocalDate end = reportDTO.getEnd();
        Integer allOrders = Integer.valueOf(ordersMapper.count(begin, end, null));
        Integer cancelledOrders = Integer.valueOf(ordersMapper.count(begin, end, Orders.CANCELLED));
        Integer completedOrders = Integer.valueOf(ordersMapper.count(begin, end, Orders.COMPLETED));
        Integer deliveredOrders = Integer.valueOf(ordersMapper.count(begin, end, Orders.CONFIRMED));
        Integer waitingOrders = Integer.valueOf(ordersMapper.count(begin, end, Orders.TO_BE_CONFIRMED));
        return new OrderOverViewVO(waitingOrders,deliveredOrders,completedOrders,cancelledOrders,allOrders);
    }


    public DishOverViewVO getDishOverView() {
        Integer sold = dishMapper.countStatus(StatusConstant.ENABLE);
        Integer discontinued = dishMapper.countStatus(StatusConstant.DISABLE);
        return new DishOverViewVO(sold,discontinued);
    }


    public SetmealOverViewVO getSetmealOverView() {
        Integer sold = setmealMapper.countStatus(StatusConstant.ENABLE);
        Integer discontinued = setmealMapper.countStatus(StatusConstant.DISABLE);
        return new SetmealOverViewVO(sold,discontinued);
    }
}
