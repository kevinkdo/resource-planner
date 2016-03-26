package resourceplanner.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.main.EmailService;
import resourceplanner.main.StandardResponse;
import resourceplanner.permissions.PermissionService;
import resourceplanner.resources.ResourceData.CanDelete;
import resourceplanner.resources.ResourceData.Resource;
import resourceplanner.resources.ResourceData.Resources;
import utilities.TimeUtility;

import java.sql.*;
import java.util.*;

/**
 * Created by jiaweizhang on 1/27/2016.
 */

@Transactional
@Service
public class ResourceService {

    @Autowired
    EmailService emailService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse createRequest(final ResourceRequest req, int userId) {
        if (!req.isValid()) {
            return new StandardResponse(true, "Invalid request");
        }

        if (req.getName().length() < 1) {
            return new StandardResponse(true, "Resource name required");
        }

        final String tagRegex = "^[^,]*[^ ,][^,]*$";

        for (String tag : req.getTags()) {
            if (!tag.matches(tagRegex)) {
                return new StandardResponse(true, "Tag name " + tag + " is invalid");
            }
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO resources (name, description, restricted) VALUES (?, ?, ?);",
                                new String[]{"resource_id"});
                        ps.setString(1, req.getName());
                        ps.setString(2, req.getDescription());
                        ps.setBoolean(3, req.isRestricted());
                        return ps;
                    }
                },
                keyHolder);

        int resourceId = keyHolder.getKey().intValue();

        List<Object[]> batch = new ArrayList<Object[]>();
        for (String tag : req.getTags()) {
            Object[] values = new Object[]{
                    resourceId,
                    tag};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO resourcetags (resource_id, tag) VALUES (?, ?);",
                batch);

        addDefaultResourcePermissions(resourceId, userId);

        return new StandardResponse(false, "Successfully inserted resource", new Resource(resourceId, req.getName(),
                req.getDescription(), req.getTags(), req.isRestricted()));
    }

    private void addDefaultResourcePermissions(int resourceId, int userId){
        List<Integer> allUsers = jt.query(
                "SELECT user_id FROM users WHERE user_id != " + userId + ";",
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("user_id");
                    }
                });
        List<Integer> allGroups = jt.query(
                "SELECT group_id FROM groups;",
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("group_id");
                    }
                });

        List<Object[]> batchUserPermissions = new ArrayList<Object[]>();
        for(int i : allUsers){
            batchUserPermissions.add(new Object[]{i, resourceId, 0});
        }

        List<Object[]> batchGroupPermissions = new ArrayList<Object[]>();
        for(int i : allGroups){
            batchGroupPermissions.add(new Object[]{i, resourceId, 0});
        }

        jt.batchUpdate(
            "INSERT INTO userresourcepermissions (user_id, resource_id, permission_level) VALUES (?, ?, ?);",
            batchUserPermissions
            );

        jt.batchUpdate(
            "INSERT INTO groupresourcepermissions (group_id, resource_id, permission_level) VALUES (?, ?, ?);",
            batchGroupPermissions
            );

        jt.update(
            "INSERT INTO userresourcepermissions (user_id, resource_id, permission_level) VALUES (" + userId +
                ", " + resourceId + ", 1);"
            );
    }

    public StandardResponse getResourceById(final int resourceId, int userId) {
        List<Resource> resources = jt.query(
                "SELECT name, description, restricted FROM resources WHERE resource_id = ?;",
                new Object[]{resourceId},
                new RowMapper<Resource>() {
                    public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Resource resource = new Resource();
                        resource.setName(rs.getString("name"));
                        resource.setDescription(rs.getString("description"));
                        resource.setRestricted(rs.getBoolean("restricted"));
                        return resource;
                    }
                });

        if (resources.size() != 1) {
            return new StandardResponse(true, "Resource does not exist");
        }

        Resource resource = resources.get(0);

        List<String> tags = jt.queryForList(
                "SELECT tag FROM resourcetags WHERE resource_id = ?;",
                new Object[]{resourceId},
                String.class);
        resource.setTags(tags);
        resource.setResource_id(resourceId);


        Set<Integer> allViewableResources = getViewableResources(userId);
        if(allViewableResources.contains(resourceId) || userId == 1){
            return new StandardResponse(false, "Successfully retrieved resource", resource);
        }
        else{
            return new StandardResponse(true, "You do not have permission to view this resource");
        }
    }

    public StandardResponse getResourceByIdIgnoringPermissions(final int resourceId) {
        List<Resource> resources = jt.query(
                "SELECT name, description, restricted FROM resources WHERE resource_id = ?;",
                new Object[]{resourceId},
                new RowMapper<Resource>() {
                    public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Resource resource = new Resource();
                        resource.setName(rs.getString("name"));
                        resource.setDescription(rs.getString("description"));
                        resource.setRestricted(rs.getBoolean("restricted"));
                        return resource;
                    }
                });

        if (resources.size() != 1) {
            return new StandardResponse(true, "Resource does not exist");
        }

        Resource resource = resources.get(0);

        List<String> tags = jt.queryForList(
                "SELECT tag FROM resourcetags WHERE resource_id = ?;",
                new Object[]{resourceId},
                String.class);
        resource.setTags(tags);
        resource.setResource_id(resourceId);

        return new StandardResponse(false, "Successfully retrieved resource", resource);
    }



    public Set<Integer> getViewableResources(int userId){
        List<Integer> userViewableResources = permissionService.getUserViewableResources(userId);
        List<Integer> groupViewableResources = permissionService.getGroupViewableResources(userId);
        Set<Integer> allViewableResources = new TreeSet<Integer>(userViewableResources); 
        allViewableResources.addAll(groupViewableResources);
        return allViewableResources;
    }

    public StandardResponse getResource(String[] requiredTags, String[] excludedTags, int userId) {
        if (requiredTags == null) {
            requiredTags = new String[0];
        }
        if (excludedTags == null) {
            excludedTags = new String[0];
        }

        List<RT> rts = getResourcesWithTags();
        List<RT> noTagsrts = getResourcesWithoutTags();

        rts.addAll(noTagsrts);

        Map<Integer, Resource> processList = new HashMap<Integer, Resource>();
        Set<String> excluded = new HashSet<String>(Arrays.asList(excludedTags));
        Set<String> required = new HashSet<String>(Arrays.asList(requiredTags));

        for (RT current : rts) {
            if (processList.keySet().contains(current.resourceId)) {
                processList.get(current.resourceId).getTags().add(current.tag);
            } else {
                List<String> tagList = new ArrayList<String>();
                if (current.tag != null) {
                    tagList.add(current.tag);
                }
                Resource r = new Resource(current.resourceId, current.name, current.description, tagList, current.restricted);
                processList.put(current.resourceId, r);
            }
        }

        Set<Integer> deleteSet = new HashSet<Integer>();

        if (requiredTags.length != 0) {
            for (int i : processList.keySet()) {
                List<String> tags = processList.get(i).getTags();
                if (tags.size() == 0) {
                    deleteSet.add(i);
                    continue;
                }
                for (String s : required) {
                    boolean doBreak = false;
                    for (int j = 0; j < tags.size(); j++) {
                        if (!tags.contains(s)) {
                            deleteSet.add(i);
                            doBreak = true;
                            break;
                        }
                    }
                    if (doBreak) {
                        break;
                    }
                }
            }
        }

        for (int i : processList.keySet()) {
            List<String> tags = processList.get(i).getTags();
            for (String tag : tags) {
                if (excluded.contains(tag)) {
                    deleteSet.add(i);
                    break;
                }
            }
        }

        processList.keySet().removeAll(deleteSet);
        List<Resource> response = new ArrayList<Resource>();
        for (int i : processList.keySet()) {
            response.add(processList.get(i));
        }

        if(userId != 1){
            Set<Integer> allViewableResources = getViewableResources(userId);
            List<Resource> finalResponse = new ArrayList<Resource>();
            for(Resource r : response){
                if(allViewableResources.contains(r.getResource_id())){
                    finalResponse.add(r);
                }
            }
            return new StandardResponse(false, "Successfully retrieved resources", new Resources(finalResponse));
        }
        else{
            return new StandardResponse(false, "Successfully retrieved resources", new Resources(response));
        }
    }

    private static class RT {
        private String name;
        private String description;
        private int resourceId;
        private String tag;
        private boolean restricted;
    }

    private List<RT> getResourcesWithTags() {
        final String statement =
                "SELECT resources.name, resources.description, resources.restricted, resources.resource_id, resourcetags.tag " +
                        "FROM resourcetags INNER JOIN resources " +
                        "ON resourcetags.resource_id = resources.resource_id " +
                        "ORDER BY resourcetags.resource_id ASC ;";

        List<RT> rts = jt.query(
                statement,
                new RowMapper<RT>() {
                    public RT mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RT rt = new RT();
                        rt.name = rs.getString("name");
                        rt.description = rs.getString("description");
                        rt.restricted = rs.getBoolean("restricted");
                        rt.resourceId = rs.getInt("resource_id");
                        rt.tag = rs.getString("tag");
                        return rt;
                    }
                });
        return rts;
    }

    private List<RT> getResourcesWithoutTags() {
        final String noTagsStatement =
                "SELECT name, description, restricted, resource_id " +
                        "FROM resources " +
                        "WHERE NOT EXISTS (SELECT 1 FROM resourcetags WHERE resourcetags.resource_id = resources.resource_id) " +
                        "ORDER BY resource_id ASC ;";

        List<RT> noTagsrts = jt.query(
                noTagsStatement,
                new RowMapper<RT>() {
                    public RT mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RT rt = new RT();
                        rt.name = rs.getString("name");
                        rt.description = rs.getString("description");
                        rt.restricted = rs.getBoolean("restricted");
                        rt.resourceId = rs.getInt("resource_id");
                        rt.tag = null;
                        return rt;
                    }
                });
        return noTagsrts;
    }

    private RT getSpecificResource(int resourceId){
        List<RT> resource = jt.query("SELECT * from resources WHERE resource_id = " + resourceId + ";",
            new RowMapper<RT>() {
                    public RT mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RT rt = new RT();
                        rt.name = rs.getString("name");
                        rt.description = rs.getString("description");
                        rt.restricted = rs.getBoolean("restricted");
                        rt.resourceId = rs.getInt("resource_id");
                        rt.tag = null;
                        return rt;
                    }
                }
            );
        return resource.get(0);
    }

    private void updateNewUnrestrictedResource(int resourceId){
        jt.update("UPDATE reservationresources SET resource_approved = true WHERE resource_id = ?;",
                  resourceId);

        //find all reservations where not exists a resource that is not approved, set them approved
        jt.update("UPDATE reservations SET complete = true WHERE NOT EXISTS (SELECT * from reservationresources WHERE reservationresources.reservation_id = reservations.reservation_id AND resource_approved = false);");
    }

    public StandardResponse updateResource(ResourceRequest req, int resourceId) {
        if (!req.isValid()) {
            return new StandardResponse(true, "Invalid request");
        }

        int resourceExists = jt.queryForObject(
                "SELECT COUNT(*) FROM resources WHERE resource_id = ?;", Integer.class, resourceId);
        if (resourceExists != 1) {
            return new StandardResponse(true, "Resource does not exist");
        }

        RT oldResource = getSpecificResource(resourceId);
        if(oldResource.restricted == true && !req.isRestricted()){
            updateNewUnrestrictedResource(resourceId);
        }

        jt.update("UPDATE resources SET name = ?, description = ?, restricted = ? WHERE resource_id = ?;",
                req.getName(),
                req.getDescription(),
                req.isRestricted(),
                resourceId);

        jt.update("DELETE FROM resourcetags WHERE resource_id = ?;", resourceId);

        List<Object[]> batch = new ArrayList<Object[]>();
        for (String tag : req.getTags()) {
            Object[] values = new Object[]{
                    resourceId,
                    tag};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO resourcetags (resource_id, tag) VALUES (?, ?);",
                batch);

        return new StandardResponse(false, "Successfully updated resource", new Resource(resourceId, req.getName(),
                req.getDescription(), req.getTags(), req.isRestricted()));
    }

    public StandardResponse deleteResource(int resourceId) {
        int resourceExists = jt.queryForObject(
                "SELECT COUNT(*) FROM resources WHERE resource_id = ?;", Integer.class, resourceId);
        if (resourceExists != 1) {
            return new StandardResponse(true, "Resource does not exist");
        }

        // get list of reservations that depend on resource
        List<Integer> reservationIds = jt.queryForList(
                    "SELECT reservation_id FROM reservationresources WHERE resource_id = ?",
                    new Object[]{resourceId},
                    Integer.class
        );
        for (int reservationId : reservationIds) {
            emailService.removeScheduledEmails(reservationId);
        }

        jt.update("DELETE FROM reservationresources WHERE resource_id = ?;", resourceId);
        jt.update("DELETE FROM resourcetags WHERE resource_id = ?;", resourceId);
        jt.update("DELETE FROM resources WHERE resource_id = ?;", resourceId);
        return new StandardResponse(false, "successfully deleted resource and all accompanying reservations");
    }

    public StandardResponse canDeleteResource(int resourceId) {
        int resourceNum = jt.queryForObject("SELECT COUNT(*) FROM resources WHERE resource_id = ?;", new Object[]{resourceId}, Integer.class);
        if (resourceNum == 0) {
            return new StandardResponse(true, "Resource does not exist");
        }

        Timestamp currentTime = TimeUtility.currentUTCTimestamp();

        int reservations = jt.queryForObject("SELECT COUNT(*) FROM reservationresources, reservations WHERE reservationresources.resource_id = ? AND ? < reservations.end_time;",
                new Object[]{resourceId, currentTime}, Integer.class);

        boolean canDelete = reservations == 0;

        return new StandardResponse(false, "Successful retrieved canDelete status", new CanDelete(canDelete));
    }
}
