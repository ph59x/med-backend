package xyz.ph59.med.exception;

public class InvalidTokenException extends IllegalArgumentException {
    public InvalidTokenException() {
        super("提供的token无效");
    }
}
