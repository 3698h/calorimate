package com.calorimate.controller;

import com.calorimate.common.Result;
import com.calorimate.dto.AiRecordDTO;
import com.calorimate.dto.RecordAddDTO;
import com.calorimate.service.DietLogService;
import com.calorimate.vo.DailyDietVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(RecordsController.class);

    @Autowired
    private DietLogService dietLogService;

    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @Valid @RequestBody RecordAddDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("收到添加食物记录请求: foodName={}, calories={}, servings={}, unit={}, mealType={}",
                dto.getFoodName(), dto.getCalories(), dto.getServings(), dto.getUnit(), dto.getMealType());
        dietLogService.addManualDietLog(userId, dto);
        return Result.success("添加成功", null);
    }

    @PostMapping("/add-ai")
    public Result<?> addAi(HttpServletRequest request, @Valid @RequestBody AiRecordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        dietLogService.addAiDietLog(userId, dto);
        return Result.success();
    }

    @GetMapping("/today")
    public Result<DailyDietVO> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        DailyDietVO vo = dietLogService.getDailyDietLogs(userId, LocalDate.now());
        return Result.success(vo);
    }
}
