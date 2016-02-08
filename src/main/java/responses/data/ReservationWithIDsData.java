package responses.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by jiaweizhang on 2/2/16.
 */
public class ReservationWithIDsData {
    private int reservation_id;
    private int user_id;
    private int resource_id;
    private String begin_time;
    private String end_time;
    private boolean should_email;

    public ReservationWithIDsData(int reservation_id, int user_id, int resource_id, Timestamp begin_time,
                              Timestamp end_time, boolean should_email) {
        this.reservation_id = reservation_id;
        this.user_id = user_id;
        this.resource_id = resource_id;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String beg = df.format(begin_time);
        String end = df.format(end_time);

        //String beg = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(begin_time);
        //String end = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(end_time);
        this.begin_time = beg;
        this.end_time = end;
        this.should_email = should_email;
    }

    public ReservationWithIDsData(ReservationWithIDs reservationWithIDs){
        this.reservation_id = reservationWithIDs.getReservation_id();
        this.user_id = reservationWithIDs.getUser_id();
        this.resource_id = reservationWithIDs.getResource_id();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.begin_time = df.format(reservationWithIDs.getBegin_time());
        this.end_time = df.format(reservationWithIDs.getEnd_time());


        //this.begin_time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(reservationWithIDs.getBegin_time());
        //this.end_time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(reservationWithIDs.getEnd_time());
        this.should_email = reservationWithIDs.getShould_email();
    }

    public int getReservation_id() {
        return reservation_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getResource_id() {
        return resource_id;
    }

    public String getBegin_time() {
        return begin_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public boolean isShould_email() {
        return should_email;
    }
}
