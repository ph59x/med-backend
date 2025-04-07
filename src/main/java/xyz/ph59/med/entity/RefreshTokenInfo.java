package xyz.ph59.med.entity;

import lombok.Data;

@Data
public class RefreshTokenInfo {
    private final Integer uid;
    private final String role;
    private final String ip;
    private final String ua;
}
