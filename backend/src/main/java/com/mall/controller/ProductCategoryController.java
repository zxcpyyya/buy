package com.mall.controller;

import com.mall.common.result.Result;
import com.mall.service.ProductCategoryService;
import com.mall.vo.ProductCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类Controller
 * 
 * 
 * 1. RESTful风格设计
 * 2. 统一响应封装
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "商品分类管理", description = "商品分类查询、管理接口")
public class ProductCategoryController {
    
    /**
     * 商品分类服务
     */
    private final ProductCategoryService categoryService;
    
    /**
     * 获取分类树
     *
     * @return 分类树形结构
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取所有商品分类的树形结构")
    public Result<List<ProductCategoryVO>> getCategoryTree() {
        List<ProductCategoryVO> tree = categoryService.getCategoryTree();
        
        return Result.success(tree);
    }
    
    /**
     * 获取一级分类
     *
     * @return 一级分类列表
     */
    @GetMapping("/first")
    @Operation(summary = "获取一级分类", description = "获取所有一级商品分类")
    public Result<List<ProductCategoryVO>> getFirstLevelCategories() {
        List<ProductCategoryVO> categories = categoryService.getFirstLevelCategories();
        
        return Result.success(categories);
    }
    
    /**
     * 获取子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子分类", description = "根据父分类ID获取子分类列表")
    public Result<List<ProductCategoryVO>> getChildrenCategories(@PathVariable Long parentId) {
        List<ProductCategoryVO> categories = categoryService.getCategoriesByParentId(parentId);
        
        return Result.success(categories);
    }
    
    /**
     * 获取分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情
     */
    @GetMapping("/detail/{categoryId}")
    @Operation(summary = "获取分类详情", description = "根据分类ID获取分类详细信息")
    public Result<ProductCategoryVO> getCategoryDetail(@PathVariable Long categoryId) {
        ProductCategoryVO category = categoryService.getCategoryDetail(categoryId);
        
        return Result.success(category);
    }
}
