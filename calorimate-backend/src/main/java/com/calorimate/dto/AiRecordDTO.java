package com.calorimate.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AiRecordDTO {

    @NotBlank(message = "食物名称不能为空")
    private String foodName;

    @NotNull(message = "卡路里不能为空")
    private Double calories;

    private Double protein;

    private Double fat;

    private Double carbs;

    @NotBlank(message = "餐次不能为空")
    private String mealType;
}
