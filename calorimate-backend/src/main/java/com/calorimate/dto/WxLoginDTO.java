package com.calorimate.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class WxLoginDTO {
    @NotBlank(message = "code不能为空")
    private String code;
}
