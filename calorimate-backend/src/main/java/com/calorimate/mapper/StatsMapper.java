package com.calorimate.mapper;

import com.calorimate.vo.WeeklyStatsVO;
import com.calorimate.vo.MonthlyStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {

    List<Map<String, Object>> getDailyMealCalories(@Param("userId") Long userId, @Param("date") LocalDate date);

    List<Map<String, Object>> getDailyNutrients(@Param("userId") Long userId, @Param("date") LocalDate date);

    List<WeeklyStatsVO.DayCalories> getWeeklyCalories(@Param("userId") Long userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    List<MonthlyStatsVO.DayCalories> getMonthlyCalories(@Param("userId") Long userId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    Integer getDaysOnTarget(@Param("userId") Long userId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate,
                            @Param("targetCalories") Double targetCalories);

    Integer getDaysOverTarget(@Param("userId") Long userId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("targetCalories") Double targetCalories);
}
