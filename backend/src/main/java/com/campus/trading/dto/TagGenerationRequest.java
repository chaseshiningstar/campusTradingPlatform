package com.campus.trading.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签生成请求DTO
 */
@Data
public class TagGenerationRequest {

    @NotBlank(message = "商品描述不能为空")
    private String description;

    private String title;
}
