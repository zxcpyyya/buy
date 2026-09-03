package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收货地址表实体类
 * 
 * @author mall
 * @date 2024/01/01
 */
@Data
@TableName("address")
public class AddressDO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 地址ID
     *
     * 重要：广播表使用雪花算法保证全局唯一
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;
}
