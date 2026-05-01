package com.calorimate.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Min(value = 6, message = "密码不能少于6位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotNull(message = "身高不能为空")
    private Double height;

    @NotNull(message = "体重不能为空")
    private Double weight;

    @NotNull(message = "年龄不能为空")
    private Integer age;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(male|female)$", message = "性别只能为 male 或 female")
    private String gender;

    private Double targetCalories;
}
