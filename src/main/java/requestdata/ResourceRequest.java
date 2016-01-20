package requestdata;

import java.util.List;

/**
 * Created by jiaweizhang on 1/19/2016.
 */
public class ResourceRequest {
    private String name;
    private String description;
    private List<String> tags;

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
