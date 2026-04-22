// retrospective/service/RetrospectiveService.java
package com.naleul.naleul.domain.retrospective.service;

import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.generalCategory.repository.GeneralCategoryRepository;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.repository.GoalCategoryRepository;
import com.naleul.naleul.domain.retrospective.dto.request.RetrospectiveCreateRequest;
import com.naleul.naleul.domain.retrospective.dto.request.RetrospectiveUpdateRequest;
import com.naleul.naleul.domain.retrospective.dto.response.RetrospectiveResponse;
import com.naleul.naleul.domain.retrospective.entity.Retrospective;
import com.naleul.naleul.domain.retrospective.enums.ReviewType;
import com.naleul.naleul.domain.retrospective.repository.RetrospectiveRepository;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.global.common.response.ErrorCode;
import com.naleul.naleul.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetrospectiveService {

    private final RetrospectiveRepository retrospectiveRepository;
    private final UserRepository userRepository;
    private final GoalCategoryRepository goalCategoryRepository;
    private final GeneralCategoryRepository generalCategoryRepository;

    // 생성
    @Transactional
    public RetrospectiveResponse create(Long userId, RetrospectiveCreateRequest request) {
        User user = findUserById(userId);

        LocalDate reviewDate = resolveReviewDate(request.getReviewType(), request.getReviewDate());

        GoalCategory goalCategory = request.getGoalCategoryId() != null
                ? findGoalCategoryById(request.getGoalCategoryId()) : null;

        GeneralCategory generalCategory = request.getGeneralCategoryId() != null
                ? findGeneralCategoryById(request.getGeneralCategoryId()) : null;

        Retrospective retrospective = Retrospective.builder()
                .user(user)
                .reviewType(request.getReviewType())
                .reviewDate(reviewDate)
                .content(request.getContent())
                .goalCategory(goalCategory)
                .generalCategory(generalCategory)
                .isDeleted(false)
                .build();

        return RetrospectiveResponse.from(retrospectiveRepository.save(retrospective));
    }

    // 단건 조회
    public RetrospectiveResponse getOne(Long retrospectiveId) {
        return RetrospectiveResponse.from(findActiveById(retrospectiveId));
    }

    // 목록 조회 (필터 + 페이지네이션)
    // period: DAILY | WEEKLY | MONTHLY | null(전체)
    // 날짜 범위는 period + baseDate 기준으로 자동 계산
    public Page<RetrospectiveResponse> getList(
            Long userId,
            ReviewType reviewType,
            LocalDate baseDate,
            Long goalCategoryId,
            Long generalCategoryId,
            Pageable pageable
    ) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        DateRange range = resolveDateRange(reviewType, baseDate);

        return retrospectiveRepository
                .findAllByFilter(userId, reviewType, range.start(), range.end(), goalCategoryId, generalCategoryId, pageable)
                .map(RetrospectiveResponse::from);
    }

    // 수정
    @Transactional
    public RetrospectiveResponse update(Long retrospectiveId, RetrospectiveUpdateRequest request) {
        Retrospective retrospective = findActiveById(retrospectiveId);

        GoalCategory goalCategory = request.getGoalCategoryId() != null
                ? findGoalCategoryById(request.getGoalCategoryId()) : null;

        GeneralCategory generalCategory = request.getGeneralCategoryId() != null
                ? findGeneralCategoryById(request.getGeneralCategoryId()) : null;

        retrospective.update(request.getContent(), goalCategory, generalCategory);
        return RetrospectiveResponse.from(retrospective);
    }

    // 삭제 (소프트)
    @Transactional
    public void delete(Long retrospectiveId) {
        Retrospective retrospective = findActiveById(retrospectiveId);
        retrospective.delete();
    }

    // ── private 헬퍼 ──────────────────────────────────────

    // reviewDate가 null이면 타입 기준 오늘 날짜로 채움
    // DAILY → 오늘 / WEEKLY → 이번 주 월요일 / MONTHLY → 이번 달 1일
    private LocalDate resolveReviewDate(ReviewType type, LocalDate requestDate) {
        if (requestDate != null) return requestDate;

        LocalDate today = LocalDate.now();
        return switch (type) {
            case DAILY -> today;
            case WEEKLY -> today.with(WeekFields.of(Locale.KOREA).dayOfWeek(), 1); // 월요일
            case MONTHLY -> today.withDayOfMonth(1);
        };
    }

    // 조회 시 period + baseDate로 날짜 범위 결정
    // reviewType이 null이면 전체 범위 (start/end 모두 null)
    private DateRange resolveDateRange(ReviewType reviewType, LocalDate baseDate) {
        if (reviewType == null) return new DateRange(null, null);

        LocalDate base = baseDate != null ? baseDate : LocalDate.now();

        return switch (reviewType) {
            case DAILY -> new DateRange(base, base);
            case WEEKLY -> {
                LocalDate monday = base.with(WeekFields.of(Locale.KOREA).dayOfWeek(), 1);
                LocalDate sunday = monday.plusDays(6);
                yield new DateRange(monday, sunday);
            }
            case MONTHLY -> {
                LocalDate start = base.withDayOfMonth(1);
                LocalDate end = base.withDayOfMonth(base.lengthOfMonth());
                yield new DateRange(start, end);
            }
        };
    }

    private Retrospective findActiveById(Long id) {
        return retrospectiveRepository.findActiveById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RETROSPECTIVE_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private GoalCategory findGoalCategoryById(Long id) {
        return goalCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));
    }

    private GeneralCategory findGeneralCategoryById(Long id) {
        return generalCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERAL_CATEGORY_NOT_FOUND));
    }

    // RetrospectiveService — update, delete에 소유권 검증 추가

    @Transactional
    public RetrospectiveResponse update(Long userId, Long retrospectiveId, RetrospectiveUpdateRequest request) {
        Retrospective retrospective = findActiveByIdAndUserId(retrospectiveId, userId); // 변경

        GoalCategory goalCategory = request.getGoalCategoryId() != null
                ? findGoalCategoryById(request.getGoalCategoryId()) : null;
        GeneralCategory generalCategory = request.getGeneralCategoryId() != null
                ? findGeneralCategoryById(request.getGeneralCategoryId()) : null;

        retrospective.update(request.getContent(), goalCategory, generalCategory);
        return RetrospectiveResponse.from(retrospective);
    }

    @Transactional
    public void delete(Long userId, Long retrospectiveId) {
        Retrospective retrospective = findActiveByIdAndUserId(retrospectiveId, userId); // 변경
        retrospective.delete();
    }

    // findActiveById → findActiveByIdAndUserId로 교체
    private Retrospective findActiveByIdAndUserId(Long id, Long userId) {
        return retrospectiveRepository.findActiveByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RETROSPECTIVE_NOT_FOUND));
    }

    // 날짜 범위 값 객체 (Java 16+ record)
    private record DateRange(LocalDate start, LocalDate end) {}
}