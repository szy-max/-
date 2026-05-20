package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {

    void save(List<OrderDetail> orderDetails);

    List<OrderDetail> getById(Long id);

    List<GoodsSalesDTO> top(LocalDate begin, LocalDate end);
}
