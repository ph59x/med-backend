package xyz.ph59.med.mapper;

import org.apache.ibatis.annotations.Mapper;
import xyz.ph59.med.entity.OperationLog;
import xyz.ph59.med.entity.OperationLogQuery;

import java.util.List;

@Mapper
public interface OperationLogMapper {
    int insert(OperationLog log);
    List<OperationLog> selectByCondition(OperationLogQuery query);
}
