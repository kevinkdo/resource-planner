package resourceplanner.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.ResourceRequest;
import responses.StandardResponse;
import responses.data.Resource;
import responses.data.Resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by jiaweizhang on 1/27/2016.
 */

@Transactional
@Service
public class ResourceService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse createRequest(final ResourceRequest req) {
        if (!req.isValid()) {
            return new StandardResponse(true, "invalid json");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO resources (name, description) VALUES (?, ?);",
                                new String[] {"resource_id"});
                        ps.setString(1, req.getName());
                        ps.setString(2, req.getDescription());
                        return ps;
                    }
                },
                keyHolder);

        int resourceId = keyHolder.getKey().intValue();

        List<Object[]> batch = new ArrayList<Object[]>();
        for (String tag : req.getTags()) {
            Object[] values = new Object[] {
                    resourceId,
                    tag};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO resourcetags (resource_id, tag) VALUES (?, ?);",
                batch);

        return new StandardResponse(false, "successful resource insert", new Resource(resourceId, req.getName(),
                req.getDescription(), req.getTags()));
    }

    public StandardResponse getResourceById(final int resourceId) {
        List<Resource> resources = jt.query(
                "SELECT name, description FROM resources WHERE resource_id = ?;",
                new Object[] {resourceId},
                new RowMapper<Resource>() {
                    public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Resource resource = new Resource();
                        resource.setName(rs.getString("name"));
                        resource.setName(rs.getString("description"));
                        return resource;
                    }
                });

        if (resources.size() != 1) {
            return new StandardResponse(true, "resource does not exist");
        }

        Resource resource = resources.get(0);

        List<String> tags = jt.queryForList(
                "SELECT tag FROM resourcetags WHERE resource_id = ?;",
                new Object[] {resourceId},
                String.class);
        resource.setTags(tags);
        resource.setResource_id(resourceId);
        return new StandardResponse(false, "successfully retrieve resource", resource);
    }

    public StandardResponse getResource(String[] requiredTags, String[] excludedTags) {

        final String statement =
                "SELECT resources.name, resources.description, resources.resource_id, resourcetags.tag "+
                        "FROM resourcetags INNER JOIN resources "+
                        "ON resourcetags.resource_id = resources.resource_id "+
                        "ORDER BY resourcetags.resource_id ASC ;";

        List<RT> rts = jt.query(
                statement,
                new RowMapper<RT>() {
                    public RT mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RT rt = new RT();
                        rt.name = rs.getString("name");
                        rt.description = rs.getString("description");
                        rt.resourceId = rs.getInt("resource_id");
                        rt.tag = rs.getString("tag");
                        return rt;
                    }
                });

        Map<Integer, Resource> processList = new TreeMap<Integer, Resource>();
        Set<String> excluded = new HashSet<String>(Arrays.asList(excludedTags));
        Set<String> required = new HashSet<String>(Arrays.asList(requiredTags));

        for (int i=0; i<rts.size(); i++) {
            RT current = rts.get(i);
            if (processList.keySet().contains(current.resourceId)) {
                processList.get(current.resourceId).getTags().add(current.tag);
            } else {
                List<String> tagList = new ArrayList<String>();
                tagList.add(current.tag);
                Resource r = new Resource(current.resourceId, current.name, current.description, tagList);
                processList.put(current.resourceId, r);
            }
        }

        Map<Integer, Resource> keepMap = new TreeMap<Integer, Resource>();
        for (int i : processList.keySet()) {
            List<String> tags = processList.get(i).getTags();
            for (int j=0; j<tags.size(); j++) {
                String tag = tags.get(j);
                if (required.contains(tag)) {
                    keepMap.put(i, processList.get(i));
                    break;
                }
            }
        }

        Set<Integer> deleteSet = new HashSet<Integer>();
        for (int i : processList.keySet()) {
            List<String> tags = processList.get(i).getTags();
            for (int j=0; j<tags.size(); j++) {
                String tag = tags.get(j);
                if (excluded.contains(tag)) {
                    deleteSet.add(i);
                    break;
                }
            }
        }

        keepMap.keySet().removeAll(deleteSet);
        List<Resource> response = new ArrayList<Resource>();
        for (int i : keepMap.keySet()) {
            response.add(keepMap.get(i));
        }

        return new StandardResponse(false, "getResource", new Resources(response));
    }

    private static class RT {
        private String name;
        private String description;
        private int resourceId;
        private String tag;
    }

    public StandardResponse updateResource(ResourceRequest req, int resourceId) {
        if (!req.isValid()) {
            return new StandardResponse(true, "invalid json");
        }

        int resourceExists = jt.queryForObject(
                "SELECT COUNT(*) FROM resources WHERE resource_id = ?;", Integer.class, resourceId);
        if (resourceExists != 1) {
            return new StandardResponse(true, "Resource doesn't exist");
        }

        jt.update("UPDATE resources SET name = ?, description = ? WHERE resource_id = ?;",
                req.getName(),
                req.getDescription(),
                resourceId);

        jt.update("DELETE FROM resourcetags WHERE resource_id = ?;", resourceId);

        // TODO refactor tag insert
        List<Object[]> batch = new ArrayList<Object[]>();
        for (String tag : req.getTags()) {
            Object[] values = new Object[] {
                    resourceId,
                    tag};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO resourcetags (resource_id, tag) VALUES (?, ?);",
                batch);

        return new StandardResponse(false, "successful resource update", new Resource(resourceId, req.getName(),
                req.getDescription(), req.getTags()));
    }

    public StandardResponse deleteResource(int resourceId) {
        jt.update("DELETE FROM reservations WHERE resource_id = ?;", resourceId);
        jt.update("DELETE FROM resourcetags WHERE resource_id = ?;", resourceId);
        jt.update("DELETE FROM resources WHERE resource_id = ?;", resourceId);
        return new StandardResponse(false, "successfully deleted resource and all accompanying reservations");
    }
}
