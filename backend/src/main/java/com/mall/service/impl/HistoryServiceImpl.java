package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.entity.ProductBrowseHistoryDO;
import com.mall.entity.ProductDO;
import com.mall.entity.UserSearchHistoryDO;
import com.mall.mapper.ProductBrowseHistoryMapper;
import com.mall.mapper.ProductMapper;
import com.mall.mapper.UserSearchHistoryMapper;
import com.mall.service.HistoryService;
import com.mall.vo.BrowseHistoryVO;
import com.mall.vo.SearchHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户历史记录Service实现
 *
 * @author xiu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final ProductBrowseHistoryMapper browseHistoryMapper;
    private final UserSearchHistoryMapper searchHistoryMapper;
    private final ProductMapper productMapper;

    /**
     * 最大浏览历史数量
     */
    private static final int MAX_BROWSE_HISTORY = 50;

    /**
     * 最大搜索历史数量
     */
    private static final int MAX_SEARCH_HISTORY = 20;

    /**
     * 添加浏览记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBrowseHistory(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return;
        }

        // 先删除该用户对该商品的旧记录（如果存在）
        browseHistoryMapper.delete(new LambdaQueryWrapper<ProductBrowseHistoryDO>()
                .eq(ProductBrowseHistoryDO::getUserId, userId)
                .eq(ProductBrowseHistoryDO::getProductId, productId));

        // 添加新记录
        ProductBrowseHistoryDO history = new ProductBrowseHistoryDO();
        history.setUserId(userId);
        history.setProductId(productId);
        history.setBrowseTime(LocalDateTime.now());
        history.setCreateTime(LocalDateTime.now());
        browseHistoryMapper.insert(history);

        // 清理超出限制的旧记录
        cleanupOldBrowseHistory(userId);

        log.debug("添加浏览记录成功, userId={}, productId={}", userId, productId);
    }

    /**
     * 获取浏览历史
     */
    @Override
    public List<BrowseHistoryVO> getBrowseHistory(Long userId, Integer limit) {
        if (userId == null) {
            return List.of();
        }

        int queryLimit = (limit != null && limit > 0) ? Math.min(limit, MAX_BROWSE_HISTORY) : 10;

        List<ProductBrowseHistoryDO> histories = browseHistoryMapper.selectList(
                new LambdaQueryWrapper<ProductBrowseHistoryDO>()
                        .eq(ProductBrowseHistoryDO::getUserId, userId)
                        .orderByDesc(ProductBrowseHistoryDO::getBrowseTime)
                        .last("LIMIT " + queryLimit)
        );

        return histories.stream().map(history -> {
            BrowseHistoryVO vo = new BrowseHistoryVO();
            vo.setProductId(history.getProductId());
            vo.setBrowseTime(history.getBrowseTime());

            // 获取商品信息
            ProductDO product = productMapper.selectById(history.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductImage(product.getImage());
                vo.setPrice(product.getPrice());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 清空浏览历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearBrowseHistory(Long userId) {
        if (userId == null) {
            return;
        }

        browseHistoryMapper.delete(new LambdaQueryWrapper<ProductBrowseHistoryDO>()
                .eq(ProductBrowseHistoryDO::getUserId, userId));

        log.debug("清空浏览历史成功, userId={}", userId);
    }

    /**
     * 添加搜索历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSearchHistory(Long userId, String keyword) {
        if (userId == null || !StringUtils.hasText(keyword)) {
            return;
        }

        // 去除首尾空格
        keyword = keyword.trim();

        // 先删除该用户对该关键词的旧记录
        searchHistoryMapper.delete(new LambdaQueryWrapper<UserSearchHistoryDO>()
                .eq(UserSearchHistoryDO::getUserId, userId)
                .eq(UserSearchHistoryDO::getKeyword, keyword));

        // 添加新记录
        UserSearchHistoryDO history = new UserSearchHistoryDO();
        history.setUserId(userId);
        history.setKeyword(keyword);
        history.setSearchTime(LocalDateTime.now());
        history.setCreateTime(LocalDateTime.now());
        searchHistoryMapper.insert(history);

        // 清理超出限制的旧记录
        cleanupOldSearchHistory(userId);

        log.debug("添加搜索历史成功, userId={}, keyword={}", userId, keyword);
    }

    /**
     * 获取搜索历史
     */
    @Override
    public List<SearchHistoryVO> getSearchHistory(Long userId, Integer limit) {
        if (userId == null) {
            return List.of();
        }

        int queryLimit = (limit != null && limit > 0) ? Math.min(limit, MAX_SEARCH_HISTORY) : 10;

        List<UserSearchHistoryDO> histories = searchHistoryMapper.selectList(
                new LambdaQueryWrapper<UserSearchHistoryDO>()
                        .eq(UserSearchHistoryDO::getUserId, userId)
                        .orderByDesc(UserSearchHistoryDO::getSearchTime)
                        .last("LIMIT " + queryLimit)
        );

        return histories.stream().map(history -> {
            SearchHistoryVO vo = new SearchHistoryVO();
            vo.setKeyword(history.getKeyword());
            vo.setSearchTime(history.getSearchTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 清空搜索历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearSearchHistory(Long userId) {
        if (userId == null) {
            return;
        }

        searchHistoryMapper.delete(new LambdaQueryWrapper<UserSearchHistoryDO>()
                .eq(UserSearchHistoryDO::getUserId, userId));

        log.debug("清空搜索历史成功, userId={}", userId);
    }

    /**
     * 删除单条搜索历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSearchHistory(Long userId, String keyword) {
        if (userId == null || !StringUtils.hasText(keyword)) {
            return;
        }

        searchHistoryMapper.delete(new LambdaQueryWrapper<UserSearchHistoryDO>()
                .eq(UserSearchHistoryDO::getUserId, userId)
                .eq(UserSearchHistoryDO::getKeyword, keyword.trim()));

        log.debug("删除搜索历史成功, userId={}, keyword={}", userId, keyword);
    }

    /**
     * 清理超出限制的浏览历史
     */
    private void cleanupOldBrowseHistory(Long userId) {
        // 查询该用户的浏览历史数量
        Long count = browseHistoryMapper.selectCount(new LambdaQueryWrapper<ProductBrowseHistoryDO>()
                .eq(ProductBrowseHistoryDO::getUserId, userId));

        if (count > MAX_BROWSE_HISTORY) {
            // 删除超出部分的旧记录
            String sql = String.format(
                    "DELETE FROM product_browse_history WHERE user_id = %d AND id NOT IN (SELECT id FROM (SELECT id FROM product_browse_history WHERE user_id = %d ORDER BY browse_time DESC LIMIT %d) AS tmp)",
                    userId, userId, MAX_BROWSE_HISTORY
            );
            browseHistoryMapper.delete(new LambdaQueryWrapper<ProductBrowseHistoryDO>()
                    .eq(ProductBrowseHistoryDO::getUserId, userId)
                    .notInSql(ProductBrowseHistoryDO::getId,
                            "SELECT id FROM product_browse_history WHERE user_id = " + userId + " ORDER BY browse_time DESC LIMIT " + MAX_BROWSE_HISTORY));
        }
    }

    /**
     * 清理超出限制的搜索历史
     */
    private void cleanupOldSearchHistory(Long userId) {
        Long count = searchHistoryMapper.selectCount(new LambdaQueryWrapper<UserSearchHistoryDO>()
                .eq(UserSearchHistoryDO::getUserId, userId));

        if (count > MAX_SEARCH_HISTORY) {
            searchHistoryMapper.delete(new LambdaQueryWrapper<UserSearchHistoryDO>()
                    .eq(UserSearchHistoryDO::getUserId, userId)
                    .notInSql(UserSearchHistoryDO::getId,
                            "SELECT id FROM user_search_history WHERE user_id = " + userId + " ORDER BY search_time DESC LIMIT " + MAX_SEARCH_HISTORY));
        }
    }
}
