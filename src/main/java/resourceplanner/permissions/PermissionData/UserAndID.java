package resourceplanner.permissions.PermissionData;

/**
 * Created by Davis Treybig
 */

public class UserAndID{
    private int user_id;
    private String username;

    public UserAndID(int user_id, String username) {
        this.user_id = user_id;
        this.username = username;
    }

    //DONT REMOVE. Default empty constructor needed for PUT request
    public UserAndID(){   
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
