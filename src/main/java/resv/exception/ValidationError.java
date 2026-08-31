package resv.exception;

public record ValidationError(String field, String reason, String message) {
}
