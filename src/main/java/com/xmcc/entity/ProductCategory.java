package com.xmcc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@DynamicUpdate
@Table(name = "product_category")
@Entity
public class ProductCategory {
    @Id  //主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) //表示自增IDENTITY：mysql SEQUENCE:oracle
    private int categoryId;
    private String categoryName;
    private int categoryType;
    private Date createTime;
    private Date updateTime;
}
