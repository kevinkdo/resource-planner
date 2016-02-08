package utilities;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Created by jiaweizhang on 2/7/16.
 */
public class TimeUtility {

    public static Timestamp currentUTCTimestamp() {
        LocalDateTime ldt = LocalDateTime.now();
        ZoneId zoneId = ZoneId.of("UTC");
        return Timestamp.from(ldt.atZone(zoneId).toInstant());
    }

    public static Timestamp stringToTimestamp(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
        LocalDateTime ldt = LocalDateTime.parse(str, formatter);
        ZoneId zoneId = ZoneId.of("UTC");
        return Timestamp.from(ldt.atZone(zoneId).toInstant());
    }

    public static String timestampToString(Timestamp ts) {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormatGmt.format(ts);
    }

}
