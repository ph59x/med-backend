package xyz.ph59.med.exception;

public class VerificationFailException extends RuntimeException {
    public VerificationFailException() {
        super("客户端验证失败，请重新登录");
    }
}
