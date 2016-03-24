package resourceplanner.search.responsedata;

/**
 * Created by jiaweizhang on 3/24/16.
 */
public class SearchResource {
    private int resource_id;
    private String name;
    private String description;

    public int getResource_id() {
        return resource_id;
    }

    public void setResource_id(int resource_id) {
        this.resource_id = resource_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
