package resourceplanner.authentication.UserData;

/**
 * Created by jiaweizhang on 1/18/2016.
 */
public class Login {
    private String token;
    private int userId;
    private String username;

    public Login(String token, int userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}