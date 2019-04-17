package com.xmcc.service;

import com.xmcc.entity.OrderDetail;

import java.util.List;

public interface OrderDetailService {
    void batchInsert(List<OrderDetail> orderDetailList);
}
