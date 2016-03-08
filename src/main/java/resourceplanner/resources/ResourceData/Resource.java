package resourceplanner.resources.ResourceData;

import java.util.List;

/**
 * Created by jiaweizhang on 1/19/2016.
 */
public class Resource {
    private int resource_id;
    private String name;
    private String description;
    private List<String> tags;
    private boolean restricted;

    public Resource(int resource_id, String name, String description, List<String> tags, boolean restricted) {
        this.resource_id = resource_id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.restricted = restricted;
    }

    public Resource() {

    }

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }
}
