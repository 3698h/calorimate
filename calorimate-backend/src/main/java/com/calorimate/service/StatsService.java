package com.calorimate.service;

import com.calorimate.vo.DailyStatsVO;
import com.calorimate.vo.MonthlyStatsVO;
import com.calorimate.vo.WeeklyStatsVO;

import java.time.LocalDate;

public interface StatsService {

    DailyStatsVO getDailyStats(Long userId, LocalDate date);

    WeeklyStatsVO getWeeklyStats(Long userId, LocalDate date);

    MonthlyStatsVO getMonthlyStats(Long userId, String month);
}
