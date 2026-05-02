package com.calorimate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.calorimate.dto.AiRecordDTO;
import com.calorimate.dto.DietLogDTO;
import com.calorimate.dto.RecordAddDTO;
import com.calorimate.entity.DietLog;
import com.calorimate.entity.Food;
import com.calorimate.mapper.DietLogMapper;
import com.calorimate.mapper.FoodMapper;
import com.calorimate.service.DietLogService;
import com.calorimate.vo.DailyDietVO;
import com.calorimate.vo.DietLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DietLogServiceImpl implements DietLogService {

    @Autowired
    private DietLogMapper dietLogMapper;

    @Autowired
    private FoodMapper foodMapper;

    private static final List<String> MEAL_ORDER = Arrays.asList("早餐", "午餐", "晚餐", "加餐");

    @Override
    public void addDietLog(Long userId, DietLogDTO dto) {
        Food food = foodMapper.selectById(dto.getFoodId());
        if (food == null) {
            throw new RuntimeException("食物不存在");
        }

        DietLog dietLog = new DietLog();
        dietLog.setUserId(userId);
        dietLog.setFoodId(dto.getFoodId());
        dietLog.setMealType(dto.getMealType());
        dietLog.setServings(dto.getServings());
        dietLog.setCalories(food.getCalories() * dto.getServings());
        dietLog.setLogDate(dto.getLogDate() != null ? dto.getLogDate() : LocalDate.now());
        dietLog.setCreateTime(LocalDateTime.now());
        dietLog.setUpdateTime(LocalDateTime.now());

        dietLogMapper.insert(dietLog);
    }

    @Override
    public void addAiDietLog(Long userId, AiRecordDTO dto) {
        Food food = new Food();
        food.setName(dto.getFoodName());
        food.setCalories(dto.getCalories());
        food.setProtein(dto.getProtein() != null ? dto.getProtein() : 0.0);
        food.setFat(dto.getFat() != null ? dto.getFat() : 0.0);
        food.setCarbs(dto.getCarbs() != null ? dto.getCarbs() : 0.0);
        food.setUnit("份");
        food.setCategory("AI识别");
        food.setCreateTime(LocalDateTime.now());
        food.setUpdateTime(LocalDateTime.now());
        foodMapper.insert(food);

        DietLog dietLog = new DietLog();
        dietLog.setUserId(userId);
        dietLog.setFoodId(food.getId());
        dietLog.setMealType(dto.getMealType());
        dietLog.setServings(1.0);
        dietLog.setCalories(dto.getCalories());
        dietLog.setLogDate(LocalDate.now());
        dietLog.setCreateTime(LocalDateTime.now());
        dietLog.setUpdateTime(LocalDateTime.now());

        dietLogMapper.insert(dietLog);
    }

    @Override
    public void addManualDietLog(Long userId, RecordAddDTO dto) {
        Food food = null;
        if (dto.getFoodId() != null && dto.getFoodId() > 0) {
            food = foodMapper.selectById(dto.getFoodId());
        }
        if (food == null) {
            food = new Food();
            food.setName(dto.getFoodName());
            food.setCalories(dto.getCalories());
            food.setProtein(0.0);
            food.setFat(0.0);
            food.setCarbs(0.0);
            food.setUnit(dto.getUnit() != null ? dto.getUnit() : "份");
            food.setCategory("手动添加");
            food.setCreateTime(LocalDateTime.now());
            food.setUpdateTime(LocalDateTime.now());
            foodMapper.insert(food);
        }

        double servings = dto.getServings() != null ? dto.getServings() : 1.0;
        String mealType = dto.getMealType() != null && !dto.getMealType().isEmpty() ? dto.getMealType() : "早餐";

        DietLog dietLog = new DietLog();
        dietLog.setUserId(userId);
        dietLog.setFoodId(food.getId());
        dietLog.setMealType(mealType);
        dietLog.setServings(servings);
        dietLog.setCalories(dto.getCalories());
        dietLog.setLogDate(LocalDate.now());
        dietLog.setCreateTime(LocalDateTime.now());
        dietLog.setUpdateTime(LocalDateTime.now());

        dietLogMapper.insert(dietLog);
    }

    @Override
    public DailyDietVO getDailyDietLogs(Long userId, LocalDate date) {
        LambdaQueryWrapper<DietLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietLog::getUserId, userId)
                .eq(DietLog::getLogDate, date)
                .orderByAsc(DietLog::getCreateTime);
        List<DietLog> dietLogs = dietLogMapper.selectList(wrapper);

        Set<Long> foodIds = new HashSet<>();
        for (DietLog log : dietLogs) {
            foodIds.add(log.getFoodId());
        }

        Map<Long, Food> foodMap = new HashMap<>();
        if (!foodIds.isEmpty()) {
            List<Food> foods = foodMapper.selectBatchIds(foodIds);
            for (Food food : foods) {
                foodMap.put(food.getId(), food);
            }
        }

        Map<String, List<DietLogVO>> meals = new LinkedHashMap<>();
        for (String mealType : MEAL_ORDER) {
            meals.put(mealType, new ArrayList<>());
        }

        double totalCalories = 0;

        for (DietLog log : dietLogs) {
            Food food = foodMap.get(log.getFoodId());
            String foodName = food != null ? food.getName() : "未知食物";

            DietLogVO vo = new DietLogVO();
            vo.setId(log.getId());
            vo.setFoodId(log.getFoodId());
            vo.setFoodName(foodName);
            vo.setServings(log.getServings());
            vo.setCalories(log.getCalories());

            List<DietLogVO> mealList = meals.getOrDefault(log.getMealType(), new ArrayList<>());
            mealList.add(vo);
            meals.put(log.getMealType(), mealList);

            totalCalories += log.getCalories();
        }

        DailyDietVO result = new DailyDietVO();
        result.setDate(date.toString());
        result.setTotalCalories(Math.round(totalCalories * 100.0) / 100.0);
        result.setMeals(meals);
        return result;
    }

    @Override
    public void updateDietLog(Long userId, Long id, DietLogDTO dto) {
        DietLog dietLog = dietLogMapper.selectById(id);
        if (dietLog == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!dietLog.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此记录");
        }

        Food food = foodMapper.selectById(dto.getFoodId());
        if (food == null) {
            throw new RuntimeException("食物不存在");
        }

        dietLog.setFoodId(dto.getFoodId());
        dietLog.setMealType(dto.getMealType());
        dietLog.setServings(dto.getServings());
        dietLog.setCalories(food.getCalories() * dto.getServings());
        if (dto.getLogDate() != null) {
            dietLog.setLogDate(dto.getLogDate());
        }
        dietLog.setUpdateTime(LocalDateTime.now());

        dietLogMapper.updateById(dietLog);
    }

    @Override
    public void deleteDietLog(Long userId, Long id) {
        DietLog dietLog = dietLogMapper.selectById(id);
        if (dietLog == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!dietLog.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此记录");
        }

        dietLogMapper.deleteById(id);
    }
}
