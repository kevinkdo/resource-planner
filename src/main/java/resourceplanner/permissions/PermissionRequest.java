package resourceplanner.permissions;
import resourceplanner.permissions.PermissionData.*;

/**
 * Created by jiaweizhang on 2/22/16.
 */
public class PermissionRequest {
	private PermissionMatrix permission_matrix;

	public PermissionMatrix getPermission_matrix(){
		return permission_matrix;
	}

	public boolean isValid(){
		return permission_matrix != null;
	}
}
