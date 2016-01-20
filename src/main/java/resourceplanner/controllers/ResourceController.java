package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/19/2016.
 */

import databases.JDBC;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import requestdata.ResourceRequest;
import responses.StandardResponse;
import responses.data.Resource;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @RequestMapping(value = "/{resourceId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateResource(@PathVariable final int resourceId, @RequestBody final ResourceRequest req, final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        String email = claims.get("email").toString();
        int userId = Integer.parseInt(claims.get("user_id").toString());
        System.out.println("userId: "+userId);
        if (userId != 1) {
            return new StandardResponse(true, "Not authorized", req);
        }
        return updateResourceDB(resourceId, req);
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

    private StandardResponse updateResourceDB(int resourceId, ResourceRequest req) {
        StandardResponse sr = getResourceById(resourceId);
        if (sr.getIs_error()) {
            return new StandardResponse(true, "resource either does not exist or is broken", req);
        }
        Resource res = (Resource) sr.getData();
        if (res == null) {
            // not likely unnecessary
            return new StandardResponse(true, "resource either does not exist or is broken", req);
        }
        String name = req.getName();
        String description = req.getDescription();
        List<String> tags = req.getTags();
        Set<String> tagsSet = new HashSet<String>(tags);
        Set<String> existingTagsSet = new HashSet<String>(res.getTags());
        if (res.getName().equals(name) && res.getDescription().equals(description) && tagsSet.equals(existingTagsSet)) {
            return new StandardResponse(false, "no change was requested", req); // maybe this should be valid TODO
        }
        if (name == null) {
            name = res.getName();
        }
        if (description == null) {
            description = res.getDescription();
        }
        if (tags == null) {
            tags = res.getTags();
        }
        Connection c = JDBC.connect();
        try {
            c.setAutoCommit(false);
        } catch (Exception e) {
            return new StandardResponse(true, "database error", req);
        }
        PreparedStatement st = null;
        String updateResourcesQuery = "UPDATE resources SET name = ?, description = ? WHERE resource_id = ?;";
        // check if either name or description are different
        if (!res.getName().equals(name) || !res.getDescription().equals(description)) {
            try {
                st = c.prepareStatement(updateResourcesQuery);
                st.setString(1, name);
                st.setString(2, description);
                st.setInt(3, resourceId);
                int modified = st.executeUpdate();
                System.out.println("modified: " + modified);
            } catch (Exception f) {
                return new StandardResponse(true, "failed to update resource", req);
            }
        }

        String deleteResourceTagsQuery = "DELETE FROM resourcetags WHERE resource_id = ?";
        String insertResourceTagsQuery = "INSERT INTO resourcetags (resource_id, tag) VALUES (?, ?);";
        if (!tagsSet.equals(existingTagsSet)) {
            // if tags not equal update tags
            try {
                st = c.prepareStatement(deleteResourceTagsQuery);
                st.setInt(1, resourceId);
                int modifiedRows = st.executeUpdate();
                System.out.println("tags deleted: " + modifiedRows);
            } catch (Exception f) {
                return new StandardResponse(true, "failed to delete tags", req);
            }
            try {
                PreparedStatement ps = c.prepareStatement(insertResourceTagsQuery);
                for (String tag : tags) {
                    ps.setInt(1, resourceId);
                    ps.setString(2, tag);
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
                return new StandardResponse(false, "successful resource and resourcetag update", req);
            } catch (Exception e) {
                return new StandardResponse(true, "insert resourcetags failed in update", req);
            }
        }
        try {
            c.commit();
        } catch (Exception e) {
            return new StandardResponse(true, "failed to commit", req);
        }
        return new StandardResponse(false, "successful resource update", req);
    }
}

