package xyz.ph59.med.tsdb.query.builder;

import xyz.ph59.med.tsdb.query.TsdbQueryBuilder;

import java.time.ZonedDateTime;

public class InfluxQueryBuilder extends TsdbQueryBuilder {
    private String bucket;
    private String measurement;

    @Override
    public InfluxQueryBuilder uid(int uid) {
        return (InfluxQueryBuilder) super.uid(uid);
    }

    @Override
    public InfluxQueryBuilder startTime(ZonedDateTime start) {
        return (InfluxQueryBuilder) super.startTime(start);
    }

    @Override
    public InfluxQueryBuilder endTime(ZonedDateTime end) {
        return (InfluxQueryBuilder) super.endTime(end);
    }

    @Override
    public InfluxQueryBuilder forEval() {
        return (InfluxQueryBuilder) super.forEval();
    }

    @Override
    public InfluxQueryBuilder forSimpleQuery() {
        return (InfluxQueryBuilder) super.forSimpleQuery();
    }

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("from(bucket: \"%s\")\n", bucket));
        sb.append(String.format(
                "  |> range(start: %s, stop: %s)\n",
                start.format(formatter),
                end.format(formatter)
        ));
        sb.append(String.format(
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"%s\")\n",
                measurement
        ));
        sb.append(String.format(
                "  |> filter(fn: (r) => r[\"uid\"] == \"%d\")\n",
                uid
        ));
        if (forEval) {
            sb.append("  |> filter(fn: (r) => r[\"_field\"] == \"fhr\" or r[\"_field\"] == \"toco\")");
        }
        sb.append("  |> aggregateWindow(every: 1s, fn: last, createEmpty: false)\n");
        sb.append("  |> yield(name: \"last\")");
        return sb.toString();
    }

    public InfluxQueryBuilder bucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public InfluxQueryBuilder measurement(String measurement) {
        this.measurement = measurement;
        return this;
    }
}