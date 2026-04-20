package com.naleul.naleul.domain.goalCategory.service;

import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.color.repository.ColorRepository;
import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.generalCategory.repository.GeneralCategoryRepository;
import com.naleul.naleul.domain.goalCategory.dto.request.GeneralCategoryAssignRequest;
import com.naleul.naleul.domain.goalCategory.dto.request.GoalCategoryCompleteRequest;
import com.naleul.naleul.domain.goalCategory.dto.request.GoalCategoryCreateRequest;
import com.naleul.naleul.domain.goalCategory.dto.request.GoalCategoryUpdateRequest;
import com.naleul.naleul.domain.goalCategory.dto.response.GoalCategoryResponse;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.enums.GoalCategoryStatus;
import com.naleul.naleul.domain.goalCategory.repository.GoalCategoryRepository;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.global.common.response.ErrorCode;
import com.naleul.naleul.global.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalCategoryService {

    private final GoalCategoryRepository goalCategoryRepository;
    private final GeneralCategoryRepository generalCategoryRepository;
    private final ColorRepository colorRepository;
    private final UserRepository userRepository;

    // 목표 카테고리 생성
    @Transactional
    public GoalCategoryResponse create(GoalCategoryCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1번: Color가 없는 경우
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new CustomException(ErrorCode.COLOR_NOT_FOUND));

        // 2번: 동일한 goalCategoryName이 있는 경우
        if (goalCategoryRepository.existsByUser_UserIdAndGoalCategoryName(userId, request.getGoalCategoryName())) {
            throw new CustomException(ErrorCode.GOAL_CATEGORY_NAME_DUPLICATED);
        }

        GoalCategory goalCategory = GoalCategory.builder()
                .user(user)
                .color(color)
                .goalCategoryName(request.getGoalCategoryName())
                .goalCategoryStatus(request.getGoalCategoryStatus())
                .goalCategoryStartDate(request.getGoalCategoryStartDate())
                .build();

        goalCategoryRepository.save(goalCategory);

        GoalCategory saved = goalCategoryRepository.findByIdWithAll(goalCategory.getGoalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        return GoalCategoryResponse.from(saved);
    }

    // 목표 카테고리 단건 조회
    public GoalCategoryResponse getGoalCategory(Long goalCategoryId) {
        GoalCategory goalCategory = goalCategoryRepository.findByIdWithAll(goalCategoryId)
                .orElseThrow(() ->new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        return GoalCategoryResponse.from(goalCategory);
    }

    // 목표 카테고리 전체 조회 (유저별)
    public List<GoalCategoryResponse> getGoalCategories(Long userId) {
        return goalCategoryRepository.findAllByUserIdWithAll(userId)
                .stream()
                .map(GoalCategoryResponse::from)
                .toList();
    }


    // 기본 목표 카테고리 기타 추가
    @Transactional
    public GoalCategory createDefaultEtcCategory(User user) {
        GoalCategory etcCategory = GoalCategory.builder()
                .user(user)
                .color(null)                          // 기본 색상 없음 (or 기본 Color 지정)
                .goalCategoryName("기타")
                .goalCategoryStatus(GoalCategoryStatus.IN_PROGRESS)
                .goalCategoryStartDate(LocalDate.now())
                .build();

        return goalCategoryRepository.save(etcCategory);
    }

    // 목표 완료 처리
    @Transactional
    public GoalCategoryResponse complete(Long goalCategoryId, GoalCategoryCompleteRequest request) {

        // 3번: goalCategoryId가 없는 경우
        GoalCategory goalCategory = goalCategoryRepository.findById(goalCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        goalCategory.complete(request.getGoalCategoryEndDate(), request.getAchievement());

        return GoalCategoryResponse.from(goalCategory);
    }

    // 일반 카테고리와 연결
    @Transactional
    public GoalCategoryResponse assignGeneralCategories(Long goalCategoryId,
                                                        GeneralCategoryAssignRequest request) {
        GoalCategory goalCategory = goalCategoryRepository.findById(goalCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        List<GeneralCategory> generalCategories = generalCategoryRepository
                .findAllById(request.getGeneralCategoryIds());

        generalCategories.forEach(generalCategory -> generalCategory.assignGoalCategory(goalCategory));

        GoalCategory updated = goalCategoryRepository.findByIdWithAll(goalCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        return GoalCategoryResponse.from(updated);
    }

    // 목표 카테고리 수정
    @Transactional
    public GoalCategoryResponse update(Long goalCategoryId, GoalCategoryUpdateRequest request) {
        GoalCategory goalCategory = goalCategoryRepository.findById(goalCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        // Color 변경이 있을 때만 조회
        if (request.getColorId() != null) {
            Color color = colorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COLOR_NOT_FOUND));
            goalCategory.updateColor(color);
        }

        // goalCategoryName을 보낸 경우에만 중복 검증
        if (request.getGoalCategoryName() != null) {
            boolean isDuplicated = goalCategoryRepository
                    .existsByUser_UserIdAndGoalCategoryNameAndGoalCategoryIdNot(
                            goalCategory.getUser().getUserId(),
                            request.getGoalCategoryName(),
                            goalCategoryId  // 자기 자신은 제외
                    );
            if (isDuplicated) {
                throw new CustomException(ErrorCode.GOAL_CATEGORY_NAME_DUPLICATED);
            }
        }

        goalCategory.update(
                request.getGoalCategoryName(),
                request.getGoalCategoryStatus(),
                request.getGoalCategoryStartDate()
        );

        return GoalCategoryResponse.from(goalCategory);
    }

    // 목표 카테고리 소프트 삭제
    @Transactional
    public void delete(Long goalCategoryId) {
        GoalCategory goalCategory = goalCategoryRepository.findById(goalCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        goalCategory.delete();
    }
}
