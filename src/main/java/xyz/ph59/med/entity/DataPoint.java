package xyz.ph59.med.entity;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class DataPoint {
    private Short afm;
    private Boolean isAfm;
    private Boolean isFm;

    private Short fhr;
    private Byte fhrQuality;

    private Short toco;
    private Boolean isToco;

    private ZonedDateTime time;

    public Point convertToPoint(String measurement) {
        Point p = new Point(measurement);

        if (afm != null) {
            p.addField("afm",  afm.longValue());
        }
        if (isAfm != null) {
            p.addField("isAfm",  isAfm);
        }
        if (isFm != null) {
            p.addField("isFm",  isFm);
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
        if (isToco != null) {
            p.addField("isToco",  isToco);
        }
        p.time(time.toInstant(), WritePrecision.S);
        
        return p;
    }
}
