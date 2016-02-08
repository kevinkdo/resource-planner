package requestdata;
import java.sql.Timestamp;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import utilities.TimeUtility;

/**
 * Created by Davis Treybig on 1/24/2016.
 */

public class ReservationRequest {
    
    private Integer user_id;
    private Integer resource_id;
    private String begin_time;
    private String end_time;
    private Boolean should_email;

    public Integer getUser_id(){
        return user_id;
    }

    public Integer getResource_id(){
        return resource_id;
    }

    public Timestamp getBegin_time() {
        if(begin_time == null){
            return null;
        }
        return TimeUtility.stringToTimestamp(begin_time);
    }

    public Timestamp getEnd_time(){
        if(end_time == null){
            return null;
        }
        return TimeUtility.stringToTimestamp(end_time);
    }

    public Boolean getShould_email(){
        return should_email;
    }

    public boolean isValidCreateRequest(){
        return user_id != null && resource_id != null && begin_time != null && end_time != null 
            && should_email != null && getBegin_time().before(getEnd_time());
    }


}



