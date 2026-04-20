package com.naleul.naleul.domain.generalCategory.service;


import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.color.repository.ColorRepository;
import com.naleul.naleul.domain.generalCategory.dto.request.GeneralCategoryCreateRequest;
import com.naleul.naleul.domain.generalCategory.dto.request.GeneralCategoryUpdateRequest;
import com.naleul.naleul.domain.generalCategory.dto.response.GeneralCategoryResponse;
import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.generalCategory.repository.GeneralCategoryRepository;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.repository.GoalCategoryRepository;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용 (성능 최적화)
public class GeneralCategoryService {

    private final GeneralCategoryRepository generalCategoryRepository;
    private final GoalCategoryRepository goalCategoryRepository;
    private final ColorRepository colorRepository;
    private final UserRepository userRepository;

    // 회원가입 시 ETC 자동 생성 (UserService에서 호출)
    @Transactional
    public void createDefaultCategory(User user, GoalCategory etcGoalCategory) {
        GeneralCategory defaultCategory = GeneralCategory.createDefault(user, etcGoalCategory);
        generalCategoryRepository.save(defaultCategory);
    }

    // 전체 조회
    public List<GeneralCategoryResponse> getAll(Long userId) {
        User user = findUserById(userId);
        return generalCategoryRepository.findAllByUser(user)
                .stream()
                .map(GeneralCategoryResponse::new)
                .collect(Collectors.toList());
    }

    // 단건 조회
    public GeneralCategoryResponse getOne(Long userId, Long generalCategoryId) {
        User user = findUserById(userId);
        GeneralCategory generalCategory = findByIdAndUser(generalCategoryId, user);
        return new GeneralCategoryResponse(generalCategory);
    }

    // 생성
    @Transactional
    public GeneralCategoryResponse create(Long userId, GeneralCategoryCreateRequest request) {
        User user = findUserById(userId);
        GoalCategory goalCategory = findGoalCategoryById(request.getGoalCategoryId());
        Color color = request.getColorId() != null ? findColorById(request.getColorId()) : null;

        GeneralCategory generalCategory = GeneralCategory.create(
                request.getGeneralCategoryName(),
                user,
                goalCategory,
                color
        );

        return new GeneralCategoryResponse(generalCategoryRepository.save(generalCategory));
    }

    // 수정 (ETC 보호)
    @Transactional
    public GeneralCategoryResponse update(Long userId, Long generalCategoryId, GeneralCategoryUpdateRequest request) {
        User user = findUserById(userId);
        GeneralCategory generalCategory = findByIdAndUser(generalCategoryId, user);

        // ETC 카테고리는 수정 불가
        if (generalCategory.isDefault()) {
            throw new IllegalStateException("기본 카테고리(ETC)는 수정할 수 없습니다.");
        }

        GoalCategory goalCategory = findGoalCategoryById(request.getGoalCategoryId());
        Color color = request.getColorId() != null ? findColorById(request.getColorId()) : null;

        generalCategory.update(request.getGeneralCategoryName(), goalCategory, color);

        // ✅ @Transactional 덕분에 save() 없이도 변경사항이 DB에 반영됨 (변경 감지)
        return new GeneralCategoryResponse(generalCategory);
    }

    // 삭제 (ETC 보호)
    @Transactional
    public void delete(Long userId, Long generalCategoryId) {
        User user = findUserById(userId);
        GeneralCategory generalCategory = findByIdAndUser(generalCategoryId, user);

        if (generalCategory.isDefault()) {
            throw new IllegalStateException("기본 카테고리(ETC)는 삭제할 수 없습니다.");
        }

        generalCategoryRepository.delete(generalCategory);
    }

    // ── private 헬퍼 메서드 ──────────────────────────────

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    private GeneralCategory findByIdAndUser(Long id, User user) {
        // 유저 소유 검증 포함 (다른 유저 카테고리 접근 방지)
        return generalCategoryRepository.findByGeneralCategoryIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
    }

    private GoalCategory findGoalCategoryById(Long id) {
        return goalCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표 카테고리입니다."));
    }

    private Color findColorById(Long id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 색상입니다."));
    }
}