package xyz.ph59.med.entity.eval;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, String> toHashMap() {
        Map<String, String> tmp = new HashMap<>();
        if (taskId != null) {
            tmp.put("taskId",  taskId);
        }
        if (createTime != null) {
            tmp.put("createTime",  createTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (endTime != null) {
            tmp.put("endTime",  endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (this.getStatus()  != null) {
            tmp.put("status",  this.getStatus());
        }
        if (this.getEvalCostTime()  != null) {
            tmp.put("evalCostTime",  String.valueOf(this.getEvalCostTime()));
        }
        if (targetId != null) {
            tmp.put("targetId",  targetId.toString());
        }
        if (targetTimeStart != null) {
            tmp.put("targetTimeStart",  targetTimeStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (targetTimeEnd != null) {
            tmp.put("targetTimeEnd",  targetTimeEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (this.getResult()  != null) {
            tmp.put("result",  this.getResult());
        }
        if (callerId != null) {
            tmp.put("callerId",  callerId.toString());
        }

        return tmp;
    }
}
