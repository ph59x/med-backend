package xyz.ph59.med.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.ph59.med.entity.eval.EvalTaskInfo;
import xyz.ph59.med.mapper.EvalTaskMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DataService {
    private static final int EVAL_TASK_TTL = 1800;
    private static final String EVAL_TASK_KEY_PREFIX = "evalTask";

    private final StringRedisTemplate redisTemplate;
    private final EvalTaskMapper evalTaskMapper;

    @Transactional
    public void writeEvalTask(@NonNull EvalTaskInfo taskInfo) {
        String key = EVAL_TASK_KEY_PREFIX + ":" + taskInfo.getTaskId();
        redisTemplate.opsForHash().putAll(key, taskInfo.toHashMap());
        redisTemplate.expire(key, EVAL_TASK_TTL, TimeUnit.SECONDS);

        evalTaskMapper.insertTask(taskInfo);
    }

    @Transactional
    public void updateEvalTask(@NonNull EvalTaskInfo taskInfo) throws IllegalArgumentException {
        if (taskInfo.getTaskId() == null || taskInfo.getTaskId().isEmpty()) {
            throw new IllegalArgumentException("未传入taskId");
        }

        String key = EVAL_TASK_KEY_PREFIX + ":" + taskInfo.getTaskId();

        Map<String, String> tmp = taskInfo.toHashMap();
        for (Map.Entry<String, String> entry : tmp.entrySet()) {
            if (entry.getValue() == null) {
                tmp.remove(entry.getKey());
            }
        }

        if (!redisTemplate.hasKey(key)) {
            Map<String, String> orig = evalTaskMapper.selectTasks(
                    taskInfo.getTaskId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ).get(0).toHashMap();
            for (Map.Entry<String, String> entry : orig.entrySet()) {
                tmp.merge(entry.getKey(), entry.getValue(), (oldValue, newValue) -> oldValue);
            }
        }

        redisTemplate.opsForHash().putAll(key, tmp);
        redisTemplate.expire(key, EVAL_TASK_TTL, TimeUnit.SECONDS);

        evalTaskMapper.updateTaskResult(taskInfo);
    }

    public EvalTaskInfo queryEvalTask(@NonNull String taskId) {
        String key = EVAL_TASK_KEY_PREFIX + ":" + taskId;
        EvalTaskInfo taskInfo = new EvalTaskInfo();
        if (redisTemplate.hasKey(key)) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            taskInfo.setTaskId(taskId);
            taskInfo.setCreateTime(entries.get("createTime") != null ?
                    LocalDateTime.parse((String) entries.get("createTime")) : null);
            taskInfo.setEndTime(entries.get("endTime") != null ?
                    LocalDateTime.parse((String) entries.get("endTime")) : null);
            taskInfo.setStatus((String) entries.get("status"));
            taskInfo.setEvalCostTime(entries.get("evalCostTime") != null ?
                    Integer.parseInt((String) entries.get("evalCostTime")) : null);
            taskInfo.setTargetId(Long.parseLong((String) entries.get("targetId")));
            taskInfo.setTargetTimeStart(entries.get("targetTimeStart") != null ?
                    LocalDateTime.parse((String) entries.get("targetTimeStart")) : null);
            taskInfo.setTargetTimeEnd(entries.get("targetTimeEnd") != null ?
                    LocalDateTime.parse((String) entries.get("targetTimeEnd")) : null);
            taskInfo.setResult((String) entries.get("result"));
            taskInfo.setCallerId(Long.parseLong((String) entries.get("callerId")));
        } else {
            taskInfo = evalTaskMapper.selectTasks(
                    taskId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ).get(0);

            redisTemplate.opsForHash().putAll(key, taskInfo.toHashMap());
            redisTemplate.expire(key, EVAL_TASK_TTL, TimeUnit.SECONDS);
        }

        return taskInfo;
    }

    public Integer queryEvalTaskTargetId(@NonNull String taskId) {
        String key = EVAL_TASK_KEY_PREFIX + ":" + taskId;
        if (!redisTemplate.hasKey(key)) {
            this.queryEvalTask(taskId);
        }
        return Integer.parseInt((String) Objects.requireNonNull(redisTemplate.opsForHash().get(key, "targetId")));
    }
}
