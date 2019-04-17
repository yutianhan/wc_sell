package com.xmcc.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xmcc.common.*;
import com.xmcc.dto.OrderDetailDto;
import com.xmcc.dto.OrderMasterDto;
import com.xmcc.entity.OrderDetail;
import com.xmcc.entity.OrderMaster;
import com.xmcc.entity.ProductInfo;
import com.xmcc.repository.OrderMasterRepository;
import com.xmcc.service.OrderDetailService;
import com.xmcc.service.OrderMasterService;
import com.xmcc.service.ProductInfoService;
import com.xmcc.util.BigDecimalUtil;
import com.xmcc.util.CustomException;
import com.xmcc.util.IDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderMasterServiceImpl implements OrderMasterService {

    @Autowired
    private OrderMasterRepository orderMasterRepository;
    @Autowired
    private ProductInfoService productInfoService;
    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public ResultResponse insertOrder(OrderMasterDto orderMasterDto) {
        //在controller层已经进行了参数校验 这儿不需要了  取出订单项即可
        List<OrderDetailDto> items = orderMasterDto.getItems();
        //创建订单detail 集合 将符合的放入其中 待会批量插入
        List<OrderDetail> orderDetailList = Lists.newArrayList();
        //创建订单总金额设置为0  涉及到钱的都用 高精度计算
        BigDecimal totalPrice = new BigDecimal("0");
        //遍历订单项，获取商品详情
        for(OrderDetailDto orderDetailDto:items){
            //查询订单
            ResultResponse<ProductInfo> resultResponse = productInfoService.queryById(orderDetailDto.getProductId());
            //判断resultResponse的code即可
            if(resultResponse.getCode()== ResultEnums.FAIL.getCode()){
                throw new CustomException(resultResponse.getMsg());
            }
            //得到商品
            ProductInfo productInfo = resultResponse.getData();
            //比较库存
            if(productInfo.getProductStock()<orderDetailDto.getProductQuatity()){
                throw new CustomException(ProductEnums.PRODUCT_NOT_ENOUGH.getMsg());
            }
            //创建订单
            OrderDetail orderDetail = OrderDetail.builder().detailId(IDUtils.createIdbyUUID())
                    .productIcon(productInfo.getProductIcon())
                    .productId(orderDetailDto.getProductId())
                    .productName(productInfo.getProductName())
                    .productPrice(productInfo.getProductPrice())
                    .productQuantity(orderDetailDto.getProductQuatity()).build();
            //添加到订单单项集合中
            orderDetailList.add(orderDetail);
            //减少商品库存
            productInfo.setProductStock(productInfo.getProductStock()-orderDetail.getProductQuantity());
            //更新商品数据
            productInfoService.updateproduct(productInfo);
            //计算价格
            totalPrice = BigDecimalUtil.add(totalPrice,BigDecimalUtil.multi(productInfo.getProductPrice(),orderDetailDto.getProductQuatity()));
        }
        //生成订单id
        String orderId = IDUtils.createIdbyUUID();
        //构建订单信息
        OrderMaster orderMaster = OrderMaster.builder()
                .orderId(orderId).buyerAddress(orderMasterDto.getAddress()).buyerName(orderMasterDto.getName())
                .buyerOpenid(orderMasterDto.getOpenid()).buyerPhone(orderMasterDto.getPhone())
                .orderAmount(totalPrice).orderStatus(OrderEnum.NEW.getCode()).payStatus(PayEnums.WAIT.getCode()).build();
        //将订单id设置到订单项中
        List<OrderDetail> orderDetails = orderDetailList.stream().map(
                orderDetail -> {
                    orderDetail.setOrderId(orderId);
                    return orderDetail;
                }).collect(Collectors.toList());
        //批量插入订单项
        orderDetailService.batchInsert(orderDetails);
        //批量插入订单
        orderMasterRepository.save(orderMaster);

        HashMap<String,String> map = Maps.newHashMap();
        map.put("orderId",orderId);
        return ResultResponse.success(map);
    }
}
