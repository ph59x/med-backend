package xyz.ph59.med.entity;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class DataPoint {
    private Short afm;
    private Boolean afmFlag;
    private Boolean fmFlag;

    private Short fhr;
    private Byte fhrQuality;

    private Short toco;
    private Boolean tocoFlag;

    private ZonedDateTime time;

    public Point convertToPoint(String measurement) {
        Point p = new Point(measurement);

        if (afm != null) {
            p.addField("afm",  afm.longValue());
        }
        if (afmFlag != null) {
            p.addField("afmFlag", afmFlag);
        }
        if (fmFlag != null) {
            p.addField("fmFlag", fmFlag);
        }
        if (fhr != null) {
            p.addField("fhr",  fhr.longValue());
        }
        if (fhrQuality != null) {
            p.addField("fhrQuality",  fhrQuality.longValue());
        }
        if (toco != null) {
            p.addField("toco",  toco.longValue());
        }
        if (tocoFlag != null) {
            p.addField("tocoFlag", tocoFlag);
        }
        p.time(time.toInstant(), WritePrecision.S);
        
        return p;
    }
}
