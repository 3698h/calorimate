package com.calorimate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String username;

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
}
