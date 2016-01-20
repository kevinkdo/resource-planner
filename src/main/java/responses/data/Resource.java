package responses.data;

import java.util.List;

/**
 * Created by jiaweizhang on 1/19/2016.
 */
public class Resource {
    private int resource_id;
    private String name;
    private String description;
    private List<String> tags;

    public Resource(int resource_id, String name, String description, List<String> tags) {
        this.resource_id = resource_id;
        this.name = name;
        this.description = description;
        this.tags = tags;
    }

    public int getResource_id() {
        return resource_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }
}
