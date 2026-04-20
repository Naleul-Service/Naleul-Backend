package com.naleul.naleul.domain.generalCategory.dto.response;

import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import lombok.Getter;

@Getter
public class GeneralCategoryResponse {

    private final Long generalCategoryId;
    private final String generalCategoryName;
    private final boolean isDefault;
    private final Long goalCategoryId;
    private final String goalCategoryName;
    private final Long colorId;

    public GeneralCategoryResponse(GeneralCategory generalCategory) {
        this.generalCategoryId = generalCategory.getGeneralCategoryId();
        this.generalCategoryName = generalCategory.getGeneralCategoryName();
        this.isDefault = generalCategory.isDefault();
        this.goalCategoryId = generalCategory.getGoalCategory().getGoalCategoryId();
        this.goalCategoryName = generalCategory.getGoalCategory().getGoalCategoryName();
        this.colorId = generalCategory.getColor() != null ? generalCategory.getColor().getColorId() : null;
    }
}