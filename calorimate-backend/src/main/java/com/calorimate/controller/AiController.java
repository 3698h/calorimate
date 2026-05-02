package com.calorimate.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.calorimate.common.Result;
import com.calorimate.dto.AiParseDTO;
import com.calorimate.service.AiClient;
import com.calorimate.service.AiService;
import com.calorimate.vo.AiAdviceVO;
import com.calorimate.vo.AiParseResultVO;
import com.calorimate.vo.RecognizeFoodVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private AiService aiService;

    @Autowired
    private AiClient aiClient;

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
        try {
            // 获取图像格式
            String originalFilename = file.getOriginalFilename();
            String imageFormat = "jpeg"; // 默认格式
            if (originalFilename != null) {
                if (originalFilename.toLowerCase().endsWith(".png")) {
                    imageFormat = "png";
                } else if (originalFilename.toLowerCase().endsWith(".jpg") ||
                        originalFilename.toLowerCase().endsWith(".jpeg")) {
                    imageFormat = "jpeg";
                }
            }

            // 构建提示词
            String systemPrompt = "你是一个营养师助手。用户会上传食物图片，你需要识别图片中的食物并分析其营养成分。\n" +
                    "请严格按照JSON格式返回结果，不要返回任何其他文字。\n\n" +
                    "返回格式示例：\n" +
                    "{\"name\": \"西红柿炒蛋\", \"calories\": 350, \"confidence\": 0.95, " +
                    "\"ingredients\": [\"西红柿\", \"鸡蛋\", \"食用油\", \"盐\", \"糖\"], " +
                    "\"protein\": 15.0, \"fat\": 20.0, \"carbs\": 10.0}\n\n" +
                    "字段说明：\n" +
                    "- name: 食物名称\n" +
                    "- calories: 估算卡路里(千卡)\n" +
                    "- confidence: 识别置信度(0-1之间的小数)\n" +
                    "- ingredients: 主要食材列表\n" +
                    "- protein: 蛋白质含量(克)\n" +
                    "- fat: 脂肪含量(克)\n" +
                    "- carbs: 碳水化合物含量(克)\n\n" +
                    "注意：请尽量给出合理的营养估算值。";

            String userMessage = "请识别这张图片中的食物，并给出营养成分分析。";

            // 调用AI进行图像识别
            InputStream imageStream = file.getInputStream();
            String response = aiClient.chatWithImage(systemPrompt, userMessage, imageStream, imageFormat);

            log.info("食物识别AI响应: {}", response);

            // 解析AI响应
            String jsonStr = extractJson(response);
            JSONObject json = JSON.parseObject(jsonStr);

            RecognizeFoodVO vo = new RecognizeFoodVO();
            vo.setName(json.getString("name"));
            vo.setCalories(json.getDouble("calories"));
            vo.setConfidence(json.getDouble("confidence"));

            // 解析食材列表
            if (json.containsKey("ingredients")) {
                vo.setIngredients(json.getList("ingredients", String.class));
            } else {
                vo.setIngredients(Arrays.asList(json.getString("name")));
            }

            vo.setProtein(json.getDouble("protein"));
            vo.setFat(json.getDouble("fat"));
            vo.setCarbs(json.getDouble("carbs"));

            return Result.success(vo);
        } catch (IOException e) {
            log.error("读取图像文件失败", e);
            return Result.error(500, "读取图像文件失败");
        } catch (Exception e) {
            log.error("AI食物识别失败", e);
            return Result.error(500, "AI识别失败: " + e.getMessage());
        }
    }

    private String extractJson(String text) {
        String cleaned = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }
}
