package com.calorimate.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AiParseDTO {

    @NotBlank(message = "请输入饮食描述")
    private String userInput;

    private String mealType;
}
