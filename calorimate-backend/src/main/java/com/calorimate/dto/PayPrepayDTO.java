package com.calorimate.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PayPrepayDTO {
    @NotBlank(message = "套餐类型不能为空")
    private String planType;

    @NotBlank(message = "登录code不能为空")
    private String code;
}
