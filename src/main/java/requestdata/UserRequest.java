package requestdata;

/**
 * Created by jiaweizhang on 1/20/2016.
 */
public class UserRequest {
    private String email;
    private char[] password;
    private String username;
    private boolean should_email;

    public String getEmail() {
        return email;
    }

    public char[] getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isShould_email() {
        return should_email;
    }
}
