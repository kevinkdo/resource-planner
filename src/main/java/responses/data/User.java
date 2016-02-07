package responses.data;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

public class User {
    private int user_id;
    private String email;
    private String username;
    private boolean should_email;

    public User(int user_id, String email, String username, boolean should_email) {
        this.user_id =user_id;
        this.email = email;
        this.username = username;
        this.should_email = should_email;
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
}
