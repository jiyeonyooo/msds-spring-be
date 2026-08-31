package global.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK.name(), message, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(
            HttpStatus status,
            String message,
            T data
    ) {
        ApiResponse<T> response = new ApiResponse<>(status.name(), message, data);
        return ResponseEntity.status(status).body(response);
    }

    public static ResponseEntity<ApiResponse<Void>> error(
            HttpStatus status,
            String message
    ) {
        ApiResponse<Void> response = new ApiResponse<>(status.name(), message, null);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
