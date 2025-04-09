package xyz.ph59.med.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvalResult {
    private boolean success;
    private Integer evalCost;
    private String type;
}
