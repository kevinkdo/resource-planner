package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import requestdata.UserRequest;
import resourceplanner.services.UserService;
import responses.StandardResponse;
import responses.data.User;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createUser(@RequestBody final UserRequest req, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        int userId = Integer.parseInt(claims.get("user_id").toString());
        if (userId != 1) {
            return new StandardResponse(true, "Not authorized", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        if (!req.isValid()) {
            return new StandardResponse(true, "invalid json", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        return userService.createUser(req);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getUserById(@PathVariable final int userId, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        int adminUserId = Integer.parseInt(claims.get("user_id").toString());
        if (adminUserId != 1) {
            return new StandardResponse(true, "Not authorized");
        }
        return userService.getUserById(userId);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateUser(@PathVariable final int userId, @RequestBody final UserRequest req, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        int tokenUserId = Integer.parseInt(claims.get("user_id").toString());
        if (tokenUserId != 1) {
            return new StandardResponse(true, "Not authorized", req);
        }
        /* allow null fields or no? */
        if (!req.isValid()) {
            return new StandardResponse(true, "invalid json", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        return userService.updateUser(req, userId);
    }
}

