package xyz.ph59.med.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseInfo {
    private String jwt;
    private String token;
}
