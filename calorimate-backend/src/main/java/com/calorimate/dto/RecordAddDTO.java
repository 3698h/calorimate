package com.calorimate.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RecordAddDTO {
    @NotNull(message = "食物ID不能为空")
    private Long foodId;
    @NotBlank(message = "餐次不能为空")
    private String mealType;
    @NotNull(message = "份数不能为空")
    private Double servings;
    private String unit;
}
