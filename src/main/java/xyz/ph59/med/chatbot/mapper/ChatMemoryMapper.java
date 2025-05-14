package xyz.ph59.med.chatbot.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface ChatMemoryMapper {
    String queryContent(String chatId);
    Integer queryCreator(String chatId);

    int createChat(int uid, String chatId, LocalDateTime time, String content);
    int updateChat(String chatId, LocalDateTime time, String content);
    int removeContent(String chatId);

    boolean chatExist(String chatId);
}
