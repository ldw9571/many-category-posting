package com.bizcontent.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategorySuggestionDto {
    private String categoryName;
    private int confidencePercent;
}
