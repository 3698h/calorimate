package com.calorimate.controller;

import com.calorimate.common.Result;
import com.calorimate.dto.DietLogDTO;
import com.calorimate.service.DietLogService;
import com.calorimate.vo.DailyDietVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/diet")
public class DietLogController {

    @Autowired
    private DietLogService dietLogService;

    @PostMapping("/log")
    public Result<?> addDietLog(HttpServletRequest request, @Valid @RequestBody DietLogDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        dietLogService.addDietLog(userId, dto);
        return Result.success();
    }

    @GetMapping("/log")
    public Result<DailyDietVO> getDailyDietLogs(
            HttpServletRequest request,
            @RequestParam(required = false) String date) {

        Long userId = (Long) request.getAttribute("userId");
        LocalDate logDate = date != null ? LocalDate.parse(date) : LocalDate.now();

        DailyDietVO result = dietLogService.getDailyDietLogs(userId, logDate);
        return Result.success(result);
    }

    @PutMapping("/log/{id}")
    public Result<?> updateDietLog(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody DietLogDTO dto) {

        Long userId = (Long) request.getAttribute("userId");
        dietLogService.updateDietLog(userId, id, dto);
        return Result.success();
    }

    @DeleteMapping("/log/{id}")
    public Result<?> deleteDietLog(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        dietLogService.deleteDietLog(userId, id);
        return Result.success();
    }
}
