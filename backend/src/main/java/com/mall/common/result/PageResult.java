package com.mall.common.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 * 
 * @author mall
 * @date 2024/01/01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<List<T>> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 当前页码
     */
    private Integer current;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    public PageResult() {
    }
    
    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Integer current, Integer size) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setCode(200);
        pageResult.setMessage("查询成功");
        pageResult.setData(records);
        pageResult.setTotal(total);
        pageResult.setCurrent(current);
        pageResult.setSize(size);
        
        int pages = (int) Math.ceil((double) total / size);
        pageResult.setPages(pages);
        pageResult.setHasNext(current < pages);
        pageResult.setHasPrevious(current > 1);
        
        return pageResult;
    }
}
