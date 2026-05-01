package com.calorimate.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FoodDTO {

    @NotBlank(message = "食物名称不能为空")
    private String name;

    private String category;

    @NotNull(message = "卡路里不能为空")
    private Double calories;

    private Double protein;

    private Double fat;

    private Double carbs;

    private String unit;
}
