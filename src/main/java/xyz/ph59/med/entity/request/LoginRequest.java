package xyz.ph59.med.entity.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
