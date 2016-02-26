package responses.data.PermissionData;
import java.util.*;
import responses.data.PermissionData.*;
import responses.data.*;

/**
 * Created by Davis Treybig
 */

public class SystemPermissions {
    private List<UserSystemPermission> user_permission;
    private List<GroupSystemPermission> group_permission;

    public SystemPermissions(List<UserSystemPermission> user_permission, List<GroupSystemPermission> group_permission){
    	this.user_permission = user_permission;
    	this.group_permission = group_permission;
    }

    public List<UserSystemPermission> getUser_permission(){
    	return user_permission;
    }

    public List<GroupSystemPermission> getGroup_permission(){
    	return group_permission;
    }

    public void setUser_permission(List<UserSystemPermission> user_permission){
    	this.user_permission = user_permission;
    }

    public void setGroup_permission(List<GroupSystemPermission> group_permission){
    	this.group_permission = group_permission;
    }
}