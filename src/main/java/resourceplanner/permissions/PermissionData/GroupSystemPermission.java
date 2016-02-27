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

    public int getGroup_id(){
    	return group_id;
    }
}