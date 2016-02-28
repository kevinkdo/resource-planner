package resourceplanner.permissions.PermissionData;

/**
 * Created by Davis Treybig
 */

public class UserResourcePermission extends GenericResourcePermission {
    private int user_id;

    public UserResourcePermission(int resource_id, int permission_level, int user_id){
        super(resource_id, permission_level);
        this.user_id = user_id;
    }

    //DONT REMOVE. Default empty constructor needed for PUT request
    public UserResourcePermission(){   
    }

    public int getUser_id(){
    	return user_id;
    }

    public void setUser_id(int user_id){
    	this.user_id = user_id;
    }
}