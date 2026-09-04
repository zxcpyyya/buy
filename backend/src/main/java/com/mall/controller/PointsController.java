package com.mall.controller;

import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.service.PointsService;
import com.mall.vo.PointsRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分Controller
 *
 * @author mall
 */
@Slf4j
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Tag(name = "积分管理", description = "积分查询、使用接口")
public class PointsController {

    private final PointsService pointsService;

    /**
     * 获取用户积分信息
     *
     * GET /api/points
     */
    @GetMapping
    @Operation(summary = "积分信息", description = "获取用户当前积分和累计积分")
    public Result<Map<String, Object>> getUserPoints() {
        Long userId = UserContext.getUserId();

        Integer balance = pointsService.getUserPoints(userId);
        Integer total = pointsService.getTotalPoints(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("balance", balance);
        data.put("total", total);

        return Result.success(data);
    }

    /**
     * 获取积分记录
     *
     * GET /api/points/records
     */
    @GetMapping("/records")
    @Operation(summary = "积分记录", description = "获取用户积分变动记录")
    public Result<List<PointsRecordVO>> getPointsRecords(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.getUserId();
        List<PointsRecordVO> records = pointsService.getPointsRecords(userId, pageNum, pageSize);
        return Result.success(records);
    }
}
