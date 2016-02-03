package requestdata;
import java.sql.Timestamp;

/**
 * Created by Davis Treybig on 1/24/2016.
 */

public class ReservationRequest {
    
    private Integer user_id;
    private Integer resource_id;
    private Timestamp begin_time;
    private Timestamp end_time;
    private Boolean should_email;



    public Integer getUser_id(){
        return user_id;
    }

    public Integer getResource_id(){
        return resource_id;
    }

    public Timestamp getBegin_time() {
        return begin_time;
    }

    public Timestamp getEnd_time(){
        return end_time;
    }

    public Boolean getShould_email(){
        return should_email;
    }

    public boolean isValidCreateRequest(){
        return user_id != null && resource_id != null && begin_time != null && end_time != null 
            && should_email != null && begin_time.before(end_time);
    }


}



