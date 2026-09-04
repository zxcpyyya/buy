package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分记录表实体类
 *
 * @author xiu
 * @date 2024/01/01
 */
@Data
@TableName("points_record")
public class PointsRecordDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 积分变动数量（正数=获取，负数=使用）
     */
    private Integer points;

    /**
     * 变动后余额
     */
    private Integer balance;

    /**
     * 类型：1-订单获取，2-订单使用，3-活动赠送，4-过期扣除
     */
    private Integer type;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 描述/备注
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
