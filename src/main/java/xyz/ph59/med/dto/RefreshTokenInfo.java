package xyz.ph59.med.dto;

import lombok.Data;

@Data
public class RefreshTokenInfo {
    private final Integer uid;
    private final String ip;
    private final String ua;
}
