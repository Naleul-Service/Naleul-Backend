package com.naleul.naleul.domain.task.dto.request;

import java.time.LocalDateTime;

public record TaskUpdateActualRequest(

        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt
) {}