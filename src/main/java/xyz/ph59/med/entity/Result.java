package xyz.ph59.med.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import xyz.ph59.med.util.ResultBuilder;

@Data
@AllArgsConstructor
public class Result {
    @JsonProperty
    private int code;
    @JsonProperty
    private String message;
    @JsonProperty
    private Object data;

    public static ResultBuilder builder(HttpStatus code) {
        return new ResultBuilder(code);
    }

}
