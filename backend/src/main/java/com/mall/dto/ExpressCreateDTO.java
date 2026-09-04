package com.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流创建DTO
 *
 * @author xiu
 */
@Data
public class ExpressCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 快递单号
     */
    @NotBlank(message = "快递单号不能为空")
    private String expressNo;

    /**
     * 快递公司编码
     */
    @NotBlank(message = "快递公司编码不能为空")
    private String companyCode;

    /**
     * 快递公司名称
     */
    @NotBlank(message = "快递公司名称不能为空")
    private String companyName;

    /**
     * 收货人
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;
}
