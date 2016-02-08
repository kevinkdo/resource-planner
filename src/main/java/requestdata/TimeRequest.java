package requestdata;

import utilities.TimeUtility;
import java.sql.Timestamp;


/**
 * Created by Jiawei on 2/7/16.
 */

public class TimeRequest {
    private String time;
    private String time2;

    public Timestamp getTime() {
        return TimeUtility.stringToTimestamp(time);
    }

    public Timestamp getTime2() {
        return TimeUtility.stringToTimestamp(time2);
    }
}