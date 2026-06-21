package com.campus.trading.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核请求DTO
 */
@Data
public class AuditRequest {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    /**
     * 操作: 1-通过, 2-驳回, 3-下架
     */
    @NotNull(message = "审核操作不能为空")
    private Integer action;

    /**
     * 审核原因/备注
     */
    private String reason;
}
