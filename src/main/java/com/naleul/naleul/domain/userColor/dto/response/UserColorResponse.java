package com.naleul.naleul.domain.userColor.dto.response;

import com.naleul.naleul.domain.userColor.entity.UserColor;
import lombok.Getter;

@Getter
public class UserColorResponse {
    private Long colorId;
    private String colorCode;
    private boolean defaultColor;

    private UserColorResponse() {}

    public static UserColorResponse from(UserColor userColor) {
        UserColorResponse response = new UserColorResponse();
        response.colorId = userColor.getColor().getColorId();
        response.colorCode = userColor.getColor().getColorCode();
        response.defaultColor = userColor.isDefaultColor();
        return response;
    }
}