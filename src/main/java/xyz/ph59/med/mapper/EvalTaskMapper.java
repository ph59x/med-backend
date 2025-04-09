package xyz.ph59.med.mapper;

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

    List<EvalTaskInfo> selectTasks(@Param("taskId") String taskId,
                                   @Param("callerId") Long callerId,
                                   @Param("targetId") Long targetId,
                                   @Param("status") String status,
                                   @Param("createTimeStart") LocalDateTime createTimeStart,
                                   @Param("createTimeEnd") LocalDateTime createTimeEnd,
                                   @Param("endTimeStart") LocalDateTime endTimeStart,
                                   @Param("endTimeEnd") LocalDateTime endTimeEnd);

    EvalResult selectResultByTaskId(String taskId);
}
