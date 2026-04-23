package com.naleul.naleul.domain.actualTask.dto.request;

import java.time.LocalDateTime;

public record TaskUpdateActualRequest(

        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt
) {}