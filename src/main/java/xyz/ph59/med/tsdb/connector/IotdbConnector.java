package xyz.ph59.med.tsdb.connector;

import org.apache.iotdb.isession.SessionDataSet;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.tsfile.enums.TSDataType;
import org.apache.tsfile.read.common.Field;
import org.apache.tsfile.read.common.RowRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import xyz.ph59.med.entity.DataPoint;
import xyz.ph59.med.tsdb.TsdbConnector;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(name = "tsdb.type", havingValue = "iotdb")
public class IotdbConnector implements TsdbConnector {
    private final Session client;
    private final String queryPrefix;

    public IotdbConnector(
            @Value("${tsdb.iotdb.host:localhost}") String host,
            @Value("${tsdb.iotdb.port:6667}") int port,
            @Value("${tsdb.iotdb.username}") String username,
            @Value("${tsdb.iotdb.password}") String password,
            @Value("${tsdb.iotdb.table}") String tableName
    ) throws IoTDBConnectionException, StatementExecutionException {
        client = new Session.Builder()
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .build();

        client.open();
        client.setTimeZone("+08:00");

        queryPrefix = "root." + tableName + ".";
    }

    @Override
    public List<DataPoint> query(
            int uid,
            @NonNull ZonedDateTime startTime,
            @NonNull ZonedDateTime endTime
    ) {
        List<DataPoint> result = new ArrayList<>();
        String path = queryPrefix + uid;

        String sql = String.format(
                "SELECT afm, afmFlag, fmFlag, fhr, fhrQuality, toco, tocoFlag FROM %s WHERE time >= %d AND time <= %d",
                path, startTime.toInstant().toEpochMilli(), endTime.toInstant().toEpochMilli()
        );

        try {
            SessionDataSet dataSet = client.executeQueryStatement(sql);

            while (dataSet.hasNext()) {
                RowRecord record = dataSet.next();
                long timestamp = record.getTimestamp();

                List<Field> fields = record.getFields();

                Short afm = (short) fields.get(0).getIntV();
                Boolean afmFlag = fields.get(1).getBoolV();
                Boolean fmFlag = fields.get(2).getBoolV();
                Short fhr = (short) fields.get(3).getIntV();
                Byte fhrQuality = (byte) fields.get(4).getIntV();
                Short toco = (short) fields.get(5).getIntV();
                Boolean tocoFlag = fields.get(6).getBoolV();

                DataPoint dataPoint = new DataPoint();
                dataPoint.setAfm(afm);
                dataPoint.setAfmFlag(afmFlag);
                dataPoint.setFmFlag(fmFlag);
                dataPoint.setFhr(fhr);
                dataPoint.setFhrQuality(fhrQuality);
                dataPoint.setToco(toco);
                dataPoint.setTocoFlag(tocoFlag);
                dataPoint.setTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));

                result.add(dataPoint);
            }
        } catch (Exception e) {
            throw new RuntimeException("查询数据失败，UID: " + uid, e);
        }

        return result;
    }

    @Override
    public List<Short[]> queryForEval(
            int uid,
            @NonNull ZonedDateTime startTime,
            @NonNull ZonedDateTime endTime
    ) {
        List<Short[]> result = new ArrayList<>();
        String path = queryPrefix + uid;

        String sql = String.format(
                "SELECT fhr, toco FROM %s WHERE time >= %d AND time <= %d",
                path, startTime.toInstant().toEpochMilli(), endTime.toInstant().toEpochMilli()
        );

        try {
            SessionDataSet dataSet = client.executeQueryStatement(sql);

            while (dataSet.hasNext()) {
                RowRecord record = dataSet.next();

                List<Field> fields = record.getFields();

                result.add(new Short[]{(short) fields.get(0).getIntV(), (short) fields.get(1).getIntV()});
            }
        } catch (Exception e) {
            throw new RuntimeException("查询数据失败，UID: " + uid, e);
        }

        return result;
    }

    @Override
    public void write(
            int uid,
            @NonNull List<DataPoint> dataPoints
    ) {
        String userId = queryPrefix + uid;

        for (DataPoint dp : dataPoints) {
            long timestamp = dp.getTime().toInstant().toEpochMilli();

            List<String> measurements = Arrays.asList(
                    "afm",
                    "afmFlag",
                    "fmFlag",
                    "fhr",
                    "fhrQuality",
                    "toco",
                    "tocoFlag"
            );
            List<TSDataType> dataTypes = Arrays.asList(
                    TSDataType.INT32,
                    TSDataType.BOOLEAN,
                    TSDataType.BOOLEAN,
                    TSDataType.INT32,
                    TSDataType.INT32,
                    TSDataType.INT32,
                    TSDataType.BOOLEAN
            );

            List<Object> values = new ArrayList<>();
            values.add(dp.getAfm());
            values.add(dp.getAfmFlag());
            values.add(dp.getFmFlag());
            values.add(dp.getFhr());
            values.add(dp.getFhrQuality());
            values.add(dp.getToco());
            values.add(dp.getTocoFlag());

            try {
                client.insertRecord(userId, timestamp, measurements, dataTypes, values);
            } catch (IoTDBConnectionException | StatementExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
