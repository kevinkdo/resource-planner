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

        if (req.getParent_id() != 0) {
            int resourceExists = jt.queryForObject(
                    "SELECT COUNT(*) FROM resources WHERE resource_id = ?;", Integer.class, req.getParent_id());
            if (resourceExists != 1) {
                return new StandardResponse(true, "Parent resource does not exist");
            }
        }

        if(req.getShared_count() < 0){
            return new StandardResponse(true, "Shared count must be greater than or equal to 0");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO resources (name, description, restricted, shared_count, parent_id) VALUES (?, ?, ?, ?, ?);",
                                new String[]{"resource_id"});
                        ps.setString(1, req.getName());
                        ps.setString(2, req.getDescription());
                        ps.setBoolean(3, req.isRestricted());
                        ps.setInt(4, req.getShared_count());
                        ps.setInt(5, req.getParent_id());
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

        return new StandardResponse(false, "Successfully inserted resource");
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

    public int getSharedCount(int resourceId) {
        List<Integer> sharedCount = jt.query("SELECT shared_count FROM resources WHERE resource_id = " + resourceId + ";",
            new RowMapper<Integer>(){
                public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getInt("shared_count");
                }
            }
        );
        return sharedCount.get(0);
    }

    public Resource getResourceByIdHelper(final int resourceId, int userId) {
        List<Resource> resources = jt.query(
                "SELECT name, description, restricted, shared_count, parent_id FROM resources WHERE resource_id = ?;",
                new Object[]{resourceId},
                new RowMapper<Resource>() {
                    public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Resource resource = new Resource();
                        resource.setName(rs.getString("name"));
                        resource.setDescription(rs.getString("description"));
                        resource.setRestricted(rs.getBoolean("restricted"));
                        resource.setParent_id(rs.getInt("parent_id"));
                        resource.setShared_count(getSharedCount(resourceId));
                        return resource;
                    }
                });

        Resource resource = resources.get(0);

        List<String> tags = jt.queryForList(
                "SELECT tag FROM resourcetags WHERE resource_id = ?;",
                new Object[]{resourceId},
                String.class);
        resource.setTags(tags);
        resource.setResource_id(resourceId);

        // get all childrens' resourceIds
        List<Integer> childrenIds = jt.queryForList(
                "SELECT resource_id FROM resources WHERE parent_id = ?;",
                new Object[]{resourceId},
                Integer.class);

        List<Resource> children = new ArrayList<Resource>();
        for (int childId : childrenIds) {
            Resource child = getResourceByIdHelper(childId, userId);
            children.add(child);
        }
        resource.setChildren(children);

        Set<Integer> allViewableResources = getViewableResources(userId);
        Set<Integer> allReservableResources = getReservableResources(userId);
        boolean resource_p = hasSpecificPermission(userId, "resource_p");
        if(allViewableResources.contains(resourceId) || userId == 1 || resource_p){
            resource.setCan_view(true);
        } else {
            resource.setCan_view(false);
            resource.setName("Mystery");
            resource.setDescription("Mystery");
            resource.setResource_id(0);
            resource.setRestricted(false);
            resource.setShared_count(0);
            resource.setTags(new ArrayList<String>());
        }
        if (allReservableResources.contains(resourceId) || userId == 1 || resource_p) {
            resource.setCan_reserve(true);
        }
        else{
            resource.setCan_reserve(false);
        }
        return resource;
    }

    public StandardResponse getResourceById(final int resourceId, int userId) {

        int resourceExists = jt.queryForObject(
                "SELECT COUNT(*) FROM resources WHERE resource_id = ?;", Integer.class, resourceId);
        if (resourceExists != 1) {
            return new StandardResponse(true, "Resource does not exist");
        }

        Resource r = getResourceByIdHelper(resourceId, userId);
        return new StandardResponse(false, "Successfully fetched resource", r);
    }

    public StandardResponse getResourceByIdIgnoringPermissions(final int resourceId) {
        int resourceExists = jt.queryForObject(
                "SELECT COUNT(*) FROM resources WHERE resource_id = ?;", Integer.class, resourceId);
        if (resourceExists != 1) {
            return new StandardResponse(true, "Resource does not exist");
        }

        Resource r = getResourceByIdHelper(resourceId, 1);
        if (r == null) {
            return new StandardResponse(true, "You don't have view permissions for all children of the resource");
        }
        return new StandardResponse(false, "Successfully fetched resource", r);
    }



    public Set<Integer> getViewableResources(int userId){
        List<Integer> userViewableResources = permissionService.getUserViewableResources(userId);
        List<Integer> groupViewableResources = permissionService.getGroupViewableResources(userId);
        Set<Integer> allViewableResources = new TreeSet<Integer>(userViewableResources); 
        allViewableResources.addAll(groupViewableResources);
        return allViewableResources;
    }

    public Set<Integer> getReservableResources(int userId) {
        List<Integer> userReservable = permissionService.getUserReservableResources(userId);
        List<Integer> groupReservable = permissionService.getGroupReservableResources(userId);
        Set<Integer> allReservableResources = new HashSet<Integer>(userReservable);
        allReservableResources.addAll(groupReservable);
        return allReservableResources;
    }

    public StandardResponse getResource(String[] requiredTags, String[] excludedTags, int userId, boolean resourceP) {
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
            int sharedCount = getSharedCount(current.resourceId);
            if (processList.keySet().contains(current.resourceId)) {
                processList.get(current.resourceId).getTags().add(current.tag);
            } else {
                List<String> tagList = new ArrayList<String>();
                if (current.tag != null) {
                    tagList.add(current.tag);
                }
                Resource r = new Resource();
                r.setResource_id(current.resourceId);
                r.setName(current.name);
                r.setDescription(current.description);
                r.setTags(tagList);
                r.setRestricted(current.restricted);
                r.setShared_count(sharedCount);
                r.setParent_id(current.parentId);
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

        if(userId != 1 && !resourceP){
            Set<Integer> allViewableResources = getViewableResources(userId);
            Set<Integer> allReservableResources = getReservableResources(userId);
            List<Resource> finalResponse = new ArrayList<Resource>();
            for(Resource r : response){
                if (allReservableResources.contains(r.getResource_id())) {
                    r.setCan_reserve(true);
                } else {
                    r.setCan_reserve(false);
                }
                if (allViewableResources.contains(r.getResource_id())){
                    r.setCan_view(true);
                    finalResponse.add(r);
                } else {
                    r.setCan_view(false);
                }
            }
            return new StandardResponse(false, "Successfully retrieved resources", new Resources(finalResponse));
        }
        else{
            for (Resource r : response) {
                r.setCan_view(true);
                r.setCan_reserve(true);
            }
            return new StandardResponse(false, "Successfully retrieved resources", new Resources(response));
        }
    }

    private static class RT {
        private String name;
        private String description;
        private int resourceId;
        private String tag;
        private boolean restricted;
        private int sharedCount;
        private int parentId;
    }

    public StandardResponse getResourceForest(int userId) {
        // get all parents
        List<Integer> childrenIds = jt.queryForList(
                "SELECT resource_id FROM resources WHERE parent_id = 0;",
                new Object[]{},
                Integer.class);

        // remove duplicates
        List<Integer> noDupesChildren = new ArrayList<Integer>(new LinkedHashSet<Integer>(childrenIds));

        List<Resource> resources = new ArrayList<Resource>();
        for (int resourceId : noDupesChildren) {
            Resource r = getResourceByIdHelper(resourceId, userId);
            resources.add(r);
        }

        return new StandardResponse(false, "Successfully retrieved resource forest", new Resources(resources));
    }

    private List<RT> getResourcesWithTags() {
        final String statement =
                "SELECT resources.name, resources.description, resources.restricted, resources.resource_id, resources.parent_id, resourcetags.tag " +
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
                        rt.parentId = rs.getInt("parent_id");
                        return rt;
                    }
                });
        return rts;
    }

    private List<RT> getResourcesWithoutTags() {
        final String noTagsStatement =
                "SELECT name, description, restricted, resource_id, parent_id " +
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
                        rt.parentId = rs.getInt("parent_id");
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
                        rt.sharedCount = rs.getInt("shared_count");
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

    public void getAllDescendants(Resource r, Set<Integer> set) {
        for (Resource child : r.getChildren()) {
            set.add(child.getResource_id());
            getAllDescendants(child, set);
        }
    }

    public StandardResponse updateResource(ResourceRequest req, int resourceId) {
        if (!req.isValidPut()) {
            return new StandardResponse(true, "Invalid request - parent ID must be included");
        }

        int resourceExists = jt.queryForObject(
                "SELECT COUNT(*) FROM resources WHERE resource_id = ?;", Integer.class, resourceId);
        if (resourceExists != 1) {
            return new StandardResponse(true, "Resource does not exist");
        }


        // error if parent is itself
        if (req.getParent_id() == resourceId) {
            return new StandardResponse(true, "Parent ID cannot be its own resource ID");
        }

        if (req.getParent_id() != 0) {
            int parentResourceExists = jt.queryForObject(
                    "SELECT COUNT(*) FROM resources WHERE resource_id = ?;", Integer.class, req.getParent_id());
            if (parentResourceExists != 1) {
                return new StandardResponse(true, "Parent resource does not exist");
            } else {
                // parent resource exists
                // make sure that the parent isn't in the set of its children
                Resource me = getResourceByIdHelper(resourceId, 1);
                Set<Integer> childrenIds = new HashSet<Integer>();
                getAllDescendants(me, childrenIds);
                if (childrenIds.contains(req.getParent_id())) {
                    return new StandardResponse(true, "Resources cannot form cyclic relations");
                }
            }
        }

        RT oldResource = getSpecificResource(resourceId);

        if (req.getName() == null) {
            req.setName(oldResource.name);
        }

        if (req.getDescription() == null) {
            req.setDescription(oldResource.description);
        }

        if (req.getShared_count() == null) {
            req.setShared_count(oldResource.sharedCount);
        }

        if (req.getTags() == null) {
            List<String> tags = jt.queryForList(
                    "SELECT DISTINCT tag FROM resourcetags WHERE resource_id = ?;", new Object[]{resourceId}, String.class);
            req.setTags(tags);
        }

        if(req.isRestricted() == null){
            req.setRestricted(oldResource.restricted);
        }

        if(oldResource.restricted == true && !req.isRestricted()){
            updateNewUnrestrictedResource(resourceId);
        }

        jt.update("UPDATE resources SET name = ?, description = ?, restricted = ?, shared_count = ?, parent_id = ? WHERE resource_id = ?;",
                req.getName(),
                req.getDescription(),
                req.isRestricted(),
                req.getShared_count(),
                req.getParent_id(),
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

        return new StandardResponse(false, "Successfully updated resource");
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

        Resource r = getResourceByIdHelper(resourceId, 1);
        int parent = r.getParent_id();
        for (Resource child : r.getChildren()) {
            //child.setParent_id(parent);
            jt.update("UPDATE resources SET parent_id = ? WHERE resource_id = ?;", parent, child.getResource_id());
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

    public boolean hasSpecificPermission(int userId, final String permissionType){
        List<Boolean> individualPermission = jt.query(
                "SELECT " + permissionType + " FROM users WHERE user_id = " + userId + 
                " AND " + permissionType + " = true;",
                new RowMapper<Boolean>() {
                    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getBoolean(permissionType);
                    }
                });


        List<Boolean> groupPermission = jt.query(
                "SELECT " + permissionType + " FROM groups, groupmembers WHERE user_id = " + userId + 
                " AND groups.group_id = groupmembers.group_id AND " + permissionType +  " = true;",
                new RowMapper<Boolean>() {
                    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getBoolean(permissionType);
                    }
                });

        return individualPermission.size() > 0 || groupPermission.size() > 0;
    }
}
