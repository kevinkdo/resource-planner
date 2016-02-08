package requestdata;
import java.sql.Timestamp;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Created by Davis Treybig on 1/24/2016.
 */

public class ReservationRequest {
    
    private Integer user_id;
    private Integer resource_id;
    //private Timestamp begin_time;
    //private Timestamp end_time;
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH);
        if(begin_time == null){
            return null;
        }
        LocalDateTime ldt = LocalDateTime.parse(begin_time, formatter);
        if(ldt == null){
            return null;
        }
        Timestamp t = Timestamp.valueOf(ldt);
        return t;
    }

    public Timestamp getEnd_time(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH);
        if(end_time == null){
            return null;
        }
        LocalDateTime ldt = LocalDateTime.parse(end_time, formatter);
        if(ldt == null){
            return null;
        }
        Timestamp t = Timestamp.valueOf(ldt);
        return t;
    }

    public Boolean getShould_email(){
        return should_email;
    }

    public boolean isValidCreateRequest(){
        return user_id != null && resource_id != null && begin_time != null && end_time != null 
            && should_email != null && getBegin_time().before(getEnd_time());
    }


}



