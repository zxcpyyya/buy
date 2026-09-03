package com.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 收货地址DTO
 * 
 * @author mall
 * @date 2024/01/01
 */
@Data
public class AddressDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 地址ID（更新时使用）
     */
    private Long id;
    
    /**
     * 收货人
     */
    @NotBlank(message = "收货人不能为空")
    private String consignee;
    
    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    private String province;
    
    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    private String city;
    
    /**
     * 区县
     */
    @NotBlank(message = "区县不能为空")
    private String district;
    
    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;
    
    /**
     * 是否默认：0-否，1-是
     */
    private Integer isDefault;
    
    /**
     * 地址标签（家、公司等）
     */
    private String label;
}
