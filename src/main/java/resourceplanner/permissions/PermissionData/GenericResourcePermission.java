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

    //DONT REMOVE. Default empty constructor needed for PUT request
    public GenericResourcePermission(){   
    }

    public int getResource_id(){
    	return resource_id;
    }

    public void setResource_id(int resource_id){
        this.resource_id = resource_id;
    }

    public int getPermission_level(){
    	return permission_level;
    }

    public void setPermission_level(int permission_level){
        this.permission_level = permission_level;
    }
}