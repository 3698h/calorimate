package com.calorimate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("food")
public class Food {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private Double calories;

    private Double protein;

    private Double fat;

    private Double carbs;

    private String unit;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
