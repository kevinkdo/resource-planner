package resourceplanner.permissions.PermissionData;

/**
 * Created by Davis Treybig
 */

public class UserSystemPermission extends GenericSystemPermission {
    private int user_id;

    public UserSystemPermission(boolean resource_p, boolean reservation_p, boolean user_p, int user_id){
        super(resource_p, reservation_p, user_p);
        this.user_id = user_id;
    }

    //DONT REMOVE. Default empty constructor needed for PUT request
    public UserSystemPermission(){   
    }

    public int getUser_id(){
    	return user_id;
    }

    public void setUser_id(int user_id){
    	this.user_id = user_id;
    }

}