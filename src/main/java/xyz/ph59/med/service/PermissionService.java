package xyz.ph59.med.service;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.ph59.med.entity.permission.RolePermission;
import xyz.ph59.med.entity.permission.UserPermission;
import xyz.ph59.med.mapper.PermissionMapper;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionMapper permissionMapper;
    // TODO 自定义RedisTemplate
    private final StringRedisTemplate redisTemplate;

    @Transactional(readOnly = true)
    public UserPermission getUserPermission(int uid) {
        String key = "Perm:" + uid;
        // TODO 使用类似hashmap的结构缓存
        UserPermission cache = JSON.parseObject(redisTemplate.opsForValue().get(key), UserPermission.class);
        if (cache != null) {
            return cache;
        }

        UserPermission result = new UserPermission();
        List<RolePermission> rolesWithPermissions = permissionMapper.selectRolesWithPermissions(uid);

        rolesWithPermissions.forEach(rp  -> {
            result.getRoles().add(rp.getRoleCode());
            rp.getPermissions().forEach(p  ->
                    result.getPermissions()
                            .computeIfAbsent(p.getCode(),  k -> new HashSet<>())
                            .addAll(p.getScopes()));
        });

        // TODO 使用类似hashmap的结构缓存
        redisTemplate.opsForValue().set(key, JSON.toJSONString(result), 1, TimeUnit.HOURS);

        return result;
    }

    @Transactional
    // TODO 前置参数校验
    public void updateUserRoles(Integer uid, List<String> newRoleCodes) {
        permissionMapper.deleteRolesByUserId(uid);

        if (!newRoleCodes.isEmpty())  {
            permissionMapper.insertUserRoles(uid,  newRoleCodes);
        }

        redisTemplate.delete("Perm:"  + uid);
    }

    @Transactional
    public void refreshUserPermission(int uid) {
        // TODO 根据TTL确定是否刷新
        redisTemplate.delete("Perm:"  + uid);

        this.getUserPermission(uid);
    }
}
