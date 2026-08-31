package resv.exception;

public class ResvException extends RuntimeException {

    private final ResvErrorCode errorCode;

    public ResvException(ResvErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ResvErrorCode getErrorCode() {
        return errorCode;
    }
}
