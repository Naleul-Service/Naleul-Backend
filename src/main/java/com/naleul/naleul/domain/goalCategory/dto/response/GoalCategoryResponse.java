package com.naleul.naleul.domain.goalCategory.dto.response;

import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.enums.GoalCategoryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class GoalCategoryResponse {

    private Long goalCategoryId;
    private String goalCategoryName;
    private GoalCategoryStatus goalCategoryStatus;
    private LocalDate goalCategoryStartDate;
    private LocalDate goalCategoryEndDate;
    private String achievement;
    private String colorCode;
    private List<GeneralCategoryInfo> generalCategories;

    public static GoalCategoryResponse from(GoalCategory goalCategory) {
        return GoalCategoryResponse.builder()
                .goalCategoryId(goalCategory.getGoalCategoryId())
                .goalCategoryName(goalCategory.getGoalCategoryName())
                .goalCategoryStatus(goalCategory.getGoalCategoryStatus())
                .goalCategoryStartDate(goalCategory.getGoalCategoryStartDate())
                .goalCategoryEndDate(goalCategory.getGoalCategoryEndDate())
                .achievement(goalCategory.getAchievement())
                .colorCode(goalCategory.getColor().getColorCode())
                .generalCategories(goalCategory.getGeneralCategories().stream()
                        .map(GeneralCategoryInfo::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class GeneralCategoryInfo {
        private Long generalCategoryId;
        private String generalCategoryName;
        private String colorCode;

        public static GeneralCategoryInfo from(GeneralCategory generalCategory) {
            return GeneralCategoryInfo.builder()
                    .generalCategoryId(generalCategory.getGeneralCategoryId())
                    .generalCategoryName(generalCategory.getGeneralCategoryName())
                    .colorCode(generalCategory.getColor().getColorCode())
                    .build();
        }
    }
}