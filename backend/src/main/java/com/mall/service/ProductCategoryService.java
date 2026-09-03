package com.mall.service;

import com.mall.entity.ProductCategoryDO;
import com.mall.vo.ProductCategoryVO;

import java.util.List;

/**
 * 商品分类Service接口
 * 
 * @author mall
 * @date 2024/01/01
 */
public interface ProductCategoryService {
    
    /**
     * 获取所有分类（树形结构）
     *
     * @return 分类树
     */
    List<ProductCategoryVO> getCategoryTree();
    
    /**
     * 获取所有一级分类
     *
     * @return 一级分类列表
     */
    List<ProductCategoryVO> getFirstLevelCategories();
    
    /**
     * 根据父分类ID获取子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<ProductCategoryVO> getCategoriesByParentId(Long parentId);
    
    /**
     * 获取分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情
     */
    ProductCategoryVO getCategoryDetail(Long categoryId);
    
    /**
     * 创建分类
     *
     * @param categoryDO 分类信息
     * @return 分类ID
     */
    Long createCategory(ProductCategoryDO categoryDO);
    
    /**
     * 更新分类
     *
     * @param categoryId 分类ID
     * @param categoryDO 分类信息
     * @return 是否成功
     */
    Boolean updateCategory(Long categoryId, ProductCategoryDO categoryDO);
    
    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     * @return 是否成功
     */
    Boolean deleteCategory(Long categoryId);
}
