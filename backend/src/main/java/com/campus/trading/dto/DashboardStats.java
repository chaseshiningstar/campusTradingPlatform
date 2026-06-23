package com.campus.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    /**
     * 物品总数
     */
    private Long itemCount;

    /**
     * 待审核数量
     */
    private Long pendingAudit;

    /**
     * 用户总数
     */
    private Long userCount;

    /**
     * 评论总数
     */
    private Long commentCount;
}
