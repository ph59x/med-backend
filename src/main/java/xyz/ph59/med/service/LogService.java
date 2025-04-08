package xyz.ph59.med.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.ph59.med.entity.OperationLog;
import xyz.ph59.med.entity.OperationLogQuery;
import xyz.ph59.med.mapper.OperationLogMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {
    private final OperationLogMapper logMapper;

    @Transactional
    public void saveOperationLog(OperationLog log) {
        if (log.getTime() == null) {
            log.setTime(LocalDateTime.now());
        }
        logMapper.insert(log);
    }

    public List<OperationLog> queryLogs(OperationLogQuery query) {
        return logMapper.selectByCondition(query);
    }
}
