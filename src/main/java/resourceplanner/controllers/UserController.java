package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

import databases.JDBC;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import requestdata.ResourceRequest;
import requestdata.UserRequest;
import responses.StandardResponse;
import responses.data.Resource;
import utilities.PasswordHash;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createUser(@RequestBody final UserRequest req, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        String email = claims.get("email").toString();
        int userId = Integer.parseInt(claims.get("user_id").toString());
        System.out.println("userId: "+userId);
        if (userId != 1) {
            return new StandardResponse(true, "Not authorized", req);
        }

        return createUserDB(req);
    }


    private StandardResponse createUserDB(UserRequest req) {
        Connection c = JDBC.connect();
        try {
            c.setAutoCommit(false);
        } catch (SQLException e) {
            return new StandardResponse(true, "failed to disable autocommit", req);
        }
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during hashing in register");
        }
        PreparedStatement st = null;
        String resourcesInsert = "INSERT INTO users (email, passhash, username, should_email) VALUES (?, ?, ?, ?);";
        try {
            st = c.prepareStatement(resourcesInsert, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, req.getEmail());
            st.setString(2, passwordHash);
            st.setString(3, req.getUsername());
            st.setBoolean(4, req.isShould_email());
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                return new StandardResponse(true, "no new user created");
            }
            c.commit();
            return new StandardResponse(false, "user successfully created");
        } catch (Exception e) {
            return new StandardResponse(true, "failed to add user - duplicate");
            // figure out how to determine if user already exists without actually querying
        }
    }

}

