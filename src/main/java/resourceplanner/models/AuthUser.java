package resourceplanner.models;

/**
 * Created by jiaweizhang on 1/27/2016.
 */

public class AuthUser {
    private int user_id;
    private String passhash;
    private int permission;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getPasshash() {
        return passhash;
    }

    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }
}