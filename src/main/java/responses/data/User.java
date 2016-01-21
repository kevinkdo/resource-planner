package responses.data;

/**
 * Created by jiaweizhang on 1/20/2016.
 */
public class User {
    private String email;
    private String username;
    private boolean should_email;

    public User(String email, String username, boolean should_email) {
        this.email = email;
        this.username = username;
        this.should_email = should_email;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public boolean isShould_email() {
        return should_email;
    }
}
