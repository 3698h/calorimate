package com.calorimate.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String gender;
    private String birthday;
    private Double height;
    private Double weight;
    private Double targetWeight;
    private Integer age;
    private String activityLevel;
    private String goal;
}
