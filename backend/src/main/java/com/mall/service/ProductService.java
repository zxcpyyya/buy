package com.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.dto.ProductQueryDTO;
import com.mall.entity.ProductDO;
import com.mall.vo.ProductVO;

import java.util.List;

/**
 * 商品Service接口
 * 
 * @author xiu
 * @date 2024/01/01
 */
public interface ProductService {
    
    /**
     * 分页查询商品列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<ProductVO> pageProduct(ProductQueryDTO queryDTO);
    
    /**
     * 获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情VO
     */
    ProductVO getProductDetail(Long productId);
    
    /**
     * 获取热门商品
     *
     * @param limit 数量限制
     * @return 商品列表
     */
    List<ProductVO> getHotProducts(Integer limit);
    
    /**
     * 获取新品上市
     *
     * @param limit 数量限制
     * @return 商品列表
     */
    List<ProductVO> getNewProducts(Integer limit);
    
    /**
     * 根据分类ID查询商品
     *
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<ProductVO> getProductsByCategory(Long categoryId, Integer pageNum, Integer pageSize);
    
    /**
     * 搜索商品
     *
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<ProductVO> searchProducts(String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 创建商品
     *
     * @param productDTO 商品信息
     * @return 商品ID
     */
    Long createProduct(ProductDTO productDTO);
    
    /**
     * 更新商品
     *
     * @param productId 商品ID
     * @param productDTO 商品信息
     * @return 是否成功
     */
    Boolean updateProduct(Long productId, ProductDTO productDTO);
    
    /**
     * 删除商品
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    Boolean deleteProduct(Long productId);
    
    /**
     * 更新商品库存
     *
     * @param productId 商品ID
     * @param quantity 变更数量（正数为增加，负数为减少）
     * @return 是否成功
     */
    Boolean updateStock(Long productId, Integer quantity);
}
