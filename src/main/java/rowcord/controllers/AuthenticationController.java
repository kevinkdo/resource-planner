package rowcord.controllers;

/**
 * Created by jiawe on 1/16/2016.
 */

import databases.JDBC;
import org.springframework.web.bind.annotation.*;
import requestdata.auth.LoginData;
import requestdata.auth.RegisterData;
import responses.StandardResponse;
import responses.subresponses.Token;
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
    public StandardResponse register(@RequestBody final RegisterData rd) {
        return registerDB(rd.getEmail(), rd.getPassword().toCharArray());
    }

    @RequestMapping(value = "/login",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse login(@RequestBody final LoginData rd) {
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
            st = c.prepareStatement("INSERT INTO accounts (email, passhash) VALUES (?, ?);");
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
            st = c.prepareStatement("SELECT passhash FROM accounts WHERE email = ?;");
        } catch (Exception e) {
            return new StandardResponse(true, "Failed during hashing in register");
        }
        try {
            st.setString(1, email);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String passhash = rs.getString("passhash");
                st.close();
                rs.close();
                if (PasswordHash.validatePassword(password, passhash)) {
                    String jwt = TokenCreator.generateToken(email);
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


