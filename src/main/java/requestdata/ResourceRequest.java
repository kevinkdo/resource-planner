package requestdata;

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

    public boolean isValid() {
        return name != null && description != null && tags != null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        tags = new ArrayList<String>(new LinkedHashSet<String>(tags));
        return tags;
    }
}
