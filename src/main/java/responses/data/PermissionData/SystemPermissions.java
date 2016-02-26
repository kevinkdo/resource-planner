package responses.data.PermissionData;
import java.util.*;
import responses.data.PermissionData.*;
import responses.data.*;

/**
 * Created by Davis Treybig
 */

public class SystemPermissions {
    private List<UserSystemPermission> user_permissions;
    private List<GroupSystemPermission> group_permissions;

    public SystemPermissions(List<UserSystemPermission> user_permissions, List<GroupSystemPermission> group_permissions){
    	this.user_permissions = user_permissions;
    	this.group_permissions = group_permissions;
    }

    public List<UserSystemPermission> getUser_permissions(){
    	return user_permissions;
    }

    public List<GroupSystemPermission> getGroup_permissions(){
    	return group_permissions;
    }

    public void setUser_permission(List<UserSystemPermission> user_permissions){
    	this.user_permissions = user_permissions;
    }

    public void setGroup_permission(List<GroupSystemPermission> group_permissions){
    	this.group_permissions = group_permissions;
    }
}