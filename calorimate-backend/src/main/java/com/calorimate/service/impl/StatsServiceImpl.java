package com.calorimate.service.impl;

import com.calorimate.entity.User;
import com.calorimate.mapper.StatsMapper;
import com.calorimate.mapper.UserMapper;
import com.calorimate.service.StatsService;
import com.calorimate.vo.DailyStatsVO;
import com.calorimate.vo.MonthlyStatsVO;
import com.calorimate.vo.WeeklyStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private StatsMapper statsMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public DailyStatsVO getDailyStats(Long userId, LocalDate date) {
        User user = userMapper.selectById(userId);
        Double targetCalories = user != null && user.getTargetCalories() != null
                ? user.getTargetCalories()
                : 2000.0;

        List<Map<String, Object>> mealData = statsMapper.getDailyMealCalories(userId, date);
        Map<String, Double> mealCalories = new LinkedHashMap<>();
        mealCalories.put("早餐", 0.0);
        mealCalories.put("午餐", 0.0);
        mealCalories.put("晚餐", 0.0);
        mealCalories.put("加餐", 0.0);

        double totalCalories = 0;
        for (Map<String, Object> row : mealData) {
            String mealType = getStringValue(row, "mealType");
            double calories = getDoubleValue(row, "calories");
            if (mealType != null) {
                mealCalories.put(mealType, calories);
            }
            totalCalories += calories;
        }

        List<Map<String, Object>> nutrientData = statsMapper.getDailyNutrients(userId, date);
        double totalProtein = 0, totalFat = 0, totalCarbs = 0;
        if (nutrientData != null && !nutrientData.isEmpty()) {
            Map<String, Object> nutrients = nutrientData.get(0);
            if (nutrients != null) {
                totalProtein = getDoubleValue(nutrients, "totalProtein");
                totalFat = getDoubleValue(nutrients, "totalFat");
                totalCarbs = getDoubleValue(nutrients, "totalCarbs");
            }
        }

        DailyStatsVO vo = new DailyStatsVO();
        vo.setDate(date.toString());
        vo.setTotalCalories(Math.round(totalCalories * 100.0) / 100.0);
        vo.setTargetCalories(targetCalories);
        vo.setRemainingCalories(Math.round((targetCalories - totalCalories) * 100.0) / 100.0);
        vo.setTotalProtein(Math.round(totalProtein * 100.0) / 100.0);
        vo.setTotalFat(Math.round(totalFat * 100.0) / 100.0);
        vo.setTotalCarbs(Math.round(totalCarbs * 100.0) / 100.0);
        vo.setMealCalories(mealCalories);
        return vo;
    }

    @Override
    public WeeklyStatsVO getWeeklyStats(Long userId, LocalDate date) {
        LocalDate startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(6);

        List<WeeklyStatsVO.DayCalories> dbData = statsMapper.getWeeklyCalories(userId, startDate, endDate);

        Map<String, Double> caloriesMap = new LinkedHashMap<>();
        for (WeeklyStatsVO.DayCalories day : dbData) {
            Double cal = day.getCalories() != null ? day.getCalories() : 0.0;
            caloriesMap.put(day.getDate().toString(), cal);
        }

        List<WeeklyStatsVO.DayCalories> dailyList = new ArrayList<>();
        double totalCalories = 0;
        int recordedDays = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate current = startDate.plusDays(i);
            String dateStr = current.toString();
            Double calories = caloriesMap.getOrDefault(dateStr, 0.0);

            WeeklyStatsVO.DayCalories dayCalories = new WeeklyStatsVO.DayCalories();
            dayCalories.setDate(dateStr);
            dayCalories.setCalories(calories);
            dailyList.add(dayCalories);

            totalCalories += calories;
            if (calories > 0) {
                recordedDays++;
            }
        }

        WeeklyStatsVO vo = new WeeklyStatsVO();
        vo.setStartDate(startDate.toString());
        vo.setEndDate(endDate.toString());
        vo.setTotalCalories(Math.round(totalCalories * 100.0) / 100.0);
        vo.setAvgCalories(recordedDays > 0
                ? Math.round(totalCalories / recordedDays * 100.0) / 100.0
                : 0.0);
        vo.setDailyList(dailyList);
        return vo;
    }

    @Override
    public MonthlyStatsVO getMonthlyStats(Long userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        int totalDays = yearMonth.lengthOfMonth();

        List<MonthlyStatsVO.DayCalories> dbData = statsMapper.getMonthlyCalories(userId, startDate, endDate);

        Map<String, Double> caloriesMap = new LinkedHashMap<>();
        for (MonthlyStatsVO.DayCalories day : dbData) {
            Double cal = day.getCalories() != null ? day.getCalories() : 0.0;
            caloriesMap.put(day.getDate().toString(), cal);
        }

        List<MonthlyStatsVO.DayCalories> dailyList = new ArrayList<>();
        double totalCalories = 0;
        int recordedDays = 0;

        for (int i = 0; i < totalDays; i++) {
            LocalDate current = startDate.plusDays(i);
            String dateStr = current.toString();
            Double calories = caloriesMap.getOrDefault(dateStr, 0.0);

            MonthlyStatsVO.DayCalories dayCalories = new MonthlyStatsVO.DayCalories();
            dayCalories.setDate(dateStr);
            dayCalories.setCalories(calories);
            dailyList.add(dayCalories);

            totalCalories += calories;
            if (calories > 0) {
                recordedDays++;
            }
        }

        User user = userMapper.selectById(userId);
        Double targetCalories = user != null && user.getTargetCalories() != null
                ? user.getTargetCalories()
                : 2000.0;

        Integer daysOnTarget = statsMapper.getDaysOnTarget(userId, startDate, endDate, targetCalories);
        Integer daysOverTarget = statsMapper.getDaysOverTarget(userId, startDate, endDate, targetCalories);

        MonthlyStatsVO vo = new MonthlyStatsVO();
        vo.setMonth(month);
        vo.setTotalCalories(Math.round(totalCalories * 100.0) / 100.0);
        vo.setAvgCalories(recordedDays > 0
                ? Math.round(totalCalories / recordedDays * 100.0) / 100.0
                : 0.0);
        vo.setTotalDays(totalDays);
        vo.setDaysOnTarget(daysOnTarget != null ? daysOnTarget : 0);
        vo.setDaysOverTarget(daysOverTarget != null ? daysOverTarget : 0);
        vo.setDailyList(dailyList);
        return vo;
    }

    private double getDoubleValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) {
            val = map.get(key.toUpperCase());
        }
        return val != null ? ((Number) val).doubleValue() : 0.0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) {
            val = map.get(key.toUpperCase());
        }
        return val != null ? val.toString() : null;
    }
}
