package resourceplanner.authentication;

/**
 * Created by jiaweizhang on 2/27/16.
 */
public class AuthGroup {
    boolean resource_p;
    boolean reservation_p;
    boolean user_p;

    public boolean isResource_p() {
        return resource_p;
    }

    public void setResource_p(boolean resource_p) {
        this.resource_p = resource_p;
    }

    public boolean isReservation_p() {
        return reservation_p;
    }

    public void setReservation_p(boolean reservation_p) {
        this.reservation_p = reservation_p;
    }

    public boolean isUser_p() {
        return user_p;
    }

    public void setUser_p(boolean user_p) {
        this.user_p = user_p;
    }
}
