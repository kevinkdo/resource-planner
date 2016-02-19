package requestdata;

import java.util.List;

/**
 * Created by jiaweizhang on 2/12/16.
 */

public class GroupRequest {
    private String group_name;
    private List<Integer> user_ids;
    //private Boolean resource_p;
    //private Boolean reservation_p;
    //private Boolean user_p;

    public String getGroup_name() {
        return group_name;
    }

    public List<Integer> getUser_ids() {
        return user_ids;
    }

    /*
    public Boolean getResource_p() {
        return resource_p;
    }

    public Boolean getReservation_p() {
        return reservation_p;
    }

    public Boolean getUser_p() {
        return user_p;
    }
    */
}
