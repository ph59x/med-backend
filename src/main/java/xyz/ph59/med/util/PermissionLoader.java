package xyz.ph59.med.util;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xyz.ph59.med.entity.permission.UserPermission;
import xyz.ph59.med.service.PermissionService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionLoader implements StpInterface {
    private final PermissionService permissionService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        UserPermission userPermission = permissionService.getUserPermission(Integer.parseInt((String) loginId));
        return userPermission.getPermissions().keySet().stream().toList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        UserPermission userPermission = permissionService.getUserPermission(Integer.parseInt((String) loginId));
        return userPermission.getRoles().stream().toList();
    }
}
