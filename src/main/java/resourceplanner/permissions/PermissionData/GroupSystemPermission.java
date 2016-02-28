package resourceplanner.permissions.PermissionData;

/**
 * Created by Davis Treybig
 */

public class GroupSystemPermission extends GenericSystemPermission {
    private int group_id;

    public GroupSystemPermission(boolean resource_p, boolean reservation_p, boolean user_p, int group_id){
        super(resource_p, reservation_p, user_p);
        this.group_id = group_id;
    }

    //DONT REMOVE. Default empty constructor needed for PUT request
    public GroupSystemPermission(){   
    }

    public int getGroup_id(){
    	return group_id;
    }

    public void setGroup_id(int group_id){
    	this.group_id = group_id;
    }
}