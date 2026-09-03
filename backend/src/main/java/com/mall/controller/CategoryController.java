package com.mall.controller;

import com.mall.common.result.Result;
import com.mall.service.ProductCategoryService;
import com.mall.vo.ProductCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类Controller
 * 
 * 遵循RESTful API设计规范和阿里Java开发规约
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "商品分类管理", description = "商品分类查询接口")
public class CategoryController {
    
    /**
     * 商品分类服务
     */
    private final ProductCategoryService categoryService;
    
    /**
     * 获取分类树
     * 
     * GET /api/category/tree
     */
    @GetMapping("/tree")
    @Operation(summary = "分类树", description = "获取所有分类的树形结构")
    public Result<List<ProductCategoryVO>> getCategoryTree() {
        log.debug("查询分类树请求");
        
        List<ProductCategoryVO> tree = categoryService.getCategoryTree();
        
        return Result.success(tree);
    }
    
    /**
     * 获取一级分类
     * 
     * GET /api/category/first
     */
    @GetMapping("/first")
    @Operation(summary = "一级分类", description = "获取所有一级分类")
    public Result<List<ProductCategoryVO>> getFirstLevelCategories() {
        log.debug("查询一级分类请求");
        
        List<ProductCategoryVO> categories = categoryService.getFirstLevelCategories();
        
        return Result.success(categories);
    }
    
    /**
     * 根据父分类获取子分类
     * 
     * GET /api/category/children/{parentId}
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "子分类", description = "根据父分类ID获取子分类")
    public Result<List<ProductCategoryVO>> getCategoriesByParentId(
            @Parameter(description = "父分类ID")
            @PathVariable Long parentId) {
        log.debug("查询子分类请求, parentId={}", parentId);
        
        List<ProductCategoryVO> categories = categoryService.getCategoriesByParentId(parentId);
        
        return Result.success(categories);
    }
    
    /**
     * 获取分类详情
     * 
     * GET /api/category/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "分类详情", description = "获取分类详细信息")
    public Result<ProductCategoryVO> getCategoryDetail(
            @Parameter(description = "分类ID")
            @PathVariable Long id) {
        log.debug("查询分类详情请求, categoryId={}", id);
        
        ProductCategoryVO categoryVO = categoryService.getCategoryDetail(id);
        
        return Result.success(categoryVO);
    }
}
