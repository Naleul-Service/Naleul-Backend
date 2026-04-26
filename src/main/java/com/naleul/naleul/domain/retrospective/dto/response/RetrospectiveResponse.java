// retrospective/dto/response/RetrospectiveResponse.java
package com.naleul.naleul.domain.retrospective.dto.response;

import com.naleul.naleul.domain.retrospective.entity.Retrospective;
import com.naleul.naleul.domain.retrospective.enums.ReviewType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RetrospectiveResponse {

    private final Long retrospectiveId;
    private final ReviewType reviewType;
    private final LocalDate reviewDate;
    private final String content;

    private final Long goalCategoryId;
    private final String goalCategoryName;
    private final String goalCategoryColorCode;      // ✅ 추가

    private final Long generalCategoryId;
    private final String generalCategoryName;
    private final String generalCategoryColorCode;   // ✅ 추가

    private RetrospectiveResponse(Retrospective retrospective) {
        this.retrospectiveId = retrospective.getRetrospectiveId();
        this.reviewType = retrospective.getReviewType();
        this.reviewDate = retrospective.getReviewDate();
        this.content = retrospective.getContent();

        this.goalCategoryId = retrospective.getGoalCategory() != null
                ? retrospective.getGoalCategory().getGoalCategoryId() : null;
        this.goalCategoryName = retrospective.getGoalCategory() != null
                ? retrospective.getGoalCategory().getGoalCategoryName() : null;
        this.goalCategoryColorCode = retrospective.getGoalCategory() != null
                && retrospective.getGoalCategory().getUserColor() != null
                ? retrospective.getGoalCategory().getUserColor().getColorCode() : null; // ✅

        this.generalCategoryId = retrospective.getGeneralCategory() != null
                ? retrospective.getGeneralCategory().getGeneralCategoryId() : null;
        this.generalCategoryName = retrospective.getGeneralCategory() != null
                ? retrospective.getGeneralCategory().getGeneralCategoryName() : null;
        this.generalCategoryColorCode = retrospective.getGeneralCategory() != null
                && retrospective.getGeneralCategory().getColor() != null
                ? retrospective.getGeneralCategory().getColor().getColorCode() : null; // ✅
    }

    public static RetrospectiveResponse from(Retrospective retrospective) {
        return new RetrospectiveResponse(retrospective);
    }
}