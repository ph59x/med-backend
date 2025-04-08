package xyz.ph59.med.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLog {
    private Long logId;
    private String ip;
    private String ua;
    private Long userId;
    private String username;
    private String eventType;
    private String action;
    private LocalDateTime time;
    private String requestMethod;
    private String path;
    private Integer status;
    private String result;
    private String errorMessage;
}
