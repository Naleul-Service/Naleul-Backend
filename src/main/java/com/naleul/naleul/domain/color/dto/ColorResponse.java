package com.naleul.naleul.domain.color.dto;

import com.naleul.naleul.domain.color.entity.Color;
import lombok.Getter;

@Getter
public class ColorResponse {
    private Long colorId;
    private String colorCode;

    public ColorResponse(Color color) {
        this.colorId = color.getColorId();
        this.colorCode = color.getColorCode();
    }

    public static ColorResponse from(Color color) {
        return new ColorResponse(color);
    }
}
