package xyz.ph59.med.mapper;

import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.ph59.med.entity.EvalResult;
import xyz.ph59.med.entity.EvalTaskInfo;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EvalTaskMapper {
    int insertTask(EvalTaskInfo task);

    int updateTaskResult(EvalTaskInfo task);

    List<EvalTaskInfo> selectTasks(@Param("taskId") @Nullable String taskId,
                                   @Param("callerId") @Nullable Long callerId,
                                   @Param("targetId") @Nullable Long targetId,
                                   @Param("status") @Nullable String status,
                                   @Param("createTimeStart") @Nullable LocalDateTime createTimeStart,
                                   @Param("createTimeEnd") @Nullable LocalDateTime createTimeEnd,
                                   @Param("endTimeStart") @Nullable LocalDateTime endTimeStart,
                                   @Param("endTimeEnd") @Nullable LocalDateTime endTimeEnd);

    EvalResult selectResultByTaskId(String taskId);
}
