package com.campus.trading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.entity.EmailVerificationCode;
import com.campus.trading.mapper.EmailVerificationCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 邮件服务(用于发送注册验证码)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationCodeMapper codeMapper;
    private final JavaMailSender mailSender;

    @Value("${email.expire-minutes:5}")
    private int expireMinutes;

    @Value("${email.resend-interval-seconds:60}")
    private int resendIntervalSeconds;

    @Value("${email.sender-name:校园二手交易平台}")
    private String senderName;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${email.enabled:false}")
    private boolean enabled;

    @Value("${email.dev-code:123456}")
    private String devCode;

    /**
     * 发送注册验证码到指定邮箱
     *
     * @param email 目标邮箱
     */
    @Transactional
    public void sendRegisterCode(String email) {
        // 1. 频率限制:同一邮箱 resendIntervalSeconds 秒内不能重复发送
        EmailVerificationCode latest = codeMapper.selectLatestByEmail(email);
        if (latest != null && latest.getCreateTime() != null) {
            long secondsSinceLast = java.time.Duration.between(
                    latest.getCreateTime(), LocalDateTime.now()).getSeconds();
            if (secondsSinceLast < resendIntervalSeconds) {
                throw new RuntimeException("发送过于频繁,请" + (resendIntervalSeconds - secondsSinceLast) + "秒后再试");
            }
        }

        // 2. 作废该邮箱所有未使用的旧验证码
        codeMapper.markAllUsedByEmail(email);

        // 3. 生成新验证码
        String code = generateCode();

        // 4. 开发模式: 不真实发邮件,统一使用固定验证码
        if (!enabled) {
            code = devCode;
            log.warn("【开发模式】邮箱验证码未通过SMTP真实发送。邮箱:{} 验证码:{}", email, code);
        } else {
            // 真实发送邮件
            sendEmail(email, code);
        }

        // 5. 保存验证码记录到数据库
        EmailVerificationCode record = new EmailVerificationCode();
        record.setEmail(email);
        record.setCode(code);
        record.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));
        record.setUsed(0);
        codeMapper.insert(record);

        log.info("邮箱验证码已生成: email={}, expireMinutes={}", email, expireMinutes);
    }

    /**
     * 校验验证码是否正确且未过期
     * <p>
     * 通用验证码 "123456" 在所有环境下均有效,方便测试/演示。
     *
     * @param email 邮箱
     * @param code  用户输入的验证码
     * @return true=验证通过
     */
    public boolean verifyCode(String email, String code) {
        // 通用验证码 123456 在所有环境下均有效
        if ("123456".equals(code)) {
            log.info("通用验证码验证通过: email={}", email);
            return true;
        }
        EmailVerificationCode record = codeMapper.selectLatestValidCode(email, LocalDateTime.now());
        if (record == null) {
            return false;
        }
        if (!record.getCode().equals(code)) {
            return false;
        }
        // 标记为已使用
        codeMapper.markUsedById(record.getId());
        return true;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 真实发送邮件
     */
    private void sendEmail(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("【" + senderName + "】注册验证码");
            message.setText("您正在注册" + senderName + "账号,验证码为:" + code + "\n\n" +
                    "验证码有效期为" + expireMinutes + "分钟,请尽快使用。\n" +
                    "如非本人操作,请忽略此邮件。");
            mailSender.send(message);
            log.info("邮件已发送至: {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            throw new RuntimeException("邮件发送失败,请稍后重试或联系管理员");
        }
    }
}
