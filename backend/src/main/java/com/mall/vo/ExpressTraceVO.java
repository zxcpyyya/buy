package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流轨迹VO
 *
 * @author xiu
 */
@Data
public class ExpressTraceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 物流ID
     */
    private Long expressId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
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
}
