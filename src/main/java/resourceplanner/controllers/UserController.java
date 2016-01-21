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

    @RequestMapping(value = "/{userId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateUser(@PathVariable final int userId, @RequestBody final UserRequest req, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        String email = claims.get("email").toString();
        int tokenUserId = Integer.parseInt(claims.get("user_id").toString());
        System.out.println("userId: "+userId);
        if (tokenUserId != 1) {
            return new StandardResponse(true, "Not authorized", req);
        }
        return updateUserDB(userId, req);
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

    private StandardResponse updateUserDB(int userId, UserRequest req) {
        StandardResponse sr = getUserByIdDB(userId);
        if (sr.getIs_error()) {
            return new StandardResponse(true, "user either does not exist or is broken", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        User res = (User) sr.getData();
        if (res == null) {
            // not likely unnecessary
            return new StandardResponse(true, "user either does not exist or is broken", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        String email = req.getEmail();
        String username = req.getUsername();
        Boolean should_email = req.isShould_email();
        char[] password = req.getPassword();
        if (password == null) {
            return new StandardResponse(true,  "password required", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        if (email == null) {
            email = res.getEmail();
        } else {
            if (!email.equals(res.getEmail())) {
                // check that email doesn't exist
                if (emailExists(email)) {
                    return new StandardResponse(true,  "email exists already", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
                }
            }
        }
        if (username == null) {
            username = res.getUsername();
        } else {
            if (!username.equals(res.getUsername())){
                // check that username doesn't exist
                if (usernameExists(username)) {
                    return new StandardResponse(true,  "username exists already", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
                }
            }
        }
        if (should_email == null) {
            should_email = res.isShould_email();
        }

        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during hashing in register", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        Connection c = JDBC.connect();
        try {
            c.setAutoCommit(false);
        } catch (Exception e) {
            return new StandardResponse(true, "database error", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        PreparedStatement st = null;
        String updateResourcesQuery = "UPDATE users SET email = ?, username = ?, passhash = ?, should_email = ? WHERE user_id = ?;";

        try {
            st = c.prepareStatement(updateResourcesQuery);
            st.setString(1, email);
            st.setString(2, username);
            st.setString(3, passwordHash);
            st.setBoolean(4, should_email);
            st.setInt(5, userId);
            int modified = st.executeUpdate();
            c.commit();
            System.out.println("modified: " + modified);
            return new StandardResponse(false, "success update user", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        } catch (Exception f) {
            return new StandardResponse(true, "failed to update user", new User(req.getEmail(), req.getUsername(), req.isShould_email()));
        }
    }

    private boolean usernameExists(String username) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectUsersQuery = "SELECT should_email FROM users WHERE username = ?;";
        try {
            st = c.prepareStatement(selectUsersQuery);
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception f) {
            return false;
        }
    }

    private boolean emailExists(String email) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectUsersQuery = "SELECT should_email FROM users WHERE email = ?;";
        try {
            st = c.prepareStatement(selectUsersQuery);
            st.setString(1, email);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception f) {
            return false;
        }
    }
}

