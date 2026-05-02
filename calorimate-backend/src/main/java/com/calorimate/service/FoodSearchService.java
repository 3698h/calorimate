package com.calorimate.service;

import com.calorimate.entity.Food;
import java.util.List;

public interface FoodSearchService {

    List<Food> searchFood(String keyword);
}
