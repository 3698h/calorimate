package com.calorimate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.calorimate.dto.FoodDTO;
import com.calorimate.entity.Food;

public interface FoodService {

    IPage<Food> listFoods(String name, String category, Integer page, Integer size);

    Food getFoodById(Long id);

    void addFood(FoodDTO dto);

    void updateFood(Long id, FoodDTO dto);

    void deleteFood(Long id);
}
