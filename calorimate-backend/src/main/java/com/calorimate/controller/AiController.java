package com.calorimate.controller;

import com.calorimate.common.Result;
import com.calorimate.dto.AiParseDTO;
import com.calorimate.service.AiService;
import com.calorimate.vo.AiAdviceVO;
import com.calorimate.vo.AiParseResultVO;
import com.calorimate.vo.RecognizeFoodVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/parse")
    public Result<AiParseResultVO> parseDiet(
            @Valid @RequestBody AiParseDTO dto) {
        AiParseResultVO result = aiService.parseDiet(dto.getUserInput(), dto.getMealType());
        return Result.success(result);
    }

    @PostMapping("/advice")
    public Result<AiAdviceVO> getAdvice(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AiAdviceVO result = aiService.getAdvice(userId);
        return Result.success(result);
    }

    @PostMapping("/recognize-food")
    public Result<RecognizeFoodVO> recognizeFood(@RequestParam("file") MultipartFile file) {
        RecognizeFoodVO vo = new RecognizeFoodVO();
        vo.setName("西红柿炒蛋");
        vo.setCalories(350.0);
        vo.setConfidence(0.95);
        vo.setIngredients(Arrays.asList("西红柿", "鸡蛋", "食用油", "盐", "糖"));
        vo.setProtein(15.0);
        vo.setFat(20.0);
        vo.setCarbs(10.0);
        return Result.success(vo);
    }
}
