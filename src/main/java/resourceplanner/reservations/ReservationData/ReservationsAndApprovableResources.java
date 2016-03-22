package resourceplanner.reservations.ReservationData;

import resourceplanner.resources.ResourceData.Resource;
import resourceplanner.authentication.UserData.User;
import utilities.TimeUtility;
import resourceplanner.reservations.ReservationData.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Davis Treybig on 1/24/2016.
 */
public class ReservationsAndApprovableResources {

    private List<Reservation> reservations;
    private List<ApprovableResources> approvableResources;

    public ReservationsAndApprovableResources(List<Reservation> reservations, List<ApprovableResources> approvableResources) {
        this.reservations = reservations;
        this.approvableResources = approvableResources;
    }

    public List<Reservation> getReservations(){
        return reservations;
    }

    public List<ApprovableResources> getApprovableResources(){
        return approvableResources;
    }

}
