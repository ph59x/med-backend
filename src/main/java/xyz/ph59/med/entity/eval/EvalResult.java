package xyz.ph59.med.entity.eval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvalResult {
    private String status;
    @JsonIgnore
    private Integer evalCost;
    private String type;
}
