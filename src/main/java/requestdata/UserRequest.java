package requestdata;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

public class UserRequest {
    private String email;
    private char[] password;
    private String username;
    //private Boolean resource_p;
    //private Boolean reservation_p;
    //private Boolean user_p;
    private Boolean should_email;

    public boolean isValid() {
        return email != null && password != null && username != null && should_email != null;
    }

    public boolean isUpdateValid() {
        return should_email != null;
    }

    public String getEmail() {
        return email;
    }

    public char[] getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    /*
    public Boolean getResource_p() {
        return resource_p;
    }

    public Boolean getReservation_p() {
        return reservation_p;
    }

    public Boolean getUser_p() {
        return user_p;
    }
    */

    public Boolean getShould_email() {
        return should_email;
    }
}
