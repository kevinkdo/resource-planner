package resourceplanner.resources;

/**
 * Created by jiaweizhang on 1/19/2016.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import resourceplanner.main.Controller;
import resourceplanner.main.StandardResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/resources")
public class ResourceController extends Controller {

    @Autowired
    private ResourceService resourceService;

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createResource(@RequestBody final ResourceRequest req, final HttpServletRequest request) {
        if(!hasResourceP(request)){
            return new StandardResponse(true, "You are not authorized.", req);
        }
        return resourceService.createRequest(req, getRequesterID(request));
    }

    @RequestMapping(value = "/{resourceId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getResourceById(@PathVariable final int resourceId,
                                            final HttpServletRequest request) {
        int userId = getRequesterID(request);
        return resourceService.getResourceById(resourceId, userId);
    }

    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getResource(
            @RequestParam(value = "required_tags", required = false) String[] requiredTags,
            @RequestParam(value = "excluded_tags", required = false) String[] excludedTags,
            final HttpServletRequest request) {
        int userId = getRequesterID(request);
        return resourceService.getResource(requiredTags, excludedTags, userId);
    }

    @RequestMapping(value = "",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getAllResources(final HttpServletRequest request) {
        int userId = getRequesterID(request);
        return resourceService.getResource(new String[0], new String[0], userId);
    }

    @RequestMapping(value = "/forest",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getResourceForest(final HttpServletRequest request) {
        int userId = getRequesterID(request);
        return resourceService.getResourceForest(userId);
    }


        @RequestMapping(value = "/{resourceId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateResource(@PathVariable final int resourceId, @RequestBody final ResourceRequest req, final HttpServletRequest request) {
        if (!hasResourceP(request)) {
            return new StandardResponse(true, "You are not authorized", req);
        }
        return resourceService.updateResource(req, resourceId);
    }

    @RequestMapping(value = "/{resourceId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public StandardResponse deleteResource(@PathVariable final int resourceId, final HttpServletRequest request) {
        if (!hasResourceP(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return resourceService.deleteResource(resourceId);
    }

    @RequestMapping(value = "/{resourceId}/candelete",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse canDeleteResource(@PathVariable final int resourceId, final HttpServletRequest request) {
        if (!hasResourceP(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return resourceService.canDeleteResource(resourceId);
    }

}

