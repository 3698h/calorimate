package com.calorimate.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.calorimate.entity.DietLog;
import com.calorimate.entity.Food;
import com.calorimate.entity.User;
import com.calorimate.mapper.DietLogMapper;
import com.calorimate.mapper.FoodMapper;
import com.calorimate.mapper.UserMapper;
import com.calorimate.service.AiService;
import com.calorimate.service.AiClient;
import com.calorimate.vo.AiAdviceVO;
import com.calorimate.vo.AiParseResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    @Autowired
    private AiClient aiClient;

    @Autowired
    private DietLogMapper dietLogMapper;

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private UserMapper userMapper;

    private static final String PARSE_SYSTEM_PROMPT = "你是一个营养师助手。用户会描述他们吃的食物，你需要解析出每种食物的名称、份量、估算卡路里和三大营养素（蛋白质、脂肪、碳水化合物）。\n"
            +
            "请严格按照JSON格式返回结果，不要返回任何其他文字。\n\n" +
            "示例：输入\"吃了一个鸡蛋和一碗米饭\"返回：\n" +
            "{\"foods\": [{\"name\": \"鸡蛋\", \"amount\": \"1个\", \"calories\": 80, \"protein\": 6.5, \"fat\": 5.3, \"carbs\": 0.6}, "
            +
            "{\"name\": \"米饭\", \"amount\": \"1碗\", \"calories\": 230, \"protein\": 4.5, \"fat\": 0.5, \"carbs\": 50.0}], "
            +
            "\"totalCalories\": 310, \"mealType\": \"午餐\"}\n\n" +
            "注意：protein为蛋白质(克)、fat为脂肪(克)、carbs为碳水化合物(克)，请尽量给出合理估算值。";

    private static final String ADVICE_SYSTEM_PROMPT = "你是一个专业的营养师。根据用户的个人信息和今日饮食数据，给出专业的营养建议。\n" +
            "请严格按照JSON格式返回结果，不要返回任何其他文字。\n\n" +
            "示例返回：\n" +
            "{\"evaluation\": \"今日饮食热量偏低，蛋白质摄入不足\", " +
            "\"onTarget\": \"未达标，目标2000kcal实际仅摄入1200kcal\", " +
            "\"suggestion\": \"明天建议增加优质蛋白如鸡胸肉、鸡蛋，主食可适当增加\"}";

    @Override
    public AiParseResultVO parseDiet(String userInput, String mealType) {
        String userMessage = "用户输入：" + userInput;
        if (mealType != null && !mealType.isEmpty()) {
            userMessage += "\n餐次：" + mealType;
        }

        String response = aiClient.chat(PARSE_SYSTEM_PROMPT, userMessage);
        log.info("AI 原始响应: {}", response);

        return parseResponse(response, mealType);
    }

    @Override
    public AiAdviceVO getAdvice(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LambdaQueryWrapper<DietLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietLog::getUserId, userId)
                .eq(DietLog::getLogDate, LocalDate.now());
        List<DietLog> todayLogs = dietLogMapper.selectList(wrapper);

        StringBuilder dietSummary = new StringBuilder();
        double totalCalories = 0;
        for (DietLog log : todayLogs) {
            Food food = foodMapper.selectById(log.getFoodId());
            String foodName = food != null ? food.getName() : "未知食物";
            dietSummary.append(foodName)
                    .append(" ").append(log.getServings()).append("份")
                    .append(" ").append(log.getCalories()).append("kcal")
                    .append("（").append(log.getMealType()).append("）；");
            totalCalories += log.getCalories();
        }

        // 安全获取用户信息，避免空指针
        double height = user.getHeight() != null ? user.getHeight() : 170.0;
        double weight = user.getWeight() != null ? user.getWeight() : 65.0;
        int age = user.getAge() != null ? user.getAge() : 25;
        String gender = user.getGender() != null ? user.getGender() : "male";
        double targetCal = user.getTargetCalories() != null ? user.getTargetCalories() : 2000.0;

        String userMessage = String.format(
                "用户信息：身高%.1fcm 体重%.1fkg 年龄%d岁 性别%s 目标%.0fkcal\n" +
                        "今日饮食：%s\n今日总摄入：%.0fkcal",
                height, weight, age,
                "male".equals(gender) ? "男" : "女",
                targetCal,
                dietSummary.length() > 0 ? dietSummary.toString() : "暂无饮食记录",
                totalCalories);

        String response = aiClient.chat(ADVICE_SYSTEM_PROMPT, userMessage);

        return parseAdviceResponse(response);
    }

    private AiParseResultVO parseResponse(String response, String mealType) {
        try {
            String jsonStr = extractJson(response);
            log.info("提取的JSON: {}", jsonStr);
            JSONObject json = JSON.parseObject(jsonStr);

            AiParseResultVO result = new AiParseResultVO();
            result.setMealType(json.getString("mealType"));
            result.setTotalCalories(json.getDouble("totalCalories"));

            JSONArray foodsArray = json.getJSONArray("foods");
            List<AiParseResultVO.FoodItem> foods = new ArrayList<>();
            if (foodsArray != null) {
                for (int i = 0; i < foodsArray.size(); i++) {
                    JSONObject foodJson = foodsArray.getJSONObject(i);
                    AiParseResultVO.FoodItem item = new AiParseResultVO.FoodItem();
                    item.setName(foodJson.getString("name"));
                    item.setAmount(foodJson.getString("amount"));
                    item.setCalories(foodJson.getDouble("calories"));
                    item.setProtein(foodJson.getDouble("protein"));
                    item.setFat(foodJson.getDouble("fat"));
                    item.setCarbs(foodJson.getDouble("carbs"));
                    foods.add(item);
                }
            }
            result.setFoods(foods);

            if (mealType != null && !mealType.isEmpty()) {
                result.setMealType(mealType);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("AI 解析结果格式错误，请重试: " + e.getMessage());
        }
    }

    private AiAdviceVO parseAdviceResponse(String response) {
        try {
            String jsonStr = extractJson(response);
            JSONObject json = JSON.parseObject(jsonStr);

            AiAdviceVO vo = new AiAdviceVO();
            vo.setEvaluation(json.getString("evaluation"));
            vo.setOnTarget(json.getString("onTarget"));
            vo.setSuggestion(json.getString("suggestion"));
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("AI 建议解析失败，请重试: " + e.getMessage());
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
