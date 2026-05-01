package com.calorimate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private Double height;

    private Double weight;

    private Integer age;

    private String gender;

    private Double targetCalories;

    private String role;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
