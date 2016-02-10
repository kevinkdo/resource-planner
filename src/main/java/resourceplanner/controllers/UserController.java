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
        if (!isAdmin(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.createUser(req);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getUserById(@PathVariable final int userId, final HttpServletRequest request) {
        if (!isAdmin(request) && (userId != getRequesterID(request))) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.getUserById(userId);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateUser(@PathVariable final int userId, @RequestBody final UserRequest req, final HttpServletRequest request) {
        if (userId != getRequesterID(request) && !isAdmin(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.updateUser(req, userId);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public StandardResponse deleteUser(@PathVariable final int userId, final HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.deleteUser(userId);
    }

}

