package responses.data.PermissionData;
import java.util.*;
import responses.data.PermissionData.*;
import responses.data.*;

/**
 * Created by Davis Treybig
 */

public class UserSystemPermission extends GenericSystemPermission {
    private int user_id;

    public UserSystemPermission(boolean resource_p, boolean reservation_p, boolean user_p, int user_id){
        super(resource_p, reservation_p, user_p);
        this.user_id = user_id;
    }

    public int getUser_id(){
    	return user_id;
    }

}