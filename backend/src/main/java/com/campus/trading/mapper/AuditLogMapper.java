package com.campus.trading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录Mapper
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
