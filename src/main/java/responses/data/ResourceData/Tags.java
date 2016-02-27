package responses.data.ResourceData;

import java.util.List;

/**
 * Created by jiaweizhang on 1/20/2016.
 */
public class Tags {
    private List<String> tags;

    public Tags (List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }
}
