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

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getUserById(@PathVariable final int userId, final HttpServletRequest request) {
        if (!hasUserP(request) && (userId != getRequesterID(request))) {
            return new StandardResponse(true, "You are not authorized");
        }
        return userService.getUserById(userId);
    }
}

