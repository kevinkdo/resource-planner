package resourceplanner.permissions.PermissionData;

/**
 * Created by Davis Treybig
 */

public class GroupResourcePermission extends GenericResourcePermission {
    private int group_id;

    public GroupResourcePermission(int resource_id, int permission_level, int group_id){
        super(resource_id, permission_level);
        this.group_id = group_id;
    }

    //DONT REMOVE. Default empty constructor needed for PUT request
    public GroupResourcePermission(){   
    }

    public int getGroup_id(){
    	return group_id;
    }

    public void setGroup_id(int group_id){
    	this.group_id = group_id;
    }
}