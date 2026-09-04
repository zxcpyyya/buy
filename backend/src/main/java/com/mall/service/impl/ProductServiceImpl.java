package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.dto.ProductDTO;
import com.mall.dto.ProductQueryDTO;
import com.mall.entity.ProductCategoryDO;
import com.mall.entity.ProductDO;
import com.mall.mapper.ProductCategoryMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.ProductService;
import com.mall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品Service实现类
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    
    /**
     * 分页查询商品列表
     */
    @Override
    public Page<ProductVO> pageProduct(ProductQueryDTO queryDTO) {
        Page<ProductDO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(ProductDO::getName, queryDTO.getKeyword())
                .or()
                .like(ProductDO::getDescription, queryDTO.getKeyword()));
        }
        
        // 分类筛选
        if (Objects.nonNull(queryDTO.getCategoryId())) {
            wrapper.eq(ProductDO::getCategoryId, queryDTO.getCategoryId());
        }
        
        // 价格筛选
        if (Objects.nonNull(queryDTO.getMinPrice())) {
            wrapper.ge(ProductDO::getPrice, queryDTO.getMinPrice());
        }
        if (Objects.nonNull(queryDTO.getMaxPrice())) {
            wrapper.le(ProductDO::getPrice, queryDTO.getMaxPrice());
        }
        
        // 只查询上架商品
        wrapper.eq(ProductDO::getStatus, 1);
        
        // 排序
        if (StringUtils.hasText(queryDTO.getSortBy())) {
            if ("price".equals(queryDTO.getSortBy())) {
                if ("asc".equalsIgnoreCase(queryDTO.getSortOrder())) {
                    wrapper.orderByAsc(ProductDO::getPrice);
                } else {
                    wrapper.orderByDesc(ProductDO::getPrice);
                }
            } else if ("sales".equals(queryDTO.getSortBy())) {
                wrapper.orderByDesc(ProductDO::getSales);
            } else if ("createTime".equals(queryDTO.getSortBy())) {
                wrapper.orderByDesc(ProductDO::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(ProductDO::getCreateTime);
        }
        
        Page<ProductDO> result = productMapper.selectPage(page, wrapper);
        
        return this.convertToVOPage(result);
    }
    
    /**
     * 获取商品详情
     */
    @Override
    public ProductVO getProductDetail(Long productId) {
        ProductDO productDO = productMapper.selectById(productId);
        if (Objects.isNull(productDO)) {
            throw new BusinessException("A0401", "商品不存在");
        }
        
        if (productDO.getStatus() == 0) {
            throw new BusinessException("A0401", "商品已下架");
        }
        
        return this.convertToVO(productDO);
    }
    
    /**
     * 获取热门商品
     */
    @Override
    public List<ProductVO> getHotProducts(Integer limit) {
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductDO::getStatus, 1)
            .orderByDesc(ProductDO::getSales)
            .last("LIMIT " + limit);
        
        List<ProductDO> products = productMapper.selectList(wrapper);
        
        return products.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取新品上市
     */
    @Override
    public List<ProductVO> getNewProducts(Integer limit) {
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductDO::getStatus, 1)
            .orderByDesc(ProductDO::getCreateTime)
            .last("LIMIT " + limit);
        
        List<ProductDO> products = productMapper.selectList(wrapper);
        
        return products.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据分类ID查询商品
     */
    @Override
    public Page<ProductVO> getProductsByCategory(Long categoryId, Integer pageNum, Integer pageSize) {
        Page<ProductDO> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductDO::getCategoryId, categoryId)
            .eq(ProductDO::getStatus, 1)
            .orderByDesc(ProductDO::getCreateTime);
        
        Page<ProductDO> result = productMapper.selectPage(page, wrapper);
        
        return this.convertToVOPage(result);
    }
    
    /**
     * 搜索商品
     */
    @Override
    public Page<ProductVO> searchProducts(String keyword, Integer pageNum, Integer pageSize) {
        Page<ProductDO> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(ProductDO::getName, keyword)
            .or()
            .like(ProductDO::getDescription, keyword))
            .eq(ProductDO::getStatus, 1)
            .orderByDesc(ProductDO::getSales);
        
        Page<ProductDO> result = productMapper.selectPage(page, wrapper);
        
        return this.convertToVOPage(result);
    }
    
    /**
     * 创建商品
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createProduct(ProductDTO productDTO) {
        // 校验分类是否存在
        if (Objects.nonNull(productDTO.getCategoryId())) {
            ProductCategoryDO category = categoryMapper.selectById(productDTO.getCategoryId());
            if (Objects.isNull(category)) {
                throw new BusinessException("A0401", "商品分类不存在");
            }
        }
        
        // 构建商品实体
        ProductDO productDO = new ProductDO();
        productDO.setName(productDTO.getName());
        productDO.setCategoryId(productDTO.getCategoryId());
        productDO.setPrice(productDTO.getPrice());
        productDO.setStock(productDTO.getStock());
        productDO.setImage(productDTO.getImage());
        productDO.setImages(productDTO.getImages());
        productDO.setDescription(productDTO.getDescription());
        productDO.setStatus(productDTO.getStatus() != null ? productDTO.getStatus() : 1);
        productDO.setSales(0);
        
        productMapper.insert(productDO);
        
        log.info("创建商品成功, productId={}", productDO.getId());
        
        return productDO.getId();
    }
    
    /**
     * 更新商品
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateProduct(Long productId, ProductDTO productDTO) {
        ProductDO productDO = productMapper.selectById(productId);
        if (Objects.isNull(productDO)) {
            throw new BusinessException("A0401", "商品不存在");
        }
        
        // 更新字段
        if (StringUtils.hasText(productDTO.getName())) {
            productDO.setName(productDTO.getName());
        }
        if (Objects.nonNull(productDTO.getCategoryId())) {
            productDO.setCategoryId(productDTO.getCategoryId());
        }
        if (Objects.nonNull(productDTO.getPrice())) {
            productDO.setPrice(productDTO.getPrice());
        }
        if (Objects.nonNull(productDTO.getStock())) {
            productDO.setStock(productDTO.getStock());
        }
        if (StringUtils.hasText(productDTO.getImage())) {
            productDO.setImage(productDTO.getImage());
        }
        if (StringUtils.hasText(productDTO.getImages())) {
            productDO.setImages(productDTO.getImages());
        }
        if (StringUtils.hasText(productDTO.getDescription())) {
            productDO.setDescription(productDTO.getDescription());
        }
        if (Objects.nonNull(productDTO.getStatus())) {
            productDO.setStatus(productDTO.getStatus());
        }
        
        return productMapper.updateById(productDO) > 0;
    }
    
    /**
     * 删除商品
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteProduct(Long productId) {
        ProductDO productDO = productMapper.selectById(productId);
        if (Objects.isNull(productDO)) {
            throw new BusinessException("A0401", "商品不存在");
        }
        
        return productMapper.deleteById(productId) > 0;
    }
    
    /**
     * 更新商品库存
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateStock(Long productId, Integer quantity) {
        ProductDO productDO = productMapper.selectById(productId);
        if (Objects.isNull(productDO)) {
            throw new BusinessException("A0401", "商品不存在");
        }
        
        // 计算新库存（货币金额使用BigDecimal，但库存使用Integer）
        int newStock = productDO.getStock() + quantity;
        if (newStock < 0) {
            throw new BusinessException("A0401", "库存不足");
        }
        
        LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ProductDO::getId, productId)
            .set(ProductDO::getStock, newStock);
        
        return productMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 转换为VO对象
     */
    private ProductVO convertToVO(ProductDO productDO) {
        ProductVO productVO = BeanUtil.copyProperties(productDO, ProductVO.class);
        
        // 获取分类名称
        if (Objects.nonNull(productDO.getCategoryId())) {
            ProductCategoryDO category = categoryMapper.selectById(productDO.getCategoryId());
            if (Objects.nonNull(category)) {
                productVO.setCategoryName(category.getName());
            }
        }
        
        // 解析图片列表
        if (StringUtils.hasText(productDO.getImages())) {
            try {
                productVO.setImages(JSONUtil.toList(productDO.getImages(), String.class));
            } catch (Exception e) {
                log.error("解析商品图片失败, productId={}", productDO.getId(), e);
            }
        }
        
        return productVO;
    }
    
    /**
     * 转换为VO分页对象
     */
    private Page<ProductVO> convertToVOPage(Page<ProductDO> page) {
        Page<ProductVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList()));
        return result;
    }
}
