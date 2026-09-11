package com.mall.service;

import com.mall.vo.BrowseHistoryVO;
import com.mall.vo.SearchHistoryVO;

import java.util.List;

/**
 * 用户历史记录Service
 *
 * @author xiu
 */
public interface HistoryService {

    /**
     * 添加浏览记录
     *
     * @param userId 用户ID
     * @param productId 商品ID
     */
    void addBrowseHistory(Long userId, Long productId);

    /**
     * 获取浏览历史
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 浏览历史列表
     */
    List<BrowseHistoryVO> getBrowseHistory(Long userId, Integer limit);

    /**
     * 清空浏览历史
     *
     * @param userId 用户ID
     */
    void clearBrowseHistory(Long userId);

    /**
     * 添加搜索历史
     *
     * @param userId 用户ID
     * @param keyword 搜索关键词
     */
    void addSearchHistory(Long userId, String keyword);

    /**
     * 获取搜索历史
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 搜索历史列表
     */
    List<SearchHistoryVO> getSearchHistory(Long userId, Integer limit);

    /**
     * 清空搜索历史
     *
     * @param userId 用户ID
     */
    void clearSearchHistory(Long userId);

    /**
     * 删除单条搜索历史
     *
     * @param userId 用户ID
     * @param keyword 关键词
     */
    void deleteSearchHistory(Long userId, String keyword);
}
