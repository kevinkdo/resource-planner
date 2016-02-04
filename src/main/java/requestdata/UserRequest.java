package requestdata;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

public class UserRequest {
    private String email;
    private char[] password;
    private String username;
    private Boolean should_email;

    public boolean isValid() {
        boolean valid = email != null && password != null && username != null && should_email != null;
        if (!valid) {
            return false;
        }
        return !(email.length() >= 250 || username.length() >= 250);
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

    public Boolean isShould_email() {
        return should_email;
    }
}
