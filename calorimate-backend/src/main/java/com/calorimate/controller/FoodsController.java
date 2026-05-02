package com.calorimate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.calorimate.common.Result;
import com.calorimate.entity.Food;
import com.calorimate.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods")
public class FoodsController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/search")
    public Result<?> search(@RequestParam(required = false) String keyword) {
        IPage<Food> page = foodService.listFoods(keyword, null, 1, 50);
        return Result.success(page.getRecords());
    }
}
