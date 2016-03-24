package resourceplanner.permissions;

/**
 * Created by jiaweizhang on 2/22/16.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import resourceplanner.main.Controller;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController extends Controller {

    @Autowired
    private PermissionService permissionService;

    /*
    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createPermissions(@RequestBody final PermissionRequest req, final HttpServletRequest request) {
        // TODO
        // maybe separate endpoints for users and groups
        return null;
    }
    */

    /*
    @RequestMapping(value = "/user/{userId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getUserPermissions(@PathVariable final int userId, final HttpServletRequest request) {
        // TODO
        return null;
    }
    */
    /*
    @RequestMapping(value = "/user/{userId}/editablePermissions",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getPermissionMatrix(@PathVariable final int userId, final HttpServletRequest request) {
        return permissionService.getPermissionMatrix(userId, )
    }

    @RequestMapping(value = "/user/{userId}/editablePermissions",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updatePermissionMatrix(@RequestBody final PermissionRequest req, final HttpServletRequest request) {
        // TODO
        return permissionService.updatePermissionMatrix(req, userId, )
    }*/
}

