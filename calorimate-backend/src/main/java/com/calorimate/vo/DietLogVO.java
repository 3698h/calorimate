package com.calorimate.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietLogVO {

    private Long id;

    private Long foodId;

    private String foodName;

    private Double servings;

    private Double calories;
}
