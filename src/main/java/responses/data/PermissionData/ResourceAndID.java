package responses.data.PermissionData;
import responses.data.PermissionData.*;
import responses.data.*;

/**
 * Created by Davis Treybig
 */

public class ResourceAndID {
    private int resource_id;
    private String resource_name;

    public ResourceAndID(int resource_id, String resource_name) {
        this.resource_id = resource_id;
        this.resource_name = resource_name;
    }

    public int getResource_id() {
        return resource_id;
    }

    public void setResource_id(int resource_id) {
        this.resource_id = resource_id;
    }


    public String getResource_name() {
        return resource_name;
    }

    public void setResource_name(String resource_name) {
        this.resource_name = resource_name;
    }
}
