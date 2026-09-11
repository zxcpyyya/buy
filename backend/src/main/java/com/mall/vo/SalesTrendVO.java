package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销售趋势VO
 *
 * @author xiu
 */
@Data
public class SalesTrendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 趋势数据列表
     */
    private List<TrendData> trendData;

    @Data
    public static class TrendData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 日期
         */
        private String date;

        /**
         * 订单数
         */
        private Integer orders;

        /**
         * 销售额
         */
        private BigDecimal sales;
    }
}
