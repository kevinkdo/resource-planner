package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import requestdata.UserRequest;
import resourceplanner.services.UserService;
import responses.StandardResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController extends Controller{

    @Autowired
    private UserService userService;

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createUser(@RequestBody final UserRequest req, final HttpServletRequest request) {
        if (!hasUserP(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.createUser(req);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getUserById(@PathVariable final int userId, final HttpServletRequest request) {
        if (!hasUserP(request) && (userId != getRequesterID(request))) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.getUserById(userId);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateUser(@PathVariable final int userId, @RequestBody final UserRequest req, final HttpServletRequest request) {
        if (userId != getRequesterID(request) && !hasUserP(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.updateUser(req, userId);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public StandardResponse deleteUser(@PathVariable final int userId, final HttpServletRequest request) {
        if (!hasUserP(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.deleteUser(userId);
    }

    @RequestMapping(value = "/{userId}/editablePermissions",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse deleteUser(@PathVariable final int userId, final HttpServletRequest request) {
        //TODO
        return new StandResponse(false, "does endpoint work?");
    }

    @RequestMapping(value = "/{userId}/editablePermissions",
            method = RequestMethod.PUT)
    @ResponseBody
    public StandardResponse deleteUser(@PathVariable final int userId, final HttpServletRequest request) {
        //TODO
        return new StandResponse(false, "does endpoint work??");
    }
}

