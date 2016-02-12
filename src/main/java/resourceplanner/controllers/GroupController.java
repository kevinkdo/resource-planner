package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 2/12/2016.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import requestdata.GroupRequest;
import requestdata.ResourceRequest;
import resourceplanner.services.GroupService;
import resourceplanner.services.ResourceService;
import responses.StandardResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/groups")
public class GroupController extends Controller{

    @Autowired
    private GroupService groupService;

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createGroup(@RequestBody final GroupRequest req, final HttpServletRequest request) {
        if(!isAdmin(request)){
            return new StandardResponse(true, "You are not authorized.", req);
        }
        return groupService.createGroup(req);
    }

    @RequestMapping(value = "/{groupId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getGroupById(@PathVariable final int groupId) {
        return groupService.getGroupById(groupId);
    }

    @RequestMapping(value = "/{groupId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateGroup(@PathVariable final int groupId, @RequestBody final GroupRequest req, final HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new StandardResponse(true, "You are not authorized", req);
        }
        return groupService.updateGroup(req, groupId);
    }

    @RequestMapping(value = "/{groupId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public StandardResponse deleteGroup(@PathVariable final int groupId, final HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return groupService.deleteGroup(groupId);
    }

}

