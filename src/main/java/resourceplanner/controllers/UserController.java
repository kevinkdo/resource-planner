package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

import databases.JDBC;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import requestdata.UserRequest;
import responses.StandardResponse;
import responses.data.User;
import utilities.PasswordHash;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;

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
        if (userId != 1) {
            return new StandardResponse(true, "Not authorized", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }

        return createUserDB(req);
    }

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getUserById(@PathVariable final int userId, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        String email = claims.get("email").toString();
        int adminUserId = Integer.parseInt(claims.get("user_id").toString());
        if (adminUserId != 1) {
            return new StandardResponse(true, "Not authorized");
        }
        return getUserByIdDB(userId);
    }


    private StandardResponse createUserDB(UserRequest req) {
        Connection c = JDBC.connect();
        try {
            c.setAutoCommit(false);
        } catch (SQLException e) {
            return new StandardResponse(true, "failed to disable autocommit", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during hashing in register", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        PreparedStatement st = null;
        String usersInsert = "INSERT INTO users (email, passhash, username, should_email) VALUES (?, ?, ?, ?);";
        try {
            st = c.prepareStatement(usersInsert, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, req.getEmail());
            st.setString(2, passwordHash);
            st.setString(3, req.getUsername());
            st.setBoolean(4, req.isShould_email());
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                return new StandardResponse(true, "no new user created", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
            }
            c.commit();
            return new StandardResponse(false, "user successfully created", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        } catch (Exception e) {
            return new StandardResponse(true, "failed to add user - duplicate", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
            // figure out how to determine if user already exists without actually querying
        }
    }

    private StandardResponse getUserByIdDB(int userId) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectUsersQuery = "SELECT email, username, should_email FROM users WHERE user_id = ?;";
        String email;
        String username;
        boolean should_email;
        try {
            st = c.prepareStatement(selectUsersQuery);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                email = rs.getString("email");
                username = rs.getString("username");
                should_email = rs.getBoolean("should_email");
            } else {
                return new StandardResponse(true, "No user found with given id");
            }
        } catch (Exception f) {
            return new StandardResponse(true, "No user found with given id 2");
        }

        return new StandardResponse(false, "success", null, new User(email, username, should_email));
    }
}

