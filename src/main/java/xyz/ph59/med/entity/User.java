package xyz.ph59.med.entity;

import lombok.Data;

@Data
public class User {
    private int id;
    private String username;
    private String hash;
    private String role;
}
