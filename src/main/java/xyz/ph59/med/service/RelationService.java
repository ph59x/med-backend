package xyz.ph59.med.service;

import cn.dev33.satoken.annotation.SaCheckRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.ph59.med.mapper.RelationMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelationService {
    private final RelationMapper relationMapper;

    @SaCheckRole("DOCTOR")
    public List<Integer> getUserRelation(int drId) {
        return relationMapper.selectRelatedUser(drId);
        // TODO hashmap缓存
    }
}
