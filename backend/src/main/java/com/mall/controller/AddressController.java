package com.mall.controller;

import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.dto.AddressDTO;
import com.mall.vo.AddressVO;
import com.mall.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址Controller
 * 
 * 遵循RESTful API设计规范和阿里Java开发规约
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Tag(name = "收货地址管理", description = "收货地址CRUD接口")
public class AddressController {
    
    /**
     * 收货地址服务
     */
    private final AddressService addressService;
    
    /**
     * 获取用户收货地址列表
     * 
     * GET /api/address
     */
    @GetMapping
    @Operation(summary = "地址列表", description = "获取当前用户收货地址列表")
    public Result<List<AddressVO>> getUserAddresses() {
        Long userId = UserContext.getUserId();
        
        log.debug("查询收货地址列表请求, userId={}", userId);
        
        List<AddressVO> addresses = addressService.getUserAddresses(userId);
        
        return Result.success(addresses);
    }
    
    /**
     * 获取收货地址详情
     * 
     * GET /api/address/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "地址详情", description = "获取收货地址详细信息")
    public Result<AddressVO> getAddressDetail(
            @Parameter(description = "地址ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.debug("查询收货地址详情请求, userId={}, addressId={}", userId, id);
        
        AddressVO addressVO = addressService.getAddressDetail(id, userId);
        
        return Result.success(addressVO);
    }
    
    /**
     * 创建收货地址
     * 
     * POST /api/address
     */
    @PostMapping
    @Operation(summary = "创建地址", description = "创建新收货地址")
    public Result<Long> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        Long userId = UserContext.getUserId();
        
        log.info("创建收货地址请求, userId={}, consignee={}", 
            userId, addressDTO.getConsignee());
        
        Long addressId = addressService.createAddress(addressDTO, userId);
        
        return Result.success("创建成功", addressId);
    }
    
    /**
     * 更新收货地址
     * 
     * PUT /api/address/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新地址", description = "更新收货地址信息")
    public Result<Boolean> updateAddress(
            @Parameter(description = "地址ID")
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        Long userId = UserContext.getUserId();
        
        log.info("更新收货地址请求, userId={}, addressId={}", userId, id);
        
        Boolean result = addressService.updateAddress(id, addressDTO, userId);
        
        return Result.success(result);
    }
    
    /**
     * 删除收货地址
     * 
     * DELETE /api/address/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除收货地址")
    public Result<Boolean> deleteAddress(
            @Parameter(description = "地址ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.info("删除收货地址请求, userId={}, addressId={}", userId, id);
        
        Boolean result = addressService.deleteAddress(id, userId);
        
        return Result.success(result);
    }
    
    /**
     * 设置默认收货地址
     * 
     * PUT /api/address/{id}/default
     */
    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认", description = "设置默认收货地址")
    public Result<Boolean> setDefaultAddress(
            @Parameter(description = "地址ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.info("设置默认收货地址请求, userId={}, addressId={}", userId, id);
        
        Boolean result = addressService.setDefaultAddress(id, userId);
        
        return Result.success(result);
    }
}
