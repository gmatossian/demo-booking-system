package demo.booking.web;

/** One invalid request field, e.g. {@code {"field": "end", "message": "..."}}. */
public record ApiFieldError(String field, String message) {
}
