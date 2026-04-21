package com.naleul.naleul.domain.color.controller;

import com.naleul.naleul.domain.color.dto.ColorCreateRequest;
import com.naleul.naleul.domain.color.dto.ColorResponse;
import com.naleul.naleul.domain.color.service.ColorService;
import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/colors")
public class ColorController {

    private final ColorService colorService;

    @PostMapping
    public ResponseEntity<ApiResponse<ColorResponse>> createColor(@RequestBody ColorCreateRequest request) {
        ColorResponse response = colorService.createColor(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.COLOR_CREATED, response));
    }

    @GetMapping("/{colorId}")
    public ResponseEntity<ApiResponse<ColorResponse>> getColorById(@PathVariable Long colorId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.COLOR_FOUND, colorService.getColorById(colorId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ColorResponse>>> getAllColors(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.COLORS_FOUND, colorService.getAllColors()));
    }

    @DeleteMapping("/{colorId}")
    public ResponseEntity<ApiResponse<Void>> deleteColor(@PathVariable Long colorId) {
        colorService.deleteColor(colorId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.COLOR_DELETED, null));
    }
}