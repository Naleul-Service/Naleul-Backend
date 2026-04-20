package com.naleul.naleul.domain.generalCategory.controller;

import com.naleul.naleul.domain.generalCategory.dto.request.GeneralCategoryCreateRequest;
import com.naleul.naleul.domain.generalCategory.dto.request.GeneralCategoryUpdateRequest;
import com.naleul.naleul.domain.generalCategory.dto.response.GeneralCategoryResponse;
import com.naleul.naleul.domain.generalCategory.service.GeneralCategoryService;
import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/general-categories")
public class GeneralCategoryController {

    private final GeneralCategoryService generalCategoryService;

    // POST /api/general-categories
    @PostMapping
    public ResponseEntity<ApiResponse<GeneralCategoryResponse>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GeneralCategoryCreateRequest request) {
        GeneralCategoryResponse response = generalCategoryService.create(userId, request);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.GENERAL_CREATED, response));
    }

    // GET /api/general-categories
    @GetMapping
    public ResponseEntity<ApiResponse<List<GeneralCategoryResponse>>> getAll(@AuthenticationPrincipal Long userId) {
        List<GeneralCategoryResponse> response = generalCategoryService.getAll(userId);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.GENERALS_FOUND, response));
    }

    // GET /api/general-categories/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneralCategoryResponse>> getOne(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        GeneralCategoryResponse response = generalCategoryService.getOne(userId, id);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.GENERAL_FOUND, response));
    }

    // PUT /api/general-categories/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneralCategoryResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GeneralCategoryUpdateRequest request) {
        GeneralCategoryResponse response = generalCategoryService.update(userId, id, request);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.GENERAL_UPDATED, response));
    }

    // DELETE /api/general-categories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        generalCategoryService.delete(userId, id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}