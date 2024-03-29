package resourceplanner.groups.GroupData;

import java.util.List;

/**
 * Created by jiaweizhang on 2/20/16.
 */
public class Group {
    private int group_id;
    private String group_name;
    private List<Integer> user_ids;

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public List<Integer> getUser_ids() {
        return user_ids;
    }

    public void setUser_ids(List<Integer> user_ids) {
        this.user_ids = user_ids;
    }
}
