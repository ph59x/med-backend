package xyz.ph59.med.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.ph59.med.entity.permission.RolePermission;

import java.util.List;

@Mapper
public interface PermissionMapper {
    List<RolePermission> selectRolesWithPermissions(Integer userId);

    int insertUserRoles(@Param("userId") Integer userId,
                        @Param("roleCodes") List<String> roleCodes);

    int deleteRolesByUserId(Integer userId);
}
