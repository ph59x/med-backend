package xyz.ph59.med.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RelationMapper {
    List<Integer> selectRelatedUser(int drId);
}
