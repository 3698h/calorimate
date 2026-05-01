package com.calorimate.controller;

import com.calorimate.common.Result;
import com.calorimate.dto.AiParseDTO;
import com.calorimate.service.AiService;
import com.calorimate.vo.AiAdviceVO;
import com.calorimate.vo.AiParseResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/parse")
    public Result<AiParseResultVO> parseDiet(
            HttpServletRequest request,
            @Valid @RequestBody AiParseDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        AiParseResultVO result = aiService.parseDiet(dto.getUserInput(), dto.getMealType());
        return Result.success(result);
    }

    @PostMapping("/advice")
    public Result<AiAdviceVO> getAdvice(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AiAdviceVO result = aiService.getAdvice(userId);
        return Result.success(result);
    }
}
