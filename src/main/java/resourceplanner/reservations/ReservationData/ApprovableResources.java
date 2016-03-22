package resourceplanner.reservations.ReservationData;

import resourceplanner.resources.ResourceData.Resource;
import resourceplanner.authentication.UserData.User;
import utilities.TimeUtility;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Davis Treybig on 1/24/2016.
 */
public class ApprovableResources {

    private int reservation_id;
    private List<Resource> approvableResources;
    private List<Resource> approvedResources;

    public ApprovableResources(int reservation_id, List<Resource> approvableResources, List<Resource> approvedResources){
        this.reservation_id = reservation_id;
        this.approvableResources = approvableResources;
        this.approvedResources = approvedResources;
    }

    public int getReservation_id(){
        return reservation_id;
    }

    public List<Resource> getApprovable_resources(){
        return approvableResources;
    }

    public List<Resource> getApproved_resources(){
    	return approvedResources;
    }

    public void setReservation_id(int reservation_id){
    	this.reservation_id = reservation_id;
    }

    public void setApprovable_resources(List<Resource> approvableResources){
    	this.approvableResources = approvableResources;
    }

    public void setApproved_resources(List<Resource> approvedResources){
    	this.approvedResources = approvedResources;
    }

}
