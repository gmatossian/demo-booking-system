package demo.booking.web;

import java.util.List;

/** A request that fails a business rule; reported as 400 with field errors. */
public class InvalidRequestException extends RuntimeException {

    private final List<ApiFieldError> errors;

    public InvalidRequestException(List<ApiFieldError> errors) {
        super("Invalid request: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<ApiFieldError> errors() {
        return errors;
    }
}
