package xyz.ph59.med.tsdb.query;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractQueryBuilder {
    protected int uid;
    protected boolean forEval = false;
    protected ZonedDateTime start;
    protected ZonedDateTime end;

    protected static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    public AbstractQueryBuilder uid(int uid) {
        this.uid = uid;
        return this;
    }

    public AbstractQueryBuilder startTime(ZonedDateTime start) {
        this.start = start;
        return this;
    }

    public AbstractQueryBuilder endTime(ZonedDateTime end) {
        this.end = end;
        return this;
    }

    public AbstractQueryBuilder forEval() {
        this.forEval = true;
        return this;
    }

    public AbstractQueryBuilder forSimpleQuery() {
        this.forEval = false;
        return this;
    }

    public abstract String build();
}
