package xyz.ph59.med.tsdb.query;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class TsdbQueryBuilder {
    protected int uid;
    protected boolean forEval = false;
    protected ZonedDateTime start;
    protected ZonedDateTime end;

    protected static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    public TsdbQueryBuilder uid(int uid) {
        this.uid = uid;
        return this;
    }

    public TsdbQueryBuilder startTime(ZonedDateTime start) {
        this.start = start;
        return this;
    }

    public TsdbQueryBuilder endTime(ZonedDateTime end) {
        this.end = end;
        return this;
    }

    public TsdbQueryBuilder forEval() {
        this.forEval = true;
        return this;
    }

    public TsdbQueryBuilder forSimpleQuery() {
        this.forEval = false;
        return this;
    }

    public abstract Object build();
}
