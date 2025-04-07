package xyz.ph59.med.exception;

public class UnauthorizedException extends IllegalAccessException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
