package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/16/2016.
 */

import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;
import requestdata.UserRequest;
import responses.StandardResponse;
import responses.data.Token;
import utilities.PasswordHash;
import utilities.TokenCreator;

import java.sql.ResultSet;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private JdbcTemplate jt;

    public void setDataSource(DataSource dataSource) {
        jt = new JdbcTemplate(dataSource);
    }

    @RequestMapping(value = "/register",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse register(@RequestBody final UserRequest req) {
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during password hashing.");
        }
        int emailExists = jt.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?;", Integer.class, req.getEmail());
        if (emailExists != 0) {
            return new StandardResponse(true, "Email already exists");
        }
        int usernameExists = jt.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?;", Integer.class, req.getUsername());
        if (usernameExists != 0) {
            return new StandardResponse(true, "Username already exists");
        }
        int returnedValue = jt.update(
                "INSERT INTO users (email, username, passhash, should_email) VALUES (?, ?, ?, true);",
                req.getEmail(), req.getUsername(), passwordHash);
        return new StandardResponse(false, "Successfully registered.");
    }

    @RequestMapping(value = "/login",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse login(@RequestBody final UserRequest req) {
        List<User> users = jt.query(
                "SELECT user_id, passhash, should_email FROM users WHERE email = ? OR username = ?;",
                new Object[]{req.getEmail(), req.getUsername()},
                new RowMapper<User>() {
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        User user = new User(rs.getInt("user_id"), rs.getString("passhash"));
                        return user;
                    }
                });
        if (users.size() != 1) {
            return new StandardResponse(true, "Email and username do not exist");
        }
        try {
            if (PasswordHash.validatePassword(req.getPassword(), users.get(0).passhash)) {
                Token token = new Token(TokenCreator.generateToken(users.get(0).user_id));
                return new StandardResponse(false, "Successfully authenticated.", null, token);
            }
        } catch (Exception e) {
            return new StandardResponse(true, "Failed to validate password.");
        }
        return new StandardResponse(true, "Failed to validate password.");
    }

    private final static class User {
        private int user_id;
        private String passhash;

        public User (int user_id, String passhash) {
            this.user_id = user_id;
            this.passhash = passhash;
        }
    }
}


