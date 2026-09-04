package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.entity.ProductCategoryDO;
import com.mall.mapper.ProductCategoryMapper;
import com.mall.service.ProductCategoryService;
import com.mall.vo.ProductCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品分类Service实现类
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {
    
    private final ProductCategoryMapper categoryMapper;
    
    /**
     * 获取所有分类（树形结构）
     */
    @Override
    public List<ProductCategoryVO> getCategoryTree() {
        // 查询所有分类
        List<ProductCategoryDO> allCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<ProductCategoryDO>()
                .eq(ProductCategoryDO::getStatus, 1)
                .orderByAsc(ProductCategoryDO::getSort)
        );
        
        // 使用isEmpty()而非size()==0
        if (CollectionUtils.isEmpty(allCategories)) {
            return new ArrayList<>();
        }
        
        // 构建树形结构
        return this.buildCategoryTree(allCategories, 0L);
    }
    
    /**
     * 获取所有一级分类
     */
    @Override
    public List<ProductCategoryVO> getFirstLevelCategories() {
        LambdaQueryWrapper<ProductCategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategoryDO::getParentId, 0L)
            .eq(ProductCategoryDO::getStatus, 1)
            .orderByAsc(ProductCategoryDO::getSort);
        
        List<ProductCategoryDO> categories = categoryMapper.selectList(wrapper);
        
        return categories.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据父分类ID获取子分类
     */
    @Override
    public List<ProductCategoryVO> getCategoriesByParentId(Long parentId) {
        LambdaQueryWrapper<ProductCategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategoryDO::getParentId, parentId)
            .eq(ProductCategoryDO::getStatus, 1)
            .orderByAsc(ProductCategoryDO::getSort);
        
        List<ProductCategoryDO> categories = categoryMapper.selectList(wrapper);
        
        return categories.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取分类详情
     */
    @Override
    public ProductCategoryVO getCategoryDetail(Long categoryId) {
        ProductCategoryDO categoryDO = categoryMapper.selectById(categoryId);
        if (Objects.isNull(categoryDO)) {
            throw new BusinessException("A0401", "分类不存在");
        }
        
        return this.convertToVO(categoryDO);
    }
    
    /**
     * 创建分类
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createCategory(ProductCategoryDO categoryDO) {
        // 设置层级
        if (categoryDO.getParentId() == 0L) {
            categoryDO.setLevel(1);
        } else {
            ProductCategoryDO parent = categoryMapper.selectById(categoryDO.getParentId());
            if (Objects.isNull(parent)) {
                throw new BusinessException("A0401", "父分类不存在");
            }
            categoryDO.setLevel(parent.getLevel() + 1);
        }
        
        categoryDO.setStatus(1);
        categoryMapper.insert(categoryDO);
        
        log.info("创建分类成功, categoryId={}, name={}", categoryDO.getId(), categoryDO.getName());
        
        return categoryDO.getId();
    }
    
    /**
     * 更新分类
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateCategory(Long categoryId, ProductCategoryDO categoryDO) {
        ProductCategoryDO existCategory = categoryMapper.selectById(categoryId);
        if (Objects.isNull(existCategory)) {
            throw new BusinessException("A0401", "分类不存在");
        }
        
        existCategory.setName(categoryDO.getName());
        existCategory.setIcon(categoryDO.getIcon());
        existCategory.setSort(categoryDO.getSort());
        existCategory.setStatus(categoryDO.getStatus());
        
        return categoryMapper.updateById(existCategory) > 0;
    }
    
    /**
     * 删除分类
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteCategory(Long categoryId) {
        ProductCategoryDO categoryDO = categoryMapper.selectById(categoryId);
        if (Objects.isNull(categoryDO)) {
            throw new BusinessException("A0401", "分类不存在");
        }
        
        // 检查是否有子分类
        long childCount = categoryMapper.selectCount(
            new LambdaQueryWrapper<ProductCategoryDO>()
                .eq(ProductCategoryDO::getParentId, categoryId)
        );
        
        if (childCount > 0) {
            throw new BusinessException("A0440", "请先删除子分类");
        }
        
        return categoryMapper.deleteById(categoryId) > 0;
    }
    
    /**
     * 构建分类树
     */
    private List<ProductCategoryVO> buildCategoryTree(List<ProductCategoryDO> allCategories, Long parentId) {
        List<ProductCategoryVO> tree = new ArrayList<>();
        
        List<ProductCategoryDO> children = allCategories.stream()
            .filter(c -> Objects.equals(c.getParentId(), parentId))
            .collect(Collectors.toList());
        
        for (ProductCategoryDO category : children) {
            ProductCategoryVO vo = this.convertToVO(category);
            
            // 递归获取子分类
            List<ProductCategoryVO> childTree = this.buildCategoryTree(allCategories, category.getId());
            if (!CollectionUtils.isEmpty(childTree)) {
                vo.setChildren(childTree);
            }
            
            tree.add(vo);
        }
        
        return tree;
    }
    
    /**
     * 转换为VO
     */
    private ProductCategoryVO convertToVO(ProductCategoryDO categoryDO) {
        ProductCategoryVO vo = BeanUtil.copyProperties(categoryDO, ProductCategoryVO.class);
        return vo;
    }
}
