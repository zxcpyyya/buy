package com.mall.common.constant;

/**
 * 支付方式枚举
 * 
 * @author xiu
 * @date 2024/01/01
 */
public enum PayTypeEnum {
    
    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝");
    
    private final Integer code;
    private final String description;
    
    PayTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据code获取枚举
     */
    public static PayTypeEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (PayTypeEnum payType : values()) {
            if (payType.getCode().equals(code)) {
                return payType;
            }
        }
        return null;
    }
}
