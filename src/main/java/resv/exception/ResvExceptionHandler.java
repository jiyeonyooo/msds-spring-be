package resv.exception;

import global.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice(basePackages = "resv")
public class ResvExceptionHandler {

    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String VALIDATION_MESSAGE = "요청 값이 유효하지 않습니다.";

    @ExceptionHandler(ResvException.class)
    public ResponseEntity<ApiResponse<Void>> handleResvException(ResvException exception) {
        ResvErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.name(), errorCode.getMessage(), null));
    }

    @ExceptionHandler(ResvValidationException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleResvValidationException(
            ResvValidationException exception) {
        return validationFailed(List.of(exception.getError()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        List<ValidationError> errors = exception.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return new ValidationError(toSnakeCase(fieldError.getField()), reason(error.getCode()), error.getDefaultMessage());
                    }
                    return new ValidationError("request", reason(error.getCode()), error.getDefaultMessage());
                })
                .toList();
        return validationFailed(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleConstraintViolation(
            ConstraintViolationException exception) {
        List<ValidationError> errors = exception.getConstraintViolations().stream()
                .map(violation -> new ValidationError(
                        toSnakeCase(lastPathNode(violation)),
                        reason(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()),
                        violation.getMessage()))
                .toList();
        return validationFailed(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleHandlerMethodValidation(
            HandlerMethodValidationException exception) {
        List<ValidationError> errors = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ValidationError(
                                toSnakeCase(result.getMethodParameter().getParameterName()),
                                reason(error.getCodes() == null || error.getCodes().length == 0 ? null : error.getCodes()[0]),
                                error.getDefaultMessage())))
                .toList();
        return validationFailed(errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        String reason = exception.getRequiredType() != null && exception.getRequiredType().isEnum() ? "Enum" : "TypeMismatch";
        String message = "Enum".equals(reason)
                ? "예약 상태는 RESERVED 또는 CANCELLED만 가능합니다."
                : "요청 값의 형식이 올바르지 않습니다.";
        return validationFailed(List.of(new ValidationError(toSnakeCase(exception.getName()), reason, message)));
    }

    private ResponseEntity<ApiResponse<ValidationErrorResponse>> validationFailed(List<ValidationError> errors) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(VALIDATION_FAILED, VALIDATION_MESSAGE, new ValidationErrorResponse(errors)));
    }

    private String lastPathNode(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int separator = path.lastIndexOf('.');
        return separator >= 0 ? path.substring(separator + 1) : path;
    }

    private String reason(String code) {
        if (code == null) {
            return "Invalid";
        }
        if (code.contains("ValidDateRange")) {
            return "InvalidDateRange";
        }
        if (code.contains("typeMismatch")) {
            return "TypeMismatch";
        }
        int separator = code.lastIndexOf('.');
        return separator >= 0 ? code.substring(separator + 1) : code;
    }

    private String toSnakeCase(String value) {
        if (value == null || value.isBlank()) {
            return "request";
        }
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
