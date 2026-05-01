package com.calorimate.controller;

import com.calorimate.common.Result;
import com.calorimate.service.StatsService;
import com.calorimate.vo.DailyStatsVO;
import com.calorimate.vo.MonthlyStatsVO;
import com.calorimate.vo.WeeklyStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/daily")
    public Result<DailyStatsVO> getDailyStats(
            HttpServletRequest request,
            @RequestParam(required = false) String date) {

        Long userId = (Long) request.getAttribute("userId");
        LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();

        DailyStatsVO result = statsService.getDailyStats(userId, queryDate);
        return Result.success(result);
    }

    @GetMapping("/weekly")
    public Result<WeeklyStatsVO> getWeeklyStats(
            HttpServletRequest request,
            @RequestParam(required = false) String date) {

        Long userId = (Long) request.getAttribute("userId");
        LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();

        WeeklyStatsVO result = statsService.getWeeklyStats(userId, queryDate);
        return Result.success(result);
    }

    @GetMapping("/monthly")
    public Result<MonthlyStatsVO> getMonthlyStats(
            HttpServletRequest request,
            @RequestParam(required = false) String month) {

        Long userId = (Long) request.getAttribute("userId");
        if (month == null) {
            month = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        MonthlyStatsVO result = statsService.getMonthlyStats(userId, month);
        return Result.success(result);
    }
}
