package com.calorimate.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DailyDietVO {

    private String date;

    private Double totalCalories;

    private Map<String, List<DietLogVO>> meals;
}
