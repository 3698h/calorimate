package com.calorimate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.calorimate.entity.Food;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoodMapper extends BaseMapper<Food> {
}
