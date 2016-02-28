package resourceplanner.groups;

import java.util.List;

/**
 * Created by jiaweizhang on 2/12/16.
 */

public class GroupRequest {
    private String group_name;
    private List<Integer> user_ids;

    public boolean isValid() {
        return group_name != null && user_ids != null;
    }

    public String getGroup_name() {
        return group_name;
    }

    public List<Integer> getUser_ids() {
        return user_ids;
    }

}
