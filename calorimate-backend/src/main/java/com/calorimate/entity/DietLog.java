package com.calorimate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diet_log")
public class DietLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long foodId;

    private String mealType;

    private Double servings;

    private Double calories;

    private LocalDate logDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
