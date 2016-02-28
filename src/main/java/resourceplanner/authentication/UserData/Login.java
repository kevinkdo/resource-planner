package resourceplanner.authentication.UserData;

/**
 * Created by jiaweizhang on 1/18/2016.
 */
public class Login {
    private String token;
    private int userId;
    private String userName;

    public Login(String token, int userId, String userName) {
        this.token = token;
        this.userId = userId;
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }
}