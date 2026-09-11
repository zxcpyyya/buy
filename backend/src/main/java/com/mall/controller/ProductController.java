package com.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.dto.ProductDTO;
import com.mall.dto.ProductQueryDTO;
import com.mall.service.ProductService;
import com.mall.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品Controller
 * 
 * 
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品查询、搜索、管理接口")
public class ProductController {
    
    /**
     * 商品服务
     */
    private final ProductService productService;
    
    /**
     * 分页查询商品列表
     * 
     * GET /api/product/list
     */
    @GetMapping("/list")
    @Operation(summary = "商品列表", description = "分页查询商品列表")
    public Result<PageResult<ProductVO>> getProductList(ProductQueryDTO queryDTO) {
        log.debug("查询商品列表请求, queryDTO={}", queryDTO);
        
        Page<ProductVO> page = productService.pageProduct(queryDTO);
        
        PageResult<ProductVO> pageResult = PageResult.of(
            page.getRecords(),
            page.getTotal(),
            (int) page.getCurrent(),
            (int) page.getSize()
        );
        
        return Result.success(pageResult);
    }
    
    /**
     * 获取商品详情
     * 
     * GET /api/product/detail/{id}
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "商品详情", description = "获取商品详细信息")
    public Result<ProductVO> getProductDetail(
            @Parameter(description = "商品ID")
            @PathVariable Long id) {
        log.debug("查询商品详情请求, productId={}", id);
        
        ProductVO productVO = productService.getProductDetail(id);
        
        return Result.success(productVO);
    }
    
    /**
     * 获取热门商品
     * 
     * GET /api/product/hot
     */
    @GetMapping("/hot")
    @Operation(summary = "热门商品", description = "获取热门销售商品")
    public Result<List<ProductVO>> getHotProducts(
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("查询热门商品请求, limit={}", limit);
        
        List<ProductVO> products = productService.getHotProducts(limit);
        
        return Result.success(products);
    }
    
    /**
     * 获取新品上市
     * 
     * GET /api/product/new
     */
    @GetMapping("/new")
    @Operation(summary = "新品上市", description = "获取最新上架商品")
    public Result<List<ProductVO>> getNewProducts(
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("查询新品请求, limit={}", limit);
        
        List<ProductVO> products = productService.getNewProducts(limit);
        
        return Result.success(products);
    }
    
    /**
     * 根据分类查询商品
     * 
     * GET /api/product/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "分类商品", description = "根据分类查询商品")
    public Result<PageResult<ProductVO>> getProductsByCategory(
            @Parameter(description = "分类ID")
            @PathVariable Long categoryId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.debug("按分类查询商品请求, categoryId={}, pageNum={}, pageSize={}", 
            categoryId, pageNum, pageSize);
        
        Page<ProductVO> page = productService.getProductsByCategory(
            categoryId, pageNum, pageSize);
        
        PageResult<ProductVO> pageResult = PageResult.of(
            page.getRecords(),
            page.getTotal(),
            (int) page.getCurrent(),
            (int) page.getSize()
        );
        
        return Result.success(pageResult);
    }
    
    /**
     * 搜索商品
     * 
     * GET /api/product/search
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "关键词搜索商品")
    public Result<PageResult<ProductVO>> searchProducts(
            @Parameter(description = "搜索关键词")
            @RequestParam String keyword,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.debug("搜索商品请求, keyword={}, pageNum={}, pageSize={}", 
            keyword, pageNum, pageSize);
        
        Page<ProductVO> page = productService.searchProducts(
            keyword, pageNum, pageSize);
        
        PageResult<ProductVO> pageResult = PageResult.of(
            page.getRecords(),
            page.getTotal(),
            (int) page.getCurrent(),
            (int) page.getSize()
        );
        
        return Result.success(pageResult);
    }
    
    /**
     * 创建商品（管理员）
     *
     * POST /api/product
     */
    @PostMapping
    @RequireLogin
    @RequirePermission("product:create")
    @Operation(summary = "创建商品", description = "创建新商品")
    public Result<Long> createProduct(@RequestBody ProductDTO productDTO) {
        log.info("创建商品请求, name={}", productDTO.getName());

        Long productId = productService.createProduct(productDTO);

        return Result.success("创建成功", productId);
    }

    /**
     * 更新商品（管理员）
     *
     * PUT /api/product/{id}
     */
    @PutMapping("/{id}")
    @RequireLogin
    @RequirePermission("product:update")
    @Operation(summary = "更新商品", description = "更新商品信息")
    public Result<Boolean> updateProduct(
            @Parameter(description = "商品ID")
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {
        log.info("更新商品请求, productId={}", id);

        Boolean result = productService.updateProduct(id, productDTO);

        return Result.success(result);
    }

    /**
     * 删除商品（管理员）
     *
     * DELETE /api/product/{id}
     */
    @DeleteMapping("/{id}")
    @RequireLogin
    @RequirePermission("product:delete")
    @Operation(summary = "删除商品", description = "删除商品")
    public Result<Boolean> deleteProduct(
            @Parameter(description = "商品ID")
            @PathVariable Long id) {
        log.info("删除商品请求, productId={}", id);

        Boolean result = productService.deleteProduct(id);

        return Result.success(result);
    }
}
