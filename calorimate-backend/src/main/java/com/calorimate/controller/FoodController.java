package com.calorimate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.calorimate.common.Result;
import com.calorimate.dto.FoodDTO;
import com.calorimate.entity.Food;
import com.calorimate.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/list")
    public Result<?> listFoods(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        IPage<Food> foodPage = foodService.listFoods(name, category, page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("records", foodPage.getRecords());
        data.put("total", foodPage.getTotal());
        data.put("current", foodPage.getCurrent());
        data.put("size", foodPage.getSize());
        data.put("pages", foodPage.getPages());

        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<Food> getFoodById(@PathVariable Long id) {
        Food food = foodService.getFoodById(id);
        return Result.success(food);
    }

    @PostMapping
    public Result<?> addFood(@Valid @RequestBody FoodDTO dto) {
        foodService.addFood(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> updateFood(@PathVariable Long id, @Valid @RequestBody FoodDTO dto) {
        foodService.updateFood(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return Result.success();
    }
}
