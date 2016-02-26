package responses.data.PermissionData;

import utilities.TimeUtility;
import java.util.*;
import responses.data.PermissionData.*;
import responses.data.*;

/**
 * Created by Davis Treybig on 1/24/2016.
 */

public class PermissionMatrix{
	private List<UserAndID> users;
	private List<GroupAndID> groups;
	private List<ResourceAndID> resources;
	private SystemPermissions system_permissions;
	private ResourcePermissions resource_permissions;

	public PermissionMatrix(List<UserAndID> users, List<GroupAndID> groups, List<ResourceAndID> resources, 
			SystemPermissions system_permissions, ResourcePermissions resource_permissions){
		this.users = users;
		this.groups = groups;
		this.resources = resources;
		this.system_permissions = system_permissions;
		this.resource_permissions = resource_permissions;
	}

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

}