package xyz.ph59.med.service;

import cn.dev33.satoken.annotation.SaCheckRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xyz.ph59.med.mapper.RelationMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationService {
    private static final int DOCTOR_USER_RELATION_TTL = 1800;

    private final RelationMapper relationMapper;
    private final RedisTemplate<Object, Object> redisTemplate;

    @SaCheckRole("DOCTOR")
    public List<Integer> getUserRelation(int drId) {
        String key = "doctorUserRelation:" + drId;
        if (redisTemplate.hasKey(key)) {
            return redisTemplate.opsForSet()
                    .members(key)
                    .stream()
                    .map(object -> (Integer) object)
                    .collect(Collectors.toList());
        }
        else {
            return this.refreshUserRelation(drId);
        }
    }

    public List<Integer> refreshUserRelation(int drId) {
        String key = "doctorUserRelation:" + drId;
        List<Integer> relatedUser = relationMapper.selectRelatedUser(drId);
        for (Integer i : relatedUser) {
            redisTemplate.opsForSet().add(key, i);
        }
        redisTemplate.expire(key, DOCTOR_USER_RELATION_TTL, TimeUnit.SECONDS);
        return relatedUser;
    }
}
