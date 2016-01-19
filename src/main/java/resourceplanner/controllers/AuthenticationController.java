package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/16/2016.
 */

import databases.JDBC;
import org.springframework.web.bind.annotation.*;
import requestdata.AuthRequest;
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
    public StandardResponse register(@RequestBody final AuthRequest rd) {
        return registerDB(rd.getEmail(), rd.getPassword().toCharArray());
    }

    @RequestMapping(value = "/login",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse login(@RequestBody final AuthRequest rd) {
        return loginDB(rd.getEmail(), rd.getPassword().toCharArray());
    }


    private StandardResponse registerDB(String email, char[] password) {
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(password);
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during hashing in register");
        }
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        try {
            st = c.prepareStatement("INSERT INTO users (email, passhash, should_email) VALUES (?, ?, true);");
            st.setString(1, email);
            st.setString(2, passwordHash);
            st.executeUpdate();
            st.close();
            return new StandardResponse(false, "Successfully registered");
        } catch (Exception g) {
            return new StandardResponse(true, "Emails already exists");
        }
    }

    public StandardResponse loginDB(String email, char[] password) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        try {
            st = c.prepareStatement("SELECT user_id, passhash FROM users WHERE email = ?;");
            st.setString(1, email);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String passhash = rs.getString("passhash");
                st.close();
                rs.close();
                if (PasswordHash.validatePassword(password, passhash)) {
                    String jwt = TokenCreator.generateToken(userId, email);
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


