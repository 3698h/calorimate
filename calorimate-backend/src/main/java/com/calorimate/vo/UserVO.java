package com.calorimate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String username;

    private Double height;

    private Double weight;

    private Integer age;

    private String gender;

    private Double targetCalories;

    private String role;

    private LocalDateTime createTime;
}
