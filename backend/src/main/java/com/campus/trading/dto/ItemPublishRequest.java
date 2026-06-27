package com.campus.trading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物品发布请求DTO
 */
@Data
public class ItemPublishRequest {

    @NotBlank(message = "物品标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer conditionLevel;

    /**
     * 尺码/码数(服装类必填)
     */
    private String size;

    @NotBlank(message = "联系方式不能为空")
    private String contactInfo;

    /**
     * 物品图片(多文件上传)
     */
    private List<MultipartFile> images;

    /**
     * 商品标签,逗号分隔(最多6个)
     */
    private String tags;
}
