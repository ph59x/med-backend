package xyz.ph59.med.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvalTaskInfo {
    private String taskId;
    private LocalDateTime createTime;
    private LocalDateTime endTime;
    private String status;
    private Integer evalCostTime;
    private Long targetId;
    private LocalDateTime targetTimeStart;
    private LocalDateTime targetTimeEnd;
    private String result;
    private Long callerId;
}
