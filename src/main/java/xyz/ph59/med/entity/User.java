package xyz.ph59.med.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private int id;
    private String username;
    private String hash;
    private LocalDateTime createTime;
    private boolean disabled;
    private Integer deptId;
}
