package com.campus.trading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.entity.EmailVerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 邮箱验证码Mapper
 */
@Mapper
public interface EmailVerificationCodeMapper extends BaseMapper<EmailVerificationCode> {

    /**
     * 查询指定邮箱最新一条未使用且未过期的验证码
     */
    @Select("SELECT * FROM email_verification_code " +
            "WHERE email = #{email} AND used = 0 AND expire_time > #{now} " +
            "ORDER BY create_time DESC LIMIT 1")
    EmailVerificationCode selectLatestValidCode(@Param("email") String email,
                                                  @Param("now") LocalDateTime now);

    /**
     * 标记指定邮箱的所有未使用验证码为已使用(用于发送新验证码时作废旧验证码)
     */
    @Update("UPDATE email_verification_code SET used = 1 WHERE email = #{email} AND used = 0")
    int markAllUsedByEmail(@Param("email") String email);

    /**
     * 标记单条验证码为已使用
     */
    @Update("UPDATE email_verification_code SET used = 1 WHERE id = #{id}")
    int markUsedById(@Param("id") Long id);

    /**
     * 查询指定邮箱最近一条验证码(用于发送频率限制)
     */
    @Select("SELECT * FROM email_verification_code WHERE email = #{email} ORDER BY create_time DESC LIMIT 1")
    EmailVerificationCode selectLatestByEmail(@Param("email") String email);
}
