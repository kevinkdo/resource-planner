package responses.data;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by Davis Treybig on 1/24/2016.
 */
public class Reservation {
    private int reservation_id;
    private User user;
    private Resource resource;
    private String begin_time;
    private String end_time;
    private boolean should_email;

    public Reservation(int reservation_id, User user, Resource resource, Timestamp begin_time,
        Timestamp end_time, boolean should_email) {
        this.reservation_id = reservation_id;
        this.user = user;
        this.resource = resource;
        String beg = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(begin_time);
        String end = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(end_time);
        this.begin_time = beg;
        this.end_time = end;
        this.should_email = should_email;
    }

    public Reservation(ReservationWithIDs reservation, User user, Resource resource){
        this.reservation_id = reservation.getReservation_id();
        this.user = user;
        this.resource = resource;
        String beg = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(reservation.getBegin_time());
        String end = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(reservation.getEnd_time());
        this.begin_time = beg;
        this.end_time = end;
        this.should_email = reservation.getShould_email();
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

    public String getBegin_time() {
        return begin_time;
    }

    public String getEnd_time(){
        return end_time;
    }

    public boolean getShould_email(){
        return should_email;
    }

}
