package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收货地址VO
 * 
 * @author mall
 * @date 2024/01/01
 */
@Data
public class AddressVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 地址ID
     */
    private Long id;
    
    /**
     * 收货人
     */
    private String consignee;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String detailAddress;
    
    /**
     * 完整地址
     */
    private String fullAddress;
    
    /**
     * 是否默认：0-否，1-是
     */
    private Integer isDefault;
    
    /**
     * 地址标签（家、公司等）
     */
    private String label;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
