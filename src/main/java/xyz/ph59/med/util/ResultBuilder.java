package xyz.ph59.med.util;

import org.springframework.http.HttpStatus;
import xyz.ph59.med.entity.Result;

public class ResultBuilder {
    private final HttpStatus code;
    private String message;
    private Object data;

    public ResultBuilder(HttpStatus code) {
        this.code = code;
    }

    public ResultBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ResultBuilder data(Object data) {
        this.data = data;
        return this;
    }

    public Result build() {
        if (message == null) {
            message = code.getReasonPhrase();
        }
        return new Result(code.value(), message, data);
    }
}
