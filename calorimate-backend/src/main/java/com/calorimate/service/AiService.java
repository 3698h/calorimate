package com.calorimate.service;

import com.calorimate.vo.AiAdviceVO;
import com.calorimate.vo.AiParseResultVO;

public interface AiService {

    AiParseResultVO parseDiet(String userInput, String mealType);

    AiAdviceVO getAdvice(Long userId);
}
