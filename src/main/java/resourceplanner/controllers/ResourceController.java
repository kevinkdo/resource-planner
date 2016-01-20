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
import responses.data.Resource;
import responses.data.Token;
import utilities.PasswordHash;
import utilities.TokenCreator;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createResource(@RequestBody final ResourceRequest req, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        String email = claims.get("email").toString();
        int userId = Integer.parseInt(claims.get("user_id").toString());
        System.out.println("userId: "+userId);
        if (userId != 1) {
            return new StandardResponse(true, "Not authorized", req);
        }

        return createResourceDB(req);
    }

    @RequestMapping(value = "/{resourceId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getResourceById(@PathVariable final int resourceId) {
        return getResourceByIdDB(resourceId);
    }

    private StandardResponse createResourceDB(ResourceRequest req) {
        Connection c = JDBC.connect();
        try {
            c.setAutoCommit(false);
        } catch (SQLException e) {
            return new StandardResponse(true, "failed to disable autocommit", req);
        }
        PreparedStatement st = null;
        String resourcesInsert = "INSERT INTO resources (name, description) VALUES (?, ?);";
        String resourceTagsInsert = "INSERT INTO resourcetags (resource_id, tag) VALUES (?, ?);";
        int resourceId;
        try {
            st = c.prepareStatement(resourcesInsert, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, req.getName());
            st.setString(2, req.getDescription());
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
            for (String tag : req.getTags()) {
                ps.setInt(1, resourceId);
                ps.setString(2, tag);
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
            return new StandardResponse(false, "successful resource insert", req);
        } catch (Exception e) {
            return new StandardResponse(true, "insert resourcetags failed");
        }
    }

    private StandardResponse getResourceByIdDB(int resourceId) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectResourcesQuery = "SELECT name, description FROM resources WHERE resource_id = ?";
        String selectResourceTagsQuery = "SELECT tag FROM resourcetags WHERE resource_id = ?";
        String name;
        String description;
        List<String> tags = new ArrayList<String>();
        try {
            st = c.prepareStatement(selectResourcesQuery);
            st.setInt(1, resourceId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
                description = rs.getString("description");
            } else {
                return new StandardResponse(true, "No resource found with given id");
            }
        } catch (Exception f) {
            return new StandardResponse(true, "No resource found with given id 2");
        }

        try {
            st = c.prepareStatement(selectResourceTagsQuery);
            st.setInt(1, resourceId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String tag = rs.getString("tag");
                tags.add(tag);
            }
        } catch (Exception f) {
            return new StandardResponse(true, "failed to fetch resource tags");
        }
        return new StandardResponse(false, "success", null, new Resource(resourceId, name, description, tags));
    }
}

