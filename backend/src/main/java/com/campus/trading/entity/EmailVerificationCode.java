package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮箱验证码实体
 */
@Data
@TableName("email_verification_code")
public class EmailVerificationCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;

    private String code;

    private LocalDateTime expireTime;

    /**
     * 是否已使用: 0-未使用, 1-已使用
     */
    private Integer used;

    private LocalDateTime createTime;
}
