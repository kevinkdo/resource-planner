package responses.data.UserData;

/**
 * Created by jiaweizhang on 2/3/16.
 */
public class UserUpdate {
    private boolean should_email;

    public UserUpdate(boolean should_email) {
        this.should_email = should_email;
    }

    public boolean isShould_email() {
        return should_email;
    }
}
