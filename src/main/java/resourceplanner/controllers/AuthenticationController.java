package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/16/2016.
 */

import databases.JDBC;
import org.springframework.web.bind.annotation.*;
import requestdata.UserRequest;
import responses.StandardResponse;
import responses.data.Token;
import utilities.PasswordHash;
import utilities.TokenCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @RequestMapping(value = "/register",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse register(@RequestBody final UserRequest rd) {
        return registerDB(rd);
    }

    @RequestMapping(value = "/login",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse login(@RequestBody final UserRequest rd) {
        return loginDB(rd);
    }


    private StandardResponse registerDB(UserRequest req) {
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during hashing in register");
        }
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        try {
            st = c.prepareStatement("INSERT INTO users (email, passhash, username, should_email) VALUES (?, ?, ?, true);");
            st.setString(1, req.getEmail());
            st.setString(2, passwordHash);
            st.setString(3, "");
            st.executeUpdate();
            st.close();
            return new StandardResponse(false, "Successfully registered");
        } catch (Exception g) {
            return new StandardResponse(true, "Emails already exists");
        }
    }

    private StandardResponse loginDB(UserRequest req) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        try {
            st = c.prepareStatement("SELECT user_id, passhash FROM users WHERE email = ?;");
            st.setString(1, req.getEmail());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String passhash = rs.getString("passhash");
                st.close();
                rs.close();
                if (PasswordHash.validatePassword(req.getPassword(), passhash)) {
                    String jwt = TokenCreator.generateToken(userId, req.getEmail());
                    Token token = new Token(jwt);
                    return new StandardResponse(false, "Successfully logged in", null, token);
                } else {
                    return new StandardResponse(true, "Invalid username or password");
                }
            }
            return new StandardResponse(true, "Invalid username or password");
        } catch (Exception f) {
            return new StandardResponse(true, "Invalid username or password");
        }
    }
}


