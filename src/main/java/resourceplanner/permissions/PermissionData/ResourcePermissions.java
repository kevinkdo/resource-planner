package resourceplanner.permissions.PermissionData;
import java.util.*;

/**
 * Created by Davis Treybig
 */

public class ResourcePermissions {
    private List<UserResourcePermission> user_permissions;
    private List<GroupResourcePermission> group_permissions;

    public ResourcePermissions(List<UserResourcePermission> user_permissions,
    		List<GroupResourcePermission> group_permissions){
    	this.user_permissions = user_permissions;
    	this.group_permissions = group_permissions;
    }

    //DONT REMOVE. Default empty constructor needed for PUT request
    public ResourcePermissions(){   
    }

    public List<UserResourcePermission> getUser_permissions(){
    	return user_permissions;
    }

    public void setUser_permissions(List<UserResourcePermission> user_permissions){
       this.user_permissions = user_permissions;
    }

    public List<GroupResourcePermission> getGroup_permissions(){
    	return group_permissions;
    }

    public void setGroup_permissions(List<GroupResourcePermission> group_permissions){
        this.group_permissions = group_permissions;
    }
}