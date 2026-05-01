package com.calorimate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.calorimate.dto.FoodDTO;
import com.calorimate.entity.Food;
import com.calorimate.mapper.FoodMapper;
import com.calorimate.service.FoodService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodMapper foodMapper;

    @Override
    public IPage<Food> listFoods(String name, String category, Integer page, Integer size) {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Food::getName, name);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Food::getCategory, category);
        }

        wrapper.orderByDesc(Food::getId);

        return foodMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Food getFoodById(Long id) {
        Food food = foodMapper.selectById(id);
        if (food == null) {
            throw new RuntimeException("食物不存在");
        }
        return food;
    }

    @Override
    public void addFood(FoodDTO dto) {
        Food food = new Food();
        BeanUtils.copyProperties(dto, food);
        food.setCreateTime(LocalDateTime.now());
        food.setUpdateTime(LocalDateTime.now());
        foodMapper.insert(food);
    }

    @Override
    public void updateFood(Long id, FoodDTO dto) {
        Food food = foodMapper.selectById(id);
        if (food == null) {
            throw new RuntimeException("食物不存在");
        }
        BeanUtils.copyProperties(dto, food);
        food.setUpdateTime(LocalDateTime.now());
        foodMapper.updateById(food);
    }

    @Override
    public void deleteFood(Long id) {
        Food food = foodMapper.selectById(id);
        if (food == null) {
            throw new RuntimeException("食物不存在");
        }
        foodMapper.deleteById(id);
    }
}
