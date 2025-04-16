package xyz.ph59.med.tsdb.query.builder;

import xyz.ph59.med.tsdb.query.AbstractQueryBuilder;

public class InfluxQueryBuilder extends AbstractQueryBuilder {
    private String bucket;
    private String measurement;

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