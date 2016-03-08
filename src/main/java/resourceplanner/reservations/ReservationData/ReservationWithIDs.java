package resourceplanner.reservations.ReservationData;
import java.sql.Timestamp;

/**
 * Created by Davis Treybig on 1/24/2016.
 */
public class ReservationWithIDs {
    private String title;
    private String description;
    private int reservation_id;
    private int user_id;
    private int resource_id;
    private Timestamp begin_time;
    private Timestamp end_time;
    private boolean should_email;

    public ReservationWithIDs(String title, String description, int reservation_id, int user_id, int resource_id, Timestamp begin_time,
        Timestamp end_time, boolean should_email) {
        this.title = title;
        this.description = description;
        this.reservation_id = reservation_id;
        this.user_id = user_id;
        this.resource_id = resource_id;
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.should_email = should_email;
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

    public int getUser_id(){
        return user_id;
    }

    public int getResource_id(){
        return resource_id;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUser_id(int user_id){
        this.user_id = user_id;
    }

    public void setResource_id(int resource_id){
        this.resource_id = resource_id;
    }

    public void setBegin_time(Timestamp begin_time){
        this.begin_time = begin_time;
    }

    public void setEnd_time(Timestamp end_time){
        this.end_time = end_time;
    }

    public void setShould_email(boolean should_email){
        this.should_email = should_email;
    }
}
