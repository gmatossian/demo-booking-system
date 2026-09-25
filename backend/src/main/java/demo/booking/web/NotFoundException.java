package demo.booking.web;

/** A requested record does not exist; reported as 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
