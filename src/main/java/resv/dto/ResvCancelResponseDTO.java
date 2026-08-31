package resv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ResvCancelResponseDTO(
        @JsonProperty("resv_id") Long resvId,
        @JsonProperty("resv_number") String resvNumber,
        @JsonProperty("resv_status") String resvStatus,
        @JsonProperty("cancelled_at") LocalDateTime cancelledAt
) { }
