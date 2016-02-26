package responses.data.PermissionData;
import java.util.*;
import responses.data.PermissionData.*;
import responses.data.*;

/**
 * Created by Davis Treybig
 */

public class GroupResourcePermission extends GenericResourcePermission {
    private int group_id;

    public GroupResourcePermission(int resource_id, int permission_level, int group_id){
        super(resource_id, permission_level);
        this.group_id = group_id;
    }

    public int getGroup_id(){
    	return group_id;
    }
}