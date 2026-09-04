package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.PointsRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 积分记录Mapper
 *
 * @author mall
 * @date 2024/01/01
 */
@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecordDO> {

    /**
     * 获取用户当前积分余额
     */
    @Select("SELECT balance FROM points_record WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT 1")
    Integer getUserPointsBalance(@Param("userId") Long userId);
}
