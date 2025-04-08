package xyz.ph59.med.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogQuery extends OperationLog{
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
