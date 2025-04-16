package xyz.ph59.med.tsdb;

import org.springframework.lang.NonNull;
import xyz.ph59.med.entity.DataPoint;

import java.time.ZonedDateTime;
import java.util.List;

public interface TsdbConnector {
    /**
     * 查询数据
     *
     * @param uid       目标用户id
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 数据点列表
     */
    List<DataPoint> query(int uid,
                          @NonNull ZonedDateTime startTime,
                          @NonNull ZonedDateTime endTime);

    /**
     * 查询数据，用于创建评估任务
     *
     * @param uid       目标用户id
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 已格式化的数据点
     */
    List<Short[]> queryForEval(int uid,
                               @NonNull ZonedDateTime startTime,
                               @NonNull ZonedDateTime endTime);

    /**
     * 写入数据
     *
     * @param uid        目标用户id
     * @param dataPoints 要写入的数据点
     */
    void write(int uid,
               @NonNull List<DataPoint> dataPoints);
}
