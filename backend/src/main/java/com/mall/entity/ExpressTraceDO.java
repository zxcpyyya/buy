package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流轨迹表实体类
 *
 * @author xiu
 * @date 2026/09/03
 */
@Data
@TableName("express_trace")
public class ExpressTraceDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 轨迹ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物流ID
     */
    private Long expressId;

    /**
     * 状态：0-待发货，1-运输中，2-派送中，3-已签收，4-拒收/退回
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusName;

    /**
     * 地点
     */
    private String location;

    /**
     * 详细描述
     */
    private String message;

    /**
     * 轨迹时间
     */
    private LocalDateTime traceTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
