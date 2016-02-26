package responses.data.PermissionData;
import responses.data.PermissionData.*;
import responses.data.*;
import java.util.*;

/**
 * Created by Davis Treybig
 */

public class ResourcePermissions {
    private List<UserResourcePermission> user_permission;
    private List<GroupResourcePermission> group_permission;

    public ResourcePermissions(List<UserResourcePermission> user_permission,
    		List<GroupResourcePermission> group_permission){
    	this.user_permission = user_permission;
    	this.group_permission = group_permission;
    }

    public List<UserResourcePermission> getUser_permission(){
    	return user_permission;
    }

    public List<GroupResourcePermission> getGroup_permission(){
    	return group_permission;
    }
}