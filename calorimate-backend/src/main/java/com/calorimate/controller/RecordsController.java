package com.calorimate.controller;

import com.calorimate.common.Result;
import com.calorimate.dto.DietLogDTO;
import com.calorimate.dto.RecordAddDTO;
import com.calorimate.service.DietLogService;
import com.calorimate.vo.DailyDietVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
public class RecordsController {

    @Autowired
    private DietLogService dietLogService;

    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @Valid @RequestBody RecordAddDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        DietLogDTO logDTO = new DietLogDTO();
        logDTO.setFoodId(dto.getFoodId());
        logDTO.setMealType(dto.getMealType());
        logDTO.setServings(dto.getServings());
        logDTO.setLogDate(LocalDate.now());
        dietLogService.addDietLog(userId, logDTO);
        return Result.success();
    }

    @GetMapping("/today")
    public Result<DailyDietVO> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        DailyDietVO vo = dietLogService.getDailyDietLogs(userId, LocalDate.now());
        return Result.success(vo);
    }
}
