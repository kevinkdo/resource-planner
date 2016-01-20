package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/19/2016.
 */

import databases.JDBC;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import requestdata.AuthRequest;
import requestdata.ResourceRequest;
import responses.StandardResponse;
import responses.data.Token;
import utilities.PasswordHash;
import utilities.TokenCreator;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createResource(@RequestBody final ResourceRequest data, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        String email = claims.get("email").toString();
        int userId = Integer.parseInt(claims.get("user_id").toString());
        System.out.println("userId: "+userId);
        if (userId != 1) {
            return new StandardResponse(true, "Not authorized", data);
        }

        return createResourceDB(data);
    }

    private StandardResponse createResourceDB(ResourceRequest data) {
        Connection c = JDBC.connect();
        try {
            c.setAutoCommit(false);
        } catch (SQLException e) {
            return new StandardResponse(true, "failed to disable autocommit");
        }
        PreparedStatement st = null;
        String resourcesInsert = "INSERT INTO resources (name, description) VALUES (?, ?);";
        String resourceTagsInsert = "INSERT INTO resourcetags (resource_id, tag) VALUES (?, ?);";
        int resourceId;
        try {
            st = c.prepareStatement(resourcesInsert, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, data.getName());
            st.setString(2, data.getDescription());
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                return new StandardResponse(true, "no new record created");
            }
        } catch (Exception e) {
            return new StandardResponse(true, "failed to add record");
        }
        try {
            ResultSet generatedKeys = st.getGeneratedKeys();
            if (generatedKeys.next()) {
                resourceId = generatedKeys.getInt(1);
            } else {
                return new StandardResponse(true, "Creating resource failed, no ID obtained.");
            }
        } catch (Exception e) {
            return new StandardResponse(true, "insert resource failed - no ID obtained.");
        }
        try {
            PreparedStatement ps = c.prepareStatement(resourceTagsInsert);
            for (String tag : data.getTags()) {
                ps.setInt(1, resourceId);
                ps.setString(2, tag);
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
            return new StandardResponse(false, "successful resource insert");
        } catch (Exception e) {
            return new StandardResponse(true, "insert resourcetags failed");
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

