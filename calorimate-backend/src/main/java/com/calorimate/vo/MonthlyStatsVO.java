package com.calorimate.vo;

import lombok.Data;

import java.util.List;

@Data
public class MonthlyStatsVO {

    private String month;

    private Double totalCalories;

    private Double avgCalories;

    private Integer totalDays;

    private Integer daysOnTarget;

    private Integer daysOverTarget;

    private List<DayCalories> dailyList;

    @Data
    public static class DayCalories {
        private String date;
        private Double calories;
    }
}
