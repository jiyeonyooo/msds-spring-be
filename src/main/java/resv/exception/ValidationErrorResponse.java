package resv.exception;

import java.util.List;

public record ValidationErrorResponse(List<ValidationError> errors) {
}
