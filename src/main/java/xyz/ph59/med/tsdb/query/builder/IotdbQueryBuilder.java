package xyz.ph59.med.tsdb.query.builder;

import xyz.ph59.med.tsdb.query.TsdbQueryBuilder;

import java.time.ZonedDateTime;

public class IotdbQueryBuilder extends TsdbQueryBuilder {
    private final String queryPrefix;

    public IotdbQueryBuilder(String queryPrefix) {
        this.queryPrefix = queryPrefix;
    }

    @Override
    public IotdbQueryBuilder uid(int uid) {
        return (IotdbQueryBuilder) super.uid(uid);
    }

    @Override
    public IotdbQueryBuilder startTime(ZonedDateTime start) {
        return (IotdbQueryBuilder) super.startTime(start);
    }

    @Override
    public IotdbQueryBuilder endTime(ZonedDateTime end) {
        return (IotdbQueryBuilder) super.endTime(end);
    }

    @Override
    public IotdbQueryBuilder forEval() {
        return (IotdbQueryBuilder) super.forEval();
    }

    @Override
    public IotdbQueryBuilder forSimpleQuery() {
        return (IotdbQueryBuilder) super.forSimpleQuery();
    }

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");

        if (forEval) {
            sb.append("fhr, toco");
        }
        else {
            sb.append("afm, afmFlag, fmFlag, fhr, fhrQuality, toco, tocoFlag");
        }

        sb.append(" FROM ");
        sb.append(queryPrefix);
        sb.append(uid);

        sb.append(" WHERE time >= ");
        sb.append(start.toInstant().toEpochMilli());
        sb.append(" AND time <= ");
        sb.append(end.toInstant().toEpochMilli());

        return sb.toString();
    }
}
