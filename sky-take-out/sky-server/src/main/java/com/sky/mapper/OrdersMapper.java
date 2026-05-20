package com.sky.mapper;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper {
    void save(Orders orders);

    List<Orders> getHistoryOrders(Long userId , Integer status);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    List<OrderVO> searchOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(*) from orders where status = #{status}")
    Integer statistics(Integer status);

    void update(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> searchOutTimeOrders(Integer status, LocalDateTime time);

    @Select("select sum(amount) from orders where status = 5 and order_time between #{begin} and #{end} ")
    String sumAmount(LocalDate begin, LocalDate end);

    String count(LocalDate begin, LocalDate end, Integer status);
}
