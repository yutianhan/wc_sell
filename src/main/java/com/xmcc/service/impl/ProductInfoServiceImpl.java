package com.xmcc.service.impl;

import com.google.common.collect.Lists;
import com.xmcc.common.ResultEnums;
import com.xmcc.common.ResultResponse;
import com.xmcc.dto.ProductCategoryDto;
import com.xmcc.dto.ProductInfoDto;
import com.xmcc.entity.ProductCategory;
import com.xmcc.entity.ProductInfo;
import com.xmcc.repository.ProductCategoryRepository;
import com.xmcc.repository.ProductInfoRepository;
import com.xmcc.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductInfoServiceImpl implements ProductInfoService {

    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Override
    public ResultResponse queryList() {
        //查询所有分类
        List<ProductCategory> all = productCategoryRepository.findAll();
        //流遍历转换为dto
        List<ProductCategoryDto> productCategoryDtoList
                = all.stream().map(productCategory -> ProductCategoryDto.build(productCategory)
        ).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(all)) {
            return ResultResponse.fail();
        }
        //获取类目编号集合
        List<Integer> typeList
                = productCategoryDtoList.stream().map(productCategoryDto -> productCategoryDto.getCategoryType()).collect(Collectors.toList());
        //根据typeList查询商品列表
        List<ProductInfo> productInfoList
                = productInfoRepository.findByProductStatusAndCategoryTypeIn(ResultEnums.PRODUCT_UP.getCode(), typeList);

        System.out.println("productInfoList:"+productInfoList);

        //对productCategoryDtoList集合进行遍历 取出每个商品的类目编号，并设置到对应的目录中
        //1、将productInfo设置入foods中
        //2、过滤：不同的type对应不同的目录进行封装
        //3、将productInfo转为dto
        List<ProductInfoDto> productInfoDtos = Lists.newArrayList();


        productCategoryDtoList
                = productCategoryDtoList.parallelStream()
                .map(productCategoryDto -> { productCategoryDto.setProductInfoDto(productInfoList.stream()
                        .filter(productInfo -> productInfo.getCategoryType().equals(productCategoryDto.getCategoryType()))
                        .map(productInfo -> ProductInfoDto.build(productInfo))
                        .collect(Collectors.toList()));
                    return productCategoryDto;
                }).collect(Collectors.toList());

                System.out.println("productCategoryDtos:"+productCategoryDtoList);
            return ResultResponse.success(productCategoryDtoList);
    }
}
