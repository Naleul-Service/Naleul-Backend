package com.naleul.naleul.domain.userColor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// domain/userColor/dto/UserColorAddRequest.java

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserColorAddRequest {
    private String colorCode;  // colorId → colorCode로 변경
}