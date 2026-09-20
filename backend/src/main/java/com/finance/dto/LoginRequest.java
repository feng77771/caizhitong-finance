package com.finance.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "请输入用户名/邮箱/手机号")
    private String loginName;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    
    @NotBlank(message = "验证码UUID不能为空")
    private String uuid;
    
    private String role = "user";
}