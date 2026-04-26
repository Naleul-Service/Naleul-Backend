package com.naleul.naleul.domain.goalCategory.controller;

import com.naleul.naleul.domain.goalCategory.dto.request.GeneralCategoryAssignRequest;
import com.naleul.naleul.domain.goalCategory.dto.request.GoalCategoryCompleteRequest;
import com.naleul.naleul.domain.goalCategory.dto.request.GoalCategoryCreateRequest;
import com.naleul.naleul.domain.goalCategory.dto.request.GoalCategoryUpdateRequest;
import com.naleul.naleul.domain.goalCategory.dto.response.CompletedGoalCategoryPageResponse;
import com.naleul.naleul.domain.goalCategory.dto.response.GoalCategoryResponse;
import com.naleul.naleul.domain.goalCategory.service.GoalCategoryService;
import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.naleul.naleul.domain.goalCategory.dto.response.CompletedGoalCategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/goal-categories")
public class GoalCategoryController {
    private final GoalCategoryService goalCategoryService;

    // 목표 카테고리 생성
    @PostMapping
    public ResponseEntity<ApiResponse<GoalCategoryResponse>> create(
            @RequestBody GoalCategoryCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        GoalCategoryResponse response = goalCategoryService.create(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.GOAL_CREATED, response));
    }

    // 목표 카테고리 단건 조회
    @GetMapping("/{goalCategoryId}")
    public ResponseEntity<ApiResponse<GoalCategoryResponse>> getGoalCategory(
            @PathVariable Long goalCategoryId
    ) {
        GoalCategoryResponse response = goalCategoryService.getGoalCategory(goalCategoryId);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.GOAL_FOUND, response));
    }

    // 목표 카테고리 전체 조회 (내 목표만)
    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalCategoryResponse>>> getGoalCategories(
            @AuthenticationPrincipal Long userId
    ) {
        List<GoalCategoryResponse> response = goalCategoryService.getGoalCategories(userId);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.GOAL_FOUND, response));
    }

    // 목표 카테고리 수정
    @PatchMapping("/{goalCategoryId}")
    public ResponseEntity<ApiResponse<GoalCategoryResponse>> update(
            @PathVariable Long goalCategoryId,
            @RequestBody GoalCategoryUpdateRequest request
    ) {
        GoalCategoryResponse response = goalCategoryService.update(goalCategoryId, request);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.GOAL_UPDATED, response));
    }

    // 목표 완료 처리
    @PatchMapping("/{goalCategoryId}/complete")
    public ResponseEntity<ApiResponse<GoalCategoryResponse>> complete(
            @PathVariable Long goalCategoryId,
            @Valid @RequestBody GoalCategoryCompleteRequest request
    ) {
        GoalCategoryResponse response = goalCategoryService.complete(goalCategoryId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.GOAL_COMPLETED, response));
    }

    // GeneralCategory 연결 (드래그 앤 드롭)
    @PatchMapping("/{goalCategoryId}/general-categories")
    public ResponseEntity<ApiResponse<GoalCategoryResponse>> assignGeneralCategories(
            @PathVariable Long goalCategoryId,
            @RequestBody GeneralCategoryAssignRequest request
    ) {
        GoalCategoryResponse response = goalCategoryService.assignGeneralCategories(goalCategoryId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.GOAL_ASSIGNED, response));
    }

    // 목표 카테고리 소프트 삭제
    @DeleteMapping("/{goalCategoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long goalCategoryId
    ) {
        goalCategoryService.delete(goalCategoryId);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.GOAL_DELETED, null));
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<CompletedGoalCategoryPageResponse>> getCompletedGoalCategories(
            @AuthenticationPrincipal Long userId,
            Pageable pageable
    ) {
        CompletedGoalCategoryPageResponse response = goalCategoryService.getCompletedGoalCategories(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.GOAL_CATEGORIES_FOUND, response));
    }
}
