package com.calorimate.vo;

import lombok.Data;
import java.util.List;

@Data
public class RecognizeFoodVO {
    private String name;
    private Double calories;
    private Double confidence;
    private List<String> ingredients;
    private Double protein;
    private Double fat;
    private Double carbs;
}
