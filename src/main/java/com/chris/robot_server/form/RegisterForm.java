package com.chris.robot_server.form;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * RegisterForm
 *
 * @author chris
 * 11/3/24
 */
@Data
public class RegisterForm {

    @NotNull(message = "注册邮箱不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$", message = "不满足邮箱正则表达式")
    private String email;

    @NotNull
    @Size(max=20,min=2,message = "密码长度要在2-20之间")
    private String password;

    private String language;

    @NotNull
    private String firstName;

    private Byte age;
}
