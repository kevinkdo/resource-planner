package resourceplanner.models;

/**
 * Created by jiaweizhang on 1/27/2016.
 */

public class AuthUser {
    private int user_id;
    private String passhash;
    private boolean super_p;
    private boolean resource_p;
    private boolean reservation_p;
    private boolean user_p;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getPasshash() {
        return passhash;
    }

    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }

    public boolean isSuper_p() {
        return super_p;
    }

    public void setSuper_p(boolean super_p) {
        this.super_p = super_p;
    }

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