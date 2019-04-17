package com.xmcc.dao;

import java.util.List;

public interface BatchDao<T> {
    //批量执行接口
    void batchInsert(List<T> list);

}
