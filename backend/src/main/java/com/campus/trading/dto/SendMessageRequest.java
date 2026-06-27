package com.campus.trading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送私信请求DTO
 */
@Data
public class SendMessageRequest {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不能超过500个字符")
    private String content;
}
