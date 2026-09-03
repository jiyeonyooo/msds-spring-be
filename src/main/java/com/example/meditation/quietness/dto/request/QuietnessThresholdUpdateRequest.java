package com.example.meditation.quietness.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record QuietnessThresholdUpdateRequest(
        @NotNull @DecimalMin("0.00") @DecimalMax("999.98") @Digits(integer = 3, fraction = 2)
        BigDecimal veryQuietMax,
        @NotNull @DecimalMin("0.01") @DecimalMax("999.98") @Digits(integer = 3, fraction = 2)
        BigDecimal quietMax,
        @NotNull @DecimalMin("0.02") @DecimalMax("999.98") @Digits(integer = 3, fraction = 2)
        BigDecimal normalMax,
        @NotNull @DecimalMin("0.03") @DecimalMax("999.98") @Digits(integer = 3, fraction = 2)
        BigDecimal loudMax
) {
}
