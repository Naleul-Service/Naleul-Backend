package com.naleul.naleul.domain.actualTask.controller;

import com.naleul.naleul.domain.actualTask.dto.request.TaskActualCreateRequest;
import com.naleul.naleul.domain.actualTask.dto.request.TaskActualDailyRequest;
import com.naleul.naleul.domain.actualTask.dto.request.TaskUpdateActualRequest;
import com.naleul.naleul.domain.actualTask.dto.response.TaskActualResponse;
import com.naleul.naleul.domain.actualTask.service.TaskActualService;
import com.naleul.naleul.domain.task.dto.response.TaskResponse;
import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-actuals")
@RequiredArgsConstructor
public class TaskActualController {

    private final TaskActualService taskActualService;

    // POST /api/task-actuals
    @PostMapping
    public ResponseEntity<TaskActualResponse> createActual(
            @AuthenticationPrincipal Long userId,
            @RequestBody TaskActualCreateRequest request
    ) {
        return ResponseEntity.ok(taskActualService.createActual(userId, request));
    }

    // GET /api/task-actuals/daily
    @GetMapping("/daily")
    public ResponseEntity<List<TaskActualResponse>> getDailyActuals(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute TaskActualDailyRequest request
    ) {
        return ResponseEntity.ok(taskActualService.getDailyActuals(userId, request));
    }


    // 실제 시간 기록 (PATCH: 일부만 수정)
    @PatchMapping("/{taskId}/actual")
    public ResponseEntity<ApiResponse<TaskResponse>> recordActual(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateActualRequest request
    ) {
        TaskResponse response = taskActualService.recordActual(userId, taskId, request);
        return ResponseEntity
                .ok(ApiResponse.success(SuccessCode.TASK_COMPLETED, response));
    }
}