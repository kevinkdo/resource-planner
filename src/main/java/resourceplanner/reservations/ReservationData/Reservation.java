package resourceplanner.reservations.ReservationData;

import resourceplanner.authentication.UserData.User;
import resourceplanner.resources.ResourceData.Resource;
import utilities.TimeUtility;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Davis Treybig on 1/24/2016.
 */
public class Reservation {
    private String title;
    private String description;
    private int reservation_id;
    private User user;
    private List<Resource> resources;
    private String begin_time;
    private String end_time;
    private boolean should_email;
    private boolean complete;

    public Reservation(String title, String description, int reservation_id, User user, List<Resource> resources, Timestamp begin_time,
        Timestamp end_time, boolean should_email, boolean complete) {
        this.title = title;
        this.description = description;
        this.reservation_id = reservation_id;
        this.user = user;
        this.resources = resources;
        this.begin_time = TimeUtility.timestampToString(begin_time);
        this.end_time = TimeUtility.timestampToString(end_time);
        this.should_email = should_email;
        this.complete = complete;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getReservation_id(){
        return reservation_id;
    }

    public User getUser(){
        return user;
    }

    public List<Resource> getResources(){
        return resources;
    }

    public String getBegin_time() {
        return begin_time;
    }

    public String getEnd_time(){
        return end_time;
    }

    public boolean getShould_email(){
        return should_email;
    }

    public boolean getComplete() {
        return complete;
    }

}
