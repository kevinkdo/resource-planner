package resourceplanner.reservations;

import java.sql.Timestamp;

/**
 * Created by Davis Treybig on 1/26/2016.
 */

public class QueryReservationRequest {

    private Integer[] resource_ids;
    private String[] required_tags;
    private String[] excluded_tags;
    private Timestamp start;
    private Timestamp end;

    public QueryReservationRequest(Integer[] resource_ids, String[] required_tags, String[] excluded_tags, Timestamp start, Timestamp end) {
        this.resource_ids = resource_ids;
        this.required_tags = required_tags;
        this.excluded_tags = excluded_tags;
        this.start = start;
        this.end = end;
    }

    public boolean isValid(){
        return (start != null && end != null && start.before(end));
    }

    public Integer[] getResource_ids() {
        if (resource_ids == null) {
            return new Integer[0];
        }
        return resource_ids;
    }

    public void setResource_ids(Integer[] resource_ids) {
        this.resource_ids = resource_ids;
    }

    public String[] getRequired_tags() {
        if (required_tags == null) {
            return new String[0];
        }
        return required_tags;
    }

    public void setRequired_tags(String[] required_tags) {
        this.required_tags = required_tags;
    }

    public String[] getExcluded_tags() {
        if (excluded_tags == null) {
            return new String[0];
        }
        return excluded_tags;
    }

    public void setExcluded_tags(String[] excluded_tags) {
        this.excluded_tags = excluded_tags;
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

}


