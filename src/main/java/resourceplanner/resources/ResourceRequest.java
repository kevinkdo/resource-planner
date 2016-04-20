package resourceplanner.resources;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by jiaweizhang on 1/19/2016.
 */
public class ResourceRequest {
    private String name;
    private String description;
    private List<String> tags;
    private Boolean restricted;
    private Integer shared_count;
    private Integer parent_id;

    public boolean isValid() {
        return name != null && description != null && tags != null && restricted != null && shared_count != null && parent_id != null;
    }

    public boolean isValidPut() {
        return parent_id != null;
    }

    public String getName() {
        if(name == null){
            return null;
        }
        if (name.length() >= 250) {
            return name.substring(0, 250);
        }
        return name;
    }

    public String getDescription() {
        if(description == null){
            return null;
        }
        if (description.length() >= 2000) {
            return description.substring(0, 2000);
        }
        return description;
    }

    public List<String> getTags() {
        if(tags == null){
            return null;
        }
        tags = new ArrayList<String>(new LinkedHashSet<String>(tags));
        return tags;
    }

    public Boolean isRestricted() {
        return restricted;
    }

    public Integer getShared_count() {
        return shared_count;
    }

    public Integer getParent_id() {
        return parent_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setRestricted(Boolean restricted) {
        this.restricted = restricted;
    }

    public void setShared_count(Integer shared_count) {
        this.shared_count = shared_count;
    }

    public void setParent_id(Integer parent_id) {
        this.parent_id = parent_id;
    }
}
