package xyz.ph59.med.entity.permission;

import lombok.Data;

import java.util.Set;

@Data
public class PermissionDetail {
    private String code;
    private String name;
    private Set<String> scopes;
}
