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

    public String getName() {
        if (name.length() >= 250) {
            return name.substring(0, 250);
        }
        return name;
    }

    public String getDescription() {
        if (description.length() >= 2000) {
            return description.substring(0, 2000);
        }
        return description;
    }

    public List<String> getTags() {
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
}
