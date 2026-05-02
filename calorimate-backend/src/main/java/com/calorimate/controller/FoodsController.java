package com.calorimate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.calorimate.common.Result;
import com.calorimate.dto.FoodDTO;
import com.calorimate.entity.Food;
import com.calorimate.service.FoodSearchService;
import com.calorimate.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodsController {

    @Autowired
    private FoodService foodService;

    @Autowired
    private FoodSearchService foodSearchService;

    @GetMapping("/search")
    public Result<?> search(@RequestParam(required = false) String keyword) {
        IPage<Food> page = foodService.listFoods(keyword, null, 1, 50);
        return Result.success(page.getRecords());
    }

    @GetMapping("/search-external")
    public Result<List<Food>> searchExternal(@RequestParam String keyword) {
        List<Food> foods = foodSearchService.searchFood(keyword);
        return Result.success(foods);
    }

    @PostMapping("/import")
    public Result<Food> importFood(@Valid @RequestBody FoodDTO dto) {
        Food food = foodService.importFood(dto);
        return Result.success(food);
    }
}
