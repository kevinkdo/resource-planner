package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/19/2016.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import requestdata.ResourceRequest;
import resourceplanner.services.ResourceService;
import responses.StandardResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/resources")
public class ResourceController extends Controller{

    @Autowired
    private ResourceService resourceService;

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createResource(@RequestBody final ResourceRequest req, final HttpServletRequest request) {
        if(!isAdmin(request)){
            return new StandardResponse(true, "Not authorized", req);
        }
        return resourceService.createRequest(req);
    }

    @RequestMapping(value = "/{resourceId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getResourceById(@PathVariable final int resourceId) {
        return resourceService.getResourceById(resourceId);
    }

    @RequestMapping(value = "/{resourceId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateResource(@PathVariable final int resourceId, @RequestBody final ResourceRequest req, final HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new StandardResponse(true, "Not authorized", req);
        }
        return resourceService.updateResource(req, resourceId);
    }
}

