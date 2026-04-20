package com.naleul.naleul.domain.generalCategory.repository;

import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeneralCategoryRepository extends JpaRepository<GeneralCategory, Long> {
    // 유저의 전체 generalCategory 조회
    List<GeneralCategory> findAllByUser(User user);

    // 유저의 ETC 카테고리 조회 (기본값 확인용)
    Optional<GeneralCategory> findByUserAndIsDefaultTrue(User user);

    // 유저 소유 확인 (보안: 다른 유저 카테고리 접근 방지)
    Optional<GeneralCategory> findByGeneralCategoryIdAndUser(Long id, User user);
}
