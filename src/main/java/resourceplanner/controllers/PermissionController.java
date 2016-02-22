package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 2/22/16.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import requestdata.PermissionRequest;
import resourceplanner.services.PermissionService;
import responses.StandardResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController extends Controller{

    @Autowired
    private PermissionService permissionService;

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createPermissions(@RequestBody final PermissionRequest req, final HttpServletRequest request) {
        // TODO
        // maybe separate endpoints for users and groups
        return null;
    }

    @RequestMapping(value = "/user/{userId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getUserPermissions(@PathVariable final int userId, final HttpServletRequest request) {
        // TODO
        return null;
    }

    @RequestMapping(value = "/group/{userId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getGroupPermissions(@PathVariable final int userId, final HttpServletRequest request) {
        // TODO
        return null;
    }

    @RequestMapping(value = "",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updatePermissions(@RequestBody final PermissionRequest req, final HttpServletRequest request) {
        // TODO
        return null;
    }
}

