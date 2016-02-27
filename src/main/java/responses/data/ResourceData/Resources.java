package responses.data.ResourceData;

import responses.data.ResourceData.Resource;

import java.util.List;

/**
 * Created by jiaweizhang on 1/27/2016.
 */
public class Resources {
    List<Resource> resources;

    public Resources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
