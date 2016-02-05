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
}
