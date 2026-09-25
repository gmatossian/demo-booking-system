package demo.booking.web;

import java.util.Map;

/**
 * A request that conflicts with current state; reported as 409. The code lets
 * clients tell conflict kinds apart, and details are added to the response body.
 */
public class ConflictException extends RuntimeException {

    private final String code;
    private final Map<String, Object> details;

    public ConflictException(String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = Map.copyOf(details);
    }

    public String code() {
        return code;
    }

    public Map<String, Object> details() {
        return details;
    }
}
