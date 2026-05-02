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

    private String openid;

    private String avatarUrl;

    private Double height;

    private Double weight;

    private Integer age;

    private String gender;

    private String birthday;

    private Double targetCalories;

    private Double targetWeight;
    private String activityLevel;
    private String goal;

    private Integer vipLevel;

    private LocalDateTime vipExpireTime;

    private String role;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
