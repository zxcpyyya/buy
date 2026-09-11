package com.mall.controller;

import com.mall.common.annotation.RequireLogin;
import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.service.HistoryService;
import com.mall.vo.BrowseHistoryVO;
import com.mall.vo.SearchHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户历史记录Controller
 *
 * @author xiu
 */
@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "用户历史记录", description = "浏览历史、搜索历史接口")
public class HistoryController {

    private final HistoryService historyService;

    // ==================== 浏览历史 ====================

    /**
     * 添加浏览记录
     * POST /api/history/browse/{productId}
     */
    @PostMapping("/browse/{productId}")
    @RequireLogin
    @Operation(summary = "添加浏览记录", description = "记录用户浏览的商品")
    public Result<Void> addBrowseHistory(
            @Parameter(description = "商品ID")
            @PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        historyService.addBrowseHistory(userId, productId);
        return Result.success();
    }

    /**
     * 获取浏览历史
     * GET /api/history/browse
     */
    @GetMapping("/browse")
    @RequireLogin
    @Operation(summary = "浏览历史", description = "获取用户的浏览历史记录")
    public Result<List<BrowseHistoryVO>> getBrowseHistory(
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = UserContext.getUserId();
        List<BrowseHistoryVO> history = historyService.getBrowseHistory(userId, limit);
        return Result.success(history);
    }

    /**
     * 清空浏览历史
     * DELETE /api/history/browse
     */
    @DeleteMapping("/browse")
    @RequireLogin
    @Operation(summary = "清空浏览历史", description = "清空用户的所有浏览历史")
    public Result<Void> clearBrowseHistory() {
        Long userId = UserContext.getUserId();
        historyService.clearBrowseHistory(userId);
        return Result.success("已清空浏览历史");
    }

    // ==================== 搜索历史 ====================

    /**
     * 添加搜索历史
     * POST /api/history/search
     */
    @PostMapping("/search")
    @RequireLogin
    @Operation(summary = "添加搜索历史", description = "记录用户的搜索关键词")
    public Result<Void> addSearchHistory(
            @Parameter(description = "搜索关键词")
            @RequestParam String keyword) {
        Long userId = UserContext.getUserId();
        historyService.addSearchHistory(userId, keyword);
        return Result.success();
    }

    /**
     * 获取搜索历史
     * GET /api/history/search
     */
    @GetMapping("/search")
    @RequireLogin
    @Operation(summary = "搜索历史", description = "获取用户的搜索历史记录")
    public Result<List<SearchHistoryVO>> getSearchHistory(
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = UserContext.getUserId();
        List<SearchHistoryVO> history = historyService.getSearchHistory(userId, limit);
        return Result.success(history);
    }

    /**
     * 清空搜索历史
     * DELETE /api/history/search
     */
    @DeleteMapping("/search")
    @RequireLogin
    @Operation(summary = "清空搜索历史", description = "清空用户的所有搜索历史")
    public Result<Void> clearSearchHistory() {
        Long userId = UserContext.getUserId();
        historyService.clearSearchHistory(userId);
        return Result.success("已清空搜索历史");
    }

    /**
     * 删除单条搜索历史
     * DELETE /api/history/search/{keyword}
     */
    @DeleteMapping("/search/{keyword}")
    @RequireLogin
    @Operation(summary = "删除搜索历史", description = "删除用户指定关键词的搜索历史")
    public Result<Void> deleteSearchHistory(
            @Parameter(description = "搜索关键词")
            @PathVariable String keyword) {
        Long userId = UserContext.getUserId();
        historyService.deleteSearchHistory(userId, keyword);
        return Result.success();
    }
}
