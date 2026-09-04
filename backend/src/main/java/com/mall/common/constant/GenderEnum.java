package com.mall.common.constant;

/**
 * 性别枚举
 * 
 * @author xiu
 * @date 2026/09/03
 */
public enum GenderEnum {
    
    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");
    
    private final Integer code;
    private final String description;
    
    GenderEnum(Integer code, String description) {
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
    public static GenderEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (GenderEnum gender : values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        return null;
    }
}
