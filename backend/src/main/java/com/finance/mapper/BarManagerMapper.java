package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.BarManager;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BarManagerMapper extends BaseMapper<BarManager> {
    
    @Select("SELECT m.*, u.username FROM bar_manager m " +
            "LEFT JOIN user u ON m.user_id = u.id " +
            "WHERE m.status = 'active' ORDER BY m.start_time DESC")
    List<BarManager> selectAllActive();
    
    @Select("SELECT m.*, u.username FROM bar_manager m " +
            "LEFT JOIN user u ON m.user_id = u.id " +
            "WHERE m.category = #{category} AND m.status = 'active'")
    List<BarManager> selectByCategory(@Param("category") String category);
    
    @Select("SELECT m.*, u.username FROM bar_manager m " +
            "LEFT JOIN user u ON m.user_id = u.id " +
            "WHERE m.user_id = #{userId} AND m.status = 'active'")
    List<BarManager> selectByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM bar_manager WHERE user_id = #{userId} AND category = #{category} AND status = 'active'")
    Integer countByUserAndCategory(@Param("userId") Long userId, @Param("category") String category);
}
