package xyz.ph59.med.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseInfo {
    @JsonIgnore
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private int uid;
    private String username;
}
