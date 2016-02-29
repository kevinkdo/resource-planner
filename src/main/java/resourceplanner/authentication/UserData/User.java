package resourceplanner.authentication.UserData;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

public class User {
    private int user_id;
    private String email;
    private String username;
    private boolean should_email;
    private boolean resource_p;
    private boolean reservation_p;
    private boolean user_p;

    public User(int user_id, String email, String username, boolean should_email, boolean resource_p, boolean reservation_p, boolean user_p) {
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.should_email = should_email;
        this.resource_p = resource_p;
        this.reservation_p = reservation_p;
        this.user_p = user_p;
    }

    public User() {
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isShould_email() {
        return should_email;
    }

    public void setShould_email(boolean should_email) {
        this.should_email = should_email;
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
