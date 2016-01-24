package responses.data;
import java.sql.Timestamp;

/**
 * Created by Davis Treybig on 1/24/2016.
 */
public class Reservation {
    private int reservation_id;
    private User user;
    private Resource resource;
    private Timestamp begin_time;
    private Timestamp end_time;
    private boolean should_email;

    public Reservation(int reservation_id, User user, Resource resource, Timestamp begin_time,
        Timestamp end_time, boolean should_email) {
        this.reservation_id = reservation_id;
        this.user = user;
        this.resource = resource;
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.should_email = should_email;
    }


    public int getReservation_id(){
        return reservation_id;
    }

    public User getUser(){
        return user;
    }

    public Resource getResource(){
        return resource;
    }

    public Timestamp getBegin_time() {
        return begin_time;
    }

    public Timestamp getEnd_time(){
        return end_time;
    }

    public boolean getShould_email(){
        return should_email;
    }

}
