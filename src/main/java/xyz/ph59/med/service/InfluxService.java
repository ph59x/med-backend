package xyz.ph59.med.service;

import com.influxdb.client.*;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.exceptions.UnauthorizedException;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import xyz.ph59.med.entity.DataPoint;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InfluxService {
    private static final Logger log = LoggerFactory.getLogger(InfluxService.class);
    private final Environment springEnv;
    private final ConfigurableApplicationContext context;

    @Value("${influxdb.org}")
    private String org;
    @Value("${influxdb.bucket}")
    private String bucket;
    @Value("${influxdb.url}")
    private String url;
    private final String MEASUREMENT = "user";

    private char[] token;
    private InfluxDBClient influxDBClient;
    private WriteApi writer;

    public InfluxService(Environment springEnv, ConfigurableApplicationContext context) {
        this.springEnv = springEnv;
        this.context = context;
    }

    @PostConstruct
    private void init() {
        try {
            token = springEnv.getProperty("influxdb.token", String.class).toCharArray();
        }
        catch (NullPointerException e) {
            log.error("InfluxDB token不能为空");
        }

        influxDBClient = InfluxDBClientFactory.create(
                url,
                token,
                org,
                bucket
        );

        log.info("尝试连接InfluxDB服务...");
        try  {
            String version = influxDBClient.version();

            try {
                influxDBClient.getWriteApiBlocking().writeRecord(WritePrecision.US, "user fhr=0u,toco=0u abab");
            } catch (BadRequestException e) {
                log.info("连接成功，远程服务器版本：{}", version);
            } catch (RuntimeException e) {
                throw e;
            }
        } catch (UnauthorizedException e) {
            log.error("指定的token错误或权限不足");
            destroyWhenError();
        } catch (InfluxException e) {
            log.error("无法连接InfluxDB，请检查配置是否正确与网络和远程服务器可用性");
            destroyWhenError();
        } catch (RuntimeException e) {
            log.error("未知错误", e);
        }

        writer = influxDBClient.makeWriteApi();
    }

    @PreDestroy
    private void destroy() {
        influxDBClient.close();
    }

    private void destroyWhenError() {
        this.destroy();
        System.exit(SpringApplication.exit(context, () -> 1));
    }

    public void write(List<DataPoint> datas, int uid) {
        for (DataPoint data : datas) {
            writer.writePoint(data.convertToPoint(MEASUREMENT).addTag("uid", Integer.toString(uid)));
        }
    }

    public List<DataPoint> query(int uid, ZonedDateTime start, ZonedDateTime end) {
        QueryApi queryApi = influxDBClient.getQueryApi();

        String query = "from(bucket: \"demodata\")\n" +
                "  |> range(start: " + start +", stop: " + end + ")\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"" + MEASUREMENT +"\")\n" +
                "  |> filter(fn: (r) => r[\"uid\"] == \"1\")\n" +
                "  |> aggregateWindow(every: 1s, fn: last, createEmpty: false)\n" +
                "  |> yield(name: \"last\")";

        List<FluxTable> query1 = queryApi.query(query);

        List<DataPoint> points = query1.stream()
                .flatMap(table -> table.getRecords().stream())
                .collect(Collectors.groupingBy(record -> record.getTime()))
                .values().stream()
                .map(records -> mapRecords(records))
                .collect(Collectors.toList());

        return points;
    }

    private DataPoint mapRecords(List<FluxRecord> records) {
        DataPoint dp = new DataPoint();
        records.forEach(record  -> {
            String field = record.getField();
            Object value = record.getValue();

            switch (field) {
                case "afm":
                    dp.setAfm(((Number)value).shortValue());
                    break;
                case "afmFlag":
                    dp.setAfmFlag((Boolean)value);
                    break;
                case "fmFlag":
                    dp.setFmFlag((Boolean)value);
                    break;
                case "fhr":
                    dp.setFhr(((Number)value).shortValue());
                    break;
                case "fhrQuality":
                    dp.setFhrQuality(((Number)value).byteValue());
                    break;
                case "toco":
                    dp.setToco(((Number)value).shortValue());
                    break;
                case "tocoFlag":
                    dp.setTocoFlag((Boolean)value);
                    break;
            }
            // TODO 返回带时区的时间
            dp.setTime(ZonedDateTime.parse(record.getTime().toString()));
        });
        return dp;
    }

}
