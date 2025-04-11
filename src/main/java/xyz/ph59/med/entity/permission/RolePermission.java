package xyz.ph59.med.entity.permission;

import lombok.Data;

import java.util.List;

@Data
public class RolePermission {
    private String roleCode;
    private String roleName;
    private List<PermissionDetail> permissions;
}
