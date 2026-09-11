package com.mall.controller;

import com.mall.common.annotation.RequireLogin;
import com.mall.common.annotation.RequirePermission;
import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.dto.CouponTemplateDTO;
import com.mall.service.CouponService;
import com.mall.vo.CouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券Controller
 *
 * @author xiu
 */
@Slf4j
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Tag(name = "优惠券管理", description = "优惠券领取、使用管理接口")
public class CouponController {

    private final CouponService couponService;

    /**
     * 获取用户优惠券列表
     *
     * GET /api/coupon
     */
    @GetMapping
    @Operation(summary = "优惠券列表", description = "获取用户优惠券列表")
    public Result<List<CouponVO>> getUserCoupons(
            @Parameter(description = "状态：null-全部，0-未使用，1-已使用，2-已过期")
            @RequestParam(required = false) Integer status) {
        Long userId = UserContext.getUserId();
        List<CouponVO> coupons = couponService.getUserCoupons(userId, status);
        return Result.success(coupons);
    }

    /**
     * 获取可用优惠券（订单确认页）
     *
     * GET /api/coupon/available
     */
    @GetMapping("/available")
    @Operation(summary = "可用优惠券", description = "获取订单可用的优惠券")
    public Result<List<CouponVO>> getAvailableCoupons(
            @Parameter(description = "订单金额")
            @RequestParam BigDecimal orderAmount) {
        Long userId = UserContext.getUserId();
        List<CouponVO> coupons = couponService.getAvailableCoupons(userId, orderAmount);
        return Result.success(coupons);
    }

    /**
     * 领取优惠券
     *
     * POST /api/coupon/receive
     */
    @PostMapping("/receive")
    @Operation(summary = "领取优惠券", description = "领取优惠券")
    public Result<Long> receiveCoupon(
            @Parameter(description = "模板ID")
            @RequestParam Long templateId) {
        Long userId = UserContext.getUserId();
        Long couponId = couponService.receiveCoupon(templateId, userId);
        return Result.success("领取成功", couponId);
    }

    /**
     * 获取可领取的优惠券
     *
     * GET /api/coupon/templates
     */
    @GetMapping("/templates")
    @Operation(summary = "优惠券模板列表", description = "获取可领取的优惠券列表")
    public Result<List<CouponVO>> getAvailableTemplates() {
        Long userId = UserContext.getUserId();
        List<CouponVO> templates = couponService.getAvailableTemplates(userId);
        return Result.success(templates);
    }

    /**
     * 获取优惠券模板详情
     *
     * GET /api/coupon/template/{id}
     */
    @GetMapping("/template/{id}")
    @Operation(summary = "模板详情", description = "获取优惠券模板详情")
    public Result<CouponVO> getTemplateDetail(
            @Parameter(description = "模板ID")
            @PathVariable Long id) {
        CouponVO template = couponService.getTemplateDetail(id);
        return Result.success(template);
    }

    // ========== 管理员接口 ==========

    /**
     * 创建优惠券模板（管理员）
     *
     * POST /api/coupon/template
     */
    @PostMapping("/template")
    @RequireLogin
    @RequirePermission("coupon:create")
    @Operation(summary = "创建模板", description = "创建优惠券模板")
    public Result<Long> createTemplate(@Valid @RequestBody CouponTemplateDTO dto) {
        log.info("创建优惠券模板请求, name={}", dto.getName());
        Long templateId = couponService.createTemplate(dto);
        return Result.success("创建成功", templateId);
    }

    /**
     * 更新优惠券模板（管理员）
     *
     * PUT /api/coupon/template/{id}
     */
    @PutMapping("/template/{id}")
    @RequireLogin
    @RequirePermission("coupon:update")
    @Operation(summary = "更新模板", description = "更新优惠券模板")
    public Result<Boolean> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CouponTemplateDTO dto) {
        log.info("更新优惠券模板请求, id={}", id);
        Boolean result = couponService.updateTemplate(id, dto);
        return Result.success(result);
    }

    /**
     * 删除优惠券模板（管理员）
     *
     * DELETE /api/coupon/template/{id}
     */
    @DeleteMapping("/template/{id}")
    @RequireLogin
    @RequirePermission("coupon:delete")
    @Operation(summary = "删除模板", description = "删除优惠券模板")
    public Result<Boolean> deleteTemplate(
            @PathVariable Long id) {
        log.info("删除优惠券模板请求, id={}", id);
        Boolean result = couponService.deleteTemplate(id);
        return Result.success(result);
    }

    /**
     * 分页查询优惠券模板（管理员）
     *
     * GET /api/coupon/template/list
     */
    @GetMapping("/template/list")
    @RequireLogin
    @RequirePermission("coupon:list")
    @Operation(summary = "模板列表", description = "分页查询优惠券模板")
    public Result<com.mall.common.result.PageResult<CouponVO>> getTemplateList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        var result = couponService.getTemplateList(pageNum, pageSize);
        return Result.success(result);
    }
}
