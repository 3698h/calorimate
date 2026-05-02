package com.calorimate.vo;

import lombok.Data;

import java.util.List;

@Data
public class AiParseResultVO {

    private List<FoodItem> foods;

    private Double totalCalories;

    private String mealType;

    @Data
    public static class FoodItem {
        private String name;
        private String amount;
        private Double calories;
        private Double protein;
        private Double fat;
        private Double carbs;
    }
}
