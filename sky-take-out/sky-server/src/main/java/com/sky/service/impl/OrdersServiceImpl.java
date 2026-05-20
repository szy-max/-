package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.exception.UserNotLoginException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook address = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (address == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        List<ShoppingCart> list = shoppingCartMapper.list(BaseContext.getCurrentId());
        if (list == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setUserId(BaseContext.getCurrentId());
        orders.setAddress(address.getDetail());
        orders.setPhone(address.getPhone());
        orders.setConsignee(address.getConsignee());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        ordersMapper.save(orders);
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.save(orderDetails);
        shoppingCartMapper.clean(orders.getUserId());
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime()).build();
        return orderSubmitVO;
    }

    @Override
    public PageResult getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        if(BaseContext.getCurrentId() == null){
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        List<Orders> ordersList = ordersMapper.getHistoryOrders(BaseContext.getCurrentId(),ordersPageQueryDTO.getStatus());
        Page<Orders> ordersPage = (Page) ordersList;
        List<OrderVO> orderVOS = new ArrayList<>();
        for (Orders orders : ordersList) {
            List<OrderDetail> orderDetails = orderDetailMapper.getById(orders.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            orderVO.setOrderDetailList(orderDetails);
            orderVOS.add(orderVO);
        }
        return new PageResult(ordersPage.getTotal(),orderVOS);
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        if(BaseContext.getCurrentId() == null){
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        Orders orders = ordersMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailMapper.getById(id));
        return orderVO;
    }

    @Transactional
    @Override
    public void repetition(Long id) {
        if(BaseContext.getCurrentId() == null){
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        List<OrderDetail> orderDetails = orderDetailMapper.getById(id);
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCarts.add(shoppingCart);
        }
        shoppingCartMapper.addAll(shoppingCarts);
    }

    @Override
    public PageResult searchOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        List<OrderVO> orderVOS = ordersMapper.searchOrders(ordersPageQueryDTO);
        for (OrderVO orderVO : orderVOS) {
            List<OrderDetail> orderDetails = orderDetailMapper.getById(orderVO.getId());
            String orderDishes = "";
            for (int i = 0; i < orderDetails.size(); i++) {
                OrderDetail orderDetail = orderDetails.get(i);
              if(i == orderDetails.size()-1){
                  orderDishes = orderDishes + orderDetail.getName() + orderDetail.getDishFlavor() + orderDetail.getNumber() + "份";
              } else {
                  orderDishes = orderDishes + orderDetail.getName() + orderDetail.getDishFlavor() + orderDetail.getNumber() + "份" + ",";
              }
            }
            orderVO.setOrderDishes(orderDishes);
        }
        Page<OrderVO> orderVOPage = (Page) orderVOS;
        return new PageResult(orderVOPage.getTotal(),orderVOS);
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        Integer toBeConfirmed = ordersMapper.statistics(Orders.TO_BE_CONFIRMED);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        Integer Confirmed = ordersMapper.statistics(Orders.CONFIRMED);
        orderStatisticsVO.setConfirmed(Confirmed);
        Integer deliveryInProgress = ordersMapper.statistics(Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void setStatus(Orders orders) {
        ordersMapper.update(orders);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (ordersDB.getStatus() != 1) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);
        Map<String,Object> map = new HashMap<>();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号"+ outTradeNo);
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = ordersMapper.getById(id);
        Map<String,Object> map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",orders.getId());
        map.put("content","订单号"+ orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }
}
