package resourceplanner.reservations.ReservationData;

import resourceplanner.resources.ResourceData.Resource;
import resourceplanner.authentication.UserData.User;
import utilities.TimeUtility;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Davis Treybig on 1/24/2016.
 */
public class ReservationApproval {
    private boolean approved;

    public Boolean getApproved(){
        return approved;
    }

    public void setApproved(boolean approved){
        this.approved = approved;
    }
}
