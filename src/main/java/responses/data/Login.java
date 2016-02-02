package responses.data;

/**
 * Created by jiaweizhang on 1/18/2016.
 */
public class Login {
    private String token;
    private int userId;

    public Login(String token, int userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }
}
