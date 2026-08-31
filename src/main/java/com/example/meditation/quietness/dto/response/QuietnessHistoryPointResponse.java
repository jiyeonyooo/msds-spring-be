package com.example.meditation.quietness.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuietnessHistoryPointResponse(
        BigDecimal decibel,
        LocalDateTime measuredAt
) {
}
