package com.campus.trading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论请求DTO
 */
@Data
public class CommentRequest {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    private Long parentId = 0L;

    private Long replyToUserId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500个字符")
    private String content;
}
