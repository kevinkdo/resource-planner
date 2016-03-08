package resourceplanner.reservations.ReservationData;

import utilities.TimeUtility;

import java.sql.Timestamp;

/**
 * Created by jiaweizhang on 2/2/16.
 */
public class ReservationWithIDsData {
    private String title;
    private String description;
    private int reservation_id;
    private int user_id;
    private int resource_id;
    private String begin_time;
    private String end_time;
    private boolean should_email;

    public ReservationWithIDsData(String title, String description, int reservation_id, int user_id, int resource_id, Timestamp begin_time,
                              Timestamp end_time, boolean should_email) {
        this.title = title;
        this.description = description;
        this.reservation_id = reservation_id;
        this.user_id = user_id;
        this.resource_id = resource_id;
        this.begin_time = TimeUtility.timestampToString(begin_time);
        this.end_time = TimeUtility.timestampToString(end_time);
        this.should_email = should_email;
    }

    public ReservationWithIDsData(ReservationWithIDs reservationWithIDs){
        this.title = reservationWithIDs.getTitle();
        this.description = reservationWithIDs.getDescription();
        this.reservation_id = reservationWithIDs.getReservation_id();
        this.user_id = reservationWithIDs.getUser_id();
        this.resource_id = reservationWithIDs.getResource_id();
        this.begin_time = TimeUtility.timestampToString(reservationWithIDs.getBegin_time());
        this.end_time = TimeUtility.timestampToString(reservationWithIDs.getEnd_time());
        this.should_email = reservationWithIDs.getShould_email();
    }

    public int getReservation_id() {
        return reservation_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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
