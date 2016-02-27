package resourceplanner.permissions.PermissionData;

/**
 * Created by Davis Treybig
 */

public class GenericResourcePermission {
    private int resource_id;
    private int permission_level;

    public GenericResourcePermission(int resource_id, int permission_level){
        this.resource_id = resource_id;
        this.permission_level  = permission_level;
    }

    public int getResource_id(){
    	return resource_id;
    }

    public int getPermission_level(){
    	return permission_level;
    }
}