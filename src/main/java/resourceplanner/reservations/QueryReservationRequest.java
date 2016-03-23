package resourceplanner.reservations;
import java.sql.Timestamp;

/**
 * Created by Davis Treybig on 1/26/2016.
 */

public class QueryReservationRequest {

    private Integer resource_id;
    private Timestamp start;
    private Timestamp end;


    public QueryReservationRequest(Integer resource_id, Timestamp start, Timestamp end) {
        this.resource_id = resource_id;
        this.start = start;
        this.end = end;
    }

    public Integer getResource_id() {
        return resource_id;
    }

    public void setResource_id(Integer resource_id) {
        this.resource_id = resource_id;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public boolean isValid(){
        return (start != null && end != null && start.before(end)) && resource_id != null;
    }

}


