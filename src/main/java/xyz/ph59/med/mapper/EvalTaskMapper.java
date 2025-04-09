package xyz.ph59.med.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.ph59.med.entity.EvalTaskInfo;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EvalTaskMapper {
    // 创建任务（返回受影响行数）
    int insertTask(EvalTaskInfo task);

    // 更新任务结果（返回受影响行数）
    int updateTaskResult(EvalTaskInfo task);

    // 动态查询任务（返回实体集合）
    List<EvalTaskInfo> selectTasks(@Param("taskId") String taskId,
                                   @Param("callerId") Long callerId,
                                   @Param("targetId") Long targetId,
                                   @Param("status") String status,
                                   @Param("createTimeStart") LocalDateTime createTimeStart,
                                   @Param("createTimeEnd") LocalDateTime createTimeEnd,
                                   @Param("endTimeStart") LocalDateTime endTimeStart,
                                   @Param("endTimeEnd") LocalDateTime endTimeEnd);
}
