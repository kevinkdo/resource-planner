package responses.data;

import java.util.List;

/**
 * Created by root on 2/20/16.
 */
public class Group {
    private int group_id;
    private String name;
    private List<Integer> user_ids;

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getUser_ids() {
        return user_ids;
    }

    public void setUser_ids(List<Integer> user_ids) {
        this.user_ids = user_ids;
    }
}
