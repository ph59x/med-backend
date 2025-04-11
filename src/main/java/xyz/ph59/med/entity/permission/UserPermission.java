package xyz.ph59.med.entity.permission;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class UserPermission {
    private Integer userId;
    private Set<String> roles;
    private Map<String, Set<String>> permissions;

    public UserPermission() {
        this.roles = new HashSet<>();
        this.permissions = new HashMap<>();
    }
}
