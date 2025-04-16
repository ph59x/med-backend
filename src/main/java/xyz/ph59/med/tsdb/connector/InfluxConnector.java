package xyz.ph59.med.tsdb.connector;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.exceptions.UnauthorizedException;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import xyz.ph59.med.entity.DataPoint;
import xyz.ph59.med.tsdb.TsdbConnector;
import xyz.ph59.med.tsdb.query.builder.InfluxQueryBuilder;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "tsdb.type", havingValue = "influxdb")
public class InfluxConnector implements TsdbConnector {
    private static final Logger log = LoggerFactory.getLogger(InfluxConnector.class);

    private final String MEASUREMENT = "user";

    private final InfluxDBClient influxDBClient;
    private final WriteApi writer;
    private final InfluxQueryBuilder queryBuilder;

    public InfluxConnector(
            Environment springEnv,
            ConfigurableApplicationContext context,
            @Value("${tsdb.influxdb.org}") String org,
            @Value("${tsdb.influxdb.bucket}") String bucket,
            @Value("${tsdb.influxdb.url}") String url
    ) {
        char[] token = null;
        try {
            token = springEnv.getProperty("tsdb.influxdb.token", String.class).toCharArray();
        } catch (NullPointerException e) {
            log.error("InfluxDB token不能为空");

            System.exit(SpringApplication.exit(context, () -> 1));
        }

        influxDBClient = InfluxDBClientFactory.create(url, token, org, bucket);

        log.info("尝试连接InfluxDB服务...");
        String version = null;
        try {
            version = influxDBClient.version();

            influxDBClient.getWriteApiBlocking().writeRecord(WritePrecision.US, "user fhr=0u,toco=0u abab");
        } catch (BadRequestException e) {
            log.info("连接成功，远程服务器版本：{}", version);
        } catch (UnauthorizedException e) {
            log.error("指定的token错误或权限不足");

            influxDBClient.close();
            System.exit(SpringApplication.exit(context, () -> 1));
        } catch (InfluxException e) {
            log.error("无法连接InfluxDB，请检查配置是否正确与网络和远程服务器可用性");

            influxDBClient.close();
            System.exit(SpringApplication.exit(context, () -> 1));
        } catch (RuntimeException e) {
            log.error("未知错误", e);
        }

        writer = influxDBClient.makeWriteApi();

        queryBuilder = new InfluxQueryBuilder().bucket(bucket).measurement(MEASUREMENT);
    }

    @Override
    public List<DataPoint> query(int uid,
                                 @NonNull ZonedDateTime startTime,
                                 @NonNull ZonedDateTime endTime) {
        QueryApi queryApi = influxDBClient.getQueryApi();

        String query = queryBuilder.uid(uid)
                .startTime(startTime)
                .endTime(endTime)
                .forSimpleQuery()
                .build();

        List<FluxTable> queryResult = queryApi.query(query);

        List<DataPoint> points = queryResult.stream()
                .flatMap(table -> table.getRecords().stream())
                .collect(Collectors.groupingBy(record -> record.getTime()))
                .values().stream()
                .map(records -> mapRecords(records))
                .collect(Collectors.toList());

        return points;
    }

    @Override
    public List<Short[]> queryForEval(int uid,
                                      @NonNull ZonedDateTime startTime,
                                      @NonNull ZonedDateTime endTime) {
        QueryApi queryApi = influxDBClient.getQueryApi();

        String query = queryBuilder.uid(uid)
                .startTime(startTime)
                .endTime(endTime)
                .forEval()
                .build();

        List<FluxTable> queryResult = queryApi.query(query);

        Map<Instant, Map<String, Short>> timeSeriesMap = new HashMap<>();
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                Instant time = (Instant) record.getValueByKey("_time");
                String field = record.getValueByKey("_field").toString();
                Number value = (Number) record.getValueByKey("_value");

                timeSeriesMap.computeIfAbsent(time, k -> new HashMap<>())
                        .put(field, value.shortValue());
            }
        }

        return timeSeriesMap.keySet().stream()
                .sorted()
                .map(time -> new Short[]{
                        timeSeriesMap.get(time).get("fhr"),
                        timeSeriesMap.get(time).get("toco")
                })
                .collect(Collectors.toList());
    }

    @Override
    public void write(int uid,
                      @NonNull List<DataPoint> dataPoints) {
        for (DataPoint data : dataPoints) {
            writer.writePoint(data.convertToPoint(MEASUREMENT).addTag("uid", Integer.toString(uid)));
        }
    }

    private DataPoint mapRecords(List<FluxRecord> records) {
        DataPoint dp = new DataPoint();
        records.forEach(record -> {
            String field = record.getField();
            Object value = record.getValue();

            switch (field) {
                case "afm":
                    dp.setAfm(((Number) value).shortValue());
                    break;
                case "afmFlag":
                    dp.setAfmFlag((Boolean) value);
                    break;
                case "fmFlag":
                    dp.setFmFlag((Boolean) value);
                    break;
                case "fhr":
                    dp.setFhr(((Number) value).shortValue());
                    break;
                case "fhrQuality":
                    dp.setFhrQuality(((Number) value).byteValue());
                    break;
                case "toco":
                    dp.setToco(((Number) value).shortValue());
                    break;
                case "tocoFlag":
                    dp.setTocoFlag((Boolean) value);
                    break;
            }
            // TODO 返回带时区的时间
            dp.setTime(ZonedDateTime.parse(record.getTime().toString()));
        });
        return dp;
    }
}
