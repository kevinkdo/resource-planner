package resourceplanner.reservations;

import utilities.TimeUtility;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Davis Treybig on 1/24/2016.
 */

public class ReservationRequest {

    private String title;
    private String description;
    private Integer user_id;
    private List<Integer> resource_ids;
    private String begin_time;
    private String end_time;
    private Boolean should_email;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getUser_id(){
        return user_id;
    }

    public List<Integer> getResource_ids(){
        return resource_ids;
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

    public boolean isValid(){
        return title != null && description != null && user_id != null && resource_ids != null && begin_time != null && end_time != null
            && should_email != null;
    }

    public boolean isValidTimes() {
        return getBegin_time().before(getEnd_time());
    }

}



