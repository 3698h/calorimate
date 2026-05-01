package com.calorimate.vo;

import lombok.Data;

import java.util.Map;

@Data
public class DailyStatsVO {

    private String date;

    private Double totalCalories;

    private Double targetCalories;

    private Double remainingCalories;

    private Double totalProtein;

    private Double totalFat;

    private Double totalCarbs;

    private Map<String, Double> mealCalories;
}
