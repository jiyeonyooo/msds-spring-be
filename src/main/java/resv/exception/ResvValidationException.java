package resv.exception;

public class ResvValidationException extends RuntimeException {

    private final ValidationError error;

    public ResvValidationException(String field, String reason, String message) {
        super(message);
        this.error = new ValidationError(field, reason, message);
    }

    public ValidationError getError() {
        return error;
    }
}
