package xyz.ph59.med.mapper;

import org.apache.ibatis.annotations.Mapper;
import xyz.ph59.med.entity.User;

@Mapper
public interface UserMapper {
    void insertUser(User user);
    User selectByUsername(String username);
}