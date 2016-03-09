package resourceplanner.reservations;
import java.sql.Timestamp;

/**
 * Created by Davis Treybig on 1/26/2016.
 */

public class QueryReservationRequest {
    
    private Integer[] resource_ids;
    private Integer[] user_ids;
    private String[] required_tags;
    private String[] excluded_tags;
    private Timestamp start;
    private Timestamp end;

    public QueryReservationRequest(Integer[] resource_ids, Integer[] user_ids, String[] required_tags,
                                   String[] excluded_tags, Timestamp start, Timestamp end){
        this.resource_ids = resource_ids;
        this.user_ids = user_ids;
        this.required_tags = required_tags;
        this.excluded_tags = excluded_tags;
        this.start = start;
        this.end = end;
    }

    public Integer[] getResource_ids(){
        return resource_ids;
    }

    public Integer[] getUser_ids(){
        return user_ids;
    }

    public String[] getRequired_tags(){
        return required_tags;
    }

    public String[] getExcluded_tags(){
        return excluded_tags;
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


    public boolean matchOnIds(){
        return ((resource_ids !=null  && resource_ids.length > 0) || (user_ids != null && user_ids.length > 0));
    }

    public boolean matchOnExcludedTags(){
        return excluded_tags != null && excluded_tags.length > 0;
    }

    public boolean matchOnRequiredTags(){
        return required_tags != null && required_tags.length > 0;
    }

}


