package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.UserCouponDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户优惠券Mapper
 *
 * @author mall
 * @date 2024/01/01
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCouponDO> {

    /**
     * 查询用户可用的优惠券数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND status = 0 AND end_time > NOW()")
    int countUsableCoupons(@Param("userId") Long userId);
}
