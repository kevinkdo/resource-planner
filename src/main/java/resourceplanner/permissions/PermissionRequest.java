package resourceplanner.permissions;
import resourceplanner.permissions.PermissionData.*;
import java.util.*;

/**
 * Created by jiaweizhang on 2/22/16.
 */
public class PermissionRequest {
	private List<UserAndID> users;
	private List<GroupAndID> groups;
	private List<ResourceAndID> resources;
	private SystemPermissions system_permissions;
	private ResourcePermissions resource_permissions;

	public List<UserAndID> getUsers(){
		return users;
	}

	public List<GroupAndID> getGroups(){
		return groups;
	}

	public List<ResourceAndID> getResources(){
		return resources;
	}

	public SystemPermissions getSystem_permissions(){
		return system_permissions;
	}

	public ResourcePermissions getResource_permissions(){
		return resource_permissions;
	}

	public boolean isValid(){
		return system_permissions != null && resource_permissions != null && 
			users != null && groups != null && resources != null;
	}
}
