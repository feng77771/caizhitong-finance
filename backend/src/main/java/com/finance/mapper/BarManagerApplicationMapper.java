package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.BarManagerApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BarManagerApplicationMapper extends BaseMapper<BarManagerApplication> {
    
    @Select("SELECT a.*, u.username FROM bar_manager_application a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.status = #{status} ORDER BY a.create_time DESC")
    List<BarManagerApplication> selectByStatus(@Param("status") String status);
    
    @Select("SELECT a.*, u.username FROM bar_manager_application a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.user_id = #{userId} ORDER BY a.create_time DESC")
    List<BarManagerApplication> selectByUserId(@Param("userId") Long userId);
}
