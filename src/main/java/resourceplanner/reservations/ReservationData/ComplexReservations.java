package resourceplanner.reservations.ReservationData;

import java.util.List;

/**
 * Created by jiaweizhang on 3/15/16.
 */
public class ComplexReservations {
    private List<Reservation> reservations;

    public ComplexReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
