package requestdata;
import java.sql.Timestamp;

/**
 * Created by Davis Treybig on 1/26/2016.
 */

public class GetAllMatchingReservationRequest {
    
    private Integer[] resource_ids;
    private Integer[] user_ids;
    private Timestamp start;
    private Timestamp end;

    public GetAllMatchingReservationRequest(Integer[] resource_ids, Integer[] user_ids, Timestamp start, Timestamp end){
        this.resource_ids = resource_ids;
        this.user_ids = user_ids;
        this.start = start;
        this.end = end;
    }

    public Integer[] getResource_ids(){
        return resource_ids;
    }

    public Integer[] getUser_ids(){
        return user_ids;
    }

    public Timestamp getStart(){
        return start;
    }

    public Timestamp getEnd(){
        return end;
    }

    //The id_lists may be null if the query does not specify them, and that is fine. 
    //Only the timestamps need to be verified. 
    public boolean isValid(){
        return (start != null && end != null && start.before(end));
    }

    //Returns true if either of the id lists are null, indicating that
    //the query needs to check for matching on ID's
    public boolean matchOnIds(){
        return (resource_ids !=null || user_ids != null);
    }

}


