package com.calorimate.vo;

import lombok.Data;

import java.util.List;

@Data
public class WeeklyStatsVO {

    private String startDate;

    private String endDate;

    private Double totalCalories;

    private Double avgCalories;

    private List<DayCalories> dailyList;

    @Data
    public static class DayCalories {
        private String date;
        private Double calories;
    }
}
