package resourceplanner.permissions;

/**
 * Created by jiaweizhang on 2/22/16.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.main.StandardResponse;
import resourceplanner.permissions.PermissionData.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Transactional
@Service
public class PermissionService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse getPermissionMatrix(int userId, boolean systemPermission, boolean resourcePermission){
    	List<UserSystemPermission> userSystemPermissions = new ArrayList<UserSystemPermission>();
    	List<GroupSystemPermission> groupSystemPermissions = new ArrayList<GroupSystemPermission>();

    	List<UserResourcePermission> userResourcePermissions = new ArrayList<UserResourcePermission>();
    	List<GroupResourcePermission> groupResourcePermissions = new ArrayList<GroupResourcePermission>();

    	if(systemPermission){
    		userSystemPermissions = getUserSystemPermissions();
    		groupSystemPermissions = getGroupSystemPermissions();
    	}
    	if(resourcePermission){
    		userResourcePermissions = getUserResourcePermissions();
    		groupResourcePermissions = getGroupResourcePermissions();
    	}

    	filterResourcePermissions(userId, userResourcePermissions, groupResourcePermissions);

        removeAdminRow(userSystemPermissions, userResourcePermissions);

    	SystemPermissions systemPermissions = new SystemPermissions(userSystemPermissions, groupSystemPermissions);
    	ResourcePermissions resourcePermissions = new ResourcePermissions(userResourcePermissions, groupResourcePermissions); 

    	List<UserAndID> users =  jt.query(
                "SELECT user_id, username FROM users WHERE user_id != 1;",
                new RowMapper<UserAndID>() {
                    public UserAndID mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new UserAndID(rs.getInt("user_id"), rs.getString("username"));
                    }
                });
    	List<GroupAndID> groups =  jt.query(
                "SELECT group_id, group_name FROM groups;",
                new RowMapper<GroupAndID>() {
                    public GroupAndID mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new GroupAndID(rs.getInt("group_id"), rs.getString("group_name"));
                    }
                });
    	List<ResourceAndID> resources =  getViewableResourcesAndIDs(resourcePermissions);


    	PermissionMatrix matrix = new PermissionMatrix(users, groups, resources, systemPermissions, resourcePermissions);
    	return new StandardResponse(false, "Permissions retrieved", matrix);
    }

    public StandardResponse updatePermissionMatrix(PermissionRequest permissionMatrix, int userId,
            boolean resourceP, boolean userP){

        if(!permissionMatrix.isValid()){
            return new StandardResponse(true, "JSON input has unexpected format");
        }

    	SystemPermissions systemPermissions = permissionMatrix.getSystem_permissions();
    	ResourcePermissions resourcePermissions = permissionMatrix.getResource_permissions();

        
        if((systemPermissions.getUser_permissions().size() > 0 || systemPermissions.getGroup_permissions().size() > 0)
            && !userP){
            return new StandardResponse(true, "Trying to edit system permissions without user_p");
        }

        if((resourcePermissions.getUser_permissions().size() > 0 || resourcePermissions.getGroup_permissions().size() > 0)
            && !resourceP){
            return new StandardResponse(true, "Trying to edit resource permissions without resource_p");
        }

        if(userP){
            List<Object[]> batchUserPermissions = new ArrayList<Object[]>();
            for(UserSystemPermission u : systemPermissions.getUser_permissions()){
                batchUserPermissions.add(new Object[]{u.getResource_p(), u.getReservation_p(),
                    u.getUser_p(), u.getUser_id()});
            }

            jt.batchUpdate(
                "UPDATE users SET (resource_p, reservation_p, user_p) = (?, ?, ?) WHERE user_id = ?;",
                batchUserPermissions
                );

            List<Object[]> batchGroupPermissions = new ArrayList<Object[]>();
            for(GroupSystemPermission g : systemPermissions.getGroup_permissions()){
                batchGroupPermissions.add(new Object[]{g.getResource_p(), g.getReservation_p(),
                    g.getUser_p(), g.getGroup_id()});
            }

            jt.batchUpdate(
                "UPDATE groups SET (resource_p, reservation_p, user_p) = (?, ?, ?) WHERE group_id = ?;",
                batchGroupPermissions
                );
        }

        if(resourceP){
            List<Object[]> batchUserResourcePermissions = new ArrayList<Object[]>();
            for(UserResourcePermission u : resourcePermissions.getUser_permissions()){
                batchUserResourcePermissions.add(new Object[]{u.getPermission_level(), u.getUser_id(),
                    u.getResource_id()});
            }

            jt.batchUpdate(
                "UPDATE userresourcepermissions SET permission_level = ? WHERE (user_id = ? AND resource_id = ?);",
                batchUserResourcePermissions
                );

            List<Object[]> batchGroupResourcePermissions = new ArrayList<Object[]>();
            for(GroupResourcePermission g : resourcePermissions.getGroup_permissions()){
                batchGroupResourcePermissions.add(new Object[]{g.getPermission_level(), g.getGroup_id(),
                    g.getResource_id()});
            }

            jt.batchUpdate(
                "UPDATE groupresourcepermissions SET permission_level = ? WHERE (group_id = ? AND resource_id = ?);",
                batchGroupResourcePermissions
                );
        }

        return new StandardResponse(false, "Permissions updated");
    }

    private static class TempMapResource {
        private Integer resource_id;
        private String name;
    }

    //Returns an object containing resource NAMES since resource permissions is just IDs. 
    private List<ResourceAndID> getViewableResourcesAndIDs(ResourcePermissions resourcePermissions){
    	List<TempMapResource> rList = jt.query(
    			"SELECT resource_id, name FROM resources;",
                new Object[]{},
                new RowMapper<TempMapResource>() {
			        public TempMapResource mapRow(ResultSet rs, int rowNum) throws SQLException {
                        TempMapResource t = new TempMapResource();
                        t.resource_id = rs.getInt("resource_id");
                        t.name = rs.getString("name");
			            return t;
			        };
			    });

        Map<Integer, String> allResources = new HashMap<Integer, String>();

        for (TempMapResource r : rList) {
            allResources.put(r.resource_id, r.name);
        }

    	List<ResourceAndID> viewableResources = new ArrayList<ResourceAndID>();

    	Set<Integer> resource_ids = new TreeSet<Integer>();
    	for(UserResourcePermission u : resourcePermissions.getUser_permissions()){
    		if(!resource_ids.contains(u.getResource_id())){
    			viewableResources.add(new ResourceAndID(u.getResource_id(), allResources.get(u.getResource_id())));
    			resource_ids.add(u.getResource_id());
    		}	
    	}
    	for(GroupResourcePermission g : resourcePermissions.getGroup_permissions()){
    		if(!resource_ids.contains(g.getResource_id())){
    			viewableResources.add(new ResourceAndID(g.getResource_id(), allResources.get(g.getResource_id())));
    			resource_ids.add(g.getResource_id());
    		}	
    	}

    	return viewableResources;  	
    }

    private void removeAdminRow(List<UserSystemPermission> userSystemPermissions, List<UserResourcePermission> userResourcePermissions){       
        List<UserSystemPermission> adminSystemPermissions = new ArrayList<UserSystemPermission>();
        for(UserSystemPermission u : userSystemPermissions){
            if(u.getUser_id() == 1){
                adminSystemPermissions.add(u);
            }
        }
        userSystemPermissions.removeAll(adminSystemPermissions);

        List<UserResourcePermission> adminResourcePermissions = new ArrayList<UserResourcePermission>();
        for(UserResourcePermission u : userResourcePermissions){
            if(u.getUser_id() == 1){
                adminResourcePermissions.add(u);
            }
        }
        userResourcePermissions.removeAll(adminResourcePermissions);
    }

    // Filters resource permissions to only include those that the user is allowed to see/reserve. 
    private void filterResourcePermissions(int userId, List<UserResourcePermission> userResourcePermissions, List<GroupResourcePermission> groupResourcePermissions){
        if(userId == 1){
            return;
        }

    	List<Integer> userViewableResources = getUserViewableResources(userId);
    	List<Integer> groupViewableResources = getGroupViewableResources(userId);
    	
    	Set<Integer> allViewableResources = new TreeSet<Integer>(userViewableResources); 
		allViewableResources.addAll(groupViewableResources);

        List<UserResourcePermission> userPermissionsToRemove = new ArrayList<UserResourcePermission>();
		for(UserResourcePermission u : userResourcePermissions){
			if(!allViewableResources.contains(u.getResource_id())){
                userPermissionsToRemove.add(u);
				//userResourcePermissions.remove(u);
			}
		}
        for(UserResourcePermission u : userPermissionsToRemove){
            userResourcePermissions.remove(u);
        }


        List<GroupResourcePermission> groupPermissionsToRemove = new ArrayList<GroupResourcePermission>();
		for(GroupResourcePermission g : groupResourcePermissions){
			if(!allViewableResources.contains(g.getResource_id())){
                groupPermissionsToRemove.add(g);
				//groupResourcePermissions.remove(g);
			}
		}
        for(GroupResourcePermission g : groupPermissionsToRemove){
            groupResourcePermissions.remove(g);
        }
    }

    //Returns all resources that a user can view due to personal permissions
    //(NOT INCLUDED ARE RESOURCES THE USER CAN VIEW FROM A GROUP HE IS IN)
    public List<Integer> getUserViewableResources(int userId){
    	return getUserResourcesWithPermission(1, userId);
    }

    //Returns all resources a user can view due to group memberships
    //(NOT INCLUDED ARE RESOURCES THE USER CAN VIEW FROM PERSONAL PERMISSIONS)
    public List<Integer> getGroupViewableResources(int userId){
        return getGroupResourcesWithPermission(1, userId);
    }

    //Returns all resources a user can view, via BOTH groups and personal permissions
    public List<Integer> getAllViewableResources(int userId){
        Set<Integer> resources = new HashSet<Integer>();
        resources.addAll(getUserViewableResources(userId));
        resources.addAll(getGroupViewableResources(userId));

        List<Integer> allResources = new ArrayList<Integer>();
        allResources.addAll(resources);

        return allResources;
    }

    //Returns all resources that a user can view due to personal permissions
    //(NOT INCLUDED ARE RESOURCES THE USER CAN VIEW FROM A GROUP HE IS IN)
    public List<Integer> getUserReservableResources(int userId){
        return getUserResourcesWithPermission(2, userId);
    }

    //Returns all resources a user can view due to group memberships
    //(NOT INCLUDED ARE RESOURCES THE USER CAN VIEW FROM PERSONAL PERMISSIONS)
    public List<Integer> getGroupReservableResources(int userId){
        return getGroupResourcesWithPermission(2, userId);
    }

    //Returns all resources a user can reserve, via BOTH groups and personal permissions
    public List<Integer> getAllReservableResources(int userId){
        Set<Integer> resources = new HashSet<Integer>();
        resources.addAll(getUserReservableResources(userId));
        resources.addAll(getGroupReservableResources(userId));

        List<Integer> allResources = new ArrayList<Integer>();
        allResources.addAll(resources);

        return allResources;
    }

    //Returns all resources a user is resource manager of HIMSELF (not through groups)
    public List<Integer> getUserResourceManagerResources(int userId){
        return getUserResourcesWithPermission(3, userId);
    }

    //Returns all resources a user is resource manager of through GROUPS (not personally)
    public List<Integer> getGroupResourceManagerResources(int userId){
        return getGroupResourcesWithPermission(3, userId);
    }

    //Returns all resources a user is resource manager of, via BOTH groups and personal
    //permissions
    public List<Integer> getAllResourceManagerResources(int userId){
        Set<Integer> resources = new HashSet<Integer>();
        resources.addAll(getUserResourceManagerResources(userId));
        resources.addAll(getGroupResourceManagerResources(userId));

        List<Integer> allResources = new ArrayList<Integer>();
        allResources.addAll(resources);

        return allResources;
    }

    //Returns all resources a user is resource manager of that are ALSO restricted
    public List<Integer> getAllRestrictedResourceManagerResources(int userId){
        String userQueryString = "SELECT resources.resource_id FROM userresourcepermissions, resources WHERE user_id = " + userId +
            " AND permission_level >= 3 AND resources.resource_id = userresourcepermissions.resource_id AND restricted = true;"; 

        List<Integer> userResources = jt.query(
                userQueryString,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Integer(rs.getInt("resource_id"));
                    }
                });

        String groupQueryString = "SELECT resource_id FROM groupresourcepermissions, resources WHERE group_id IN " +
                "(SELECT group_id FROM groupmembers WHERE groupmembers.user_id = " + userId + ") AND " + 
                "permission_level >= 3 AND resources.resource_id = groupresourcepermissions.resource_id AND restricted = true;";
        
        List<Integer> groupResources = jt.query(
                groupQueryString,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Integer(rs.getInt("resource_id"));
                    }
                });
        Set<Integer> resources = new HashSet<Integer>();
        resources.addAll(userResources);
        resources.addAll(groupResources);
        List<Integer> allResources = new ArrayList<Integer>();
        allResources.addAll(resources);
        return allResources;
    }

    private List<Integer> getUserResourcesWithPermission(int requiredPermissionLevel, int userId){
        String userQueryString = "SELECT resource_id FROM userresourcepermissions WHERE user_id = " + userId +
            " AND permission_level >= " + requiredPermissionLevel + ";"; 

        List<Integer> userResources = jt.query(
                userQueryString,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Integer(rs.getInt("resource_id"));
                    }
                });

        return userResources;
    }

    private List<Integer> getGroupResourcesWithPermission(int requiredPermissionLevel, int userId){
        String groupQueryString = "SELECT resource_id FROM groupresourcepermissions WHERE group_id IN " +
                "(SELECT group_id FROM groupmembers WHERE groupmembers.user_id = " + userId + ") AND " + 
                "permission_level >= " + requiredPermissionLevel + ";";
        
        List<Integer> groupResources = jt.query(
                groupQueryString,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Integer(rs.getInt("resource_id"));
                    }
                });
        return groupResources;
    }


    private List<UserSystemPermission> getUserSystemPermissions(){
    	List<UserSystemPermission> userSystemPermissions = jt.query(
                "SELECT user_id, resource_p, reservation_p, user_p FROM users;",
                new RowMapper<UserSystemPermission>() {
                    public UserSystemPermission mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new UserSystemPermission(rs.getBoolean("resource_p"), rs.getBoolean("reservation_p"),
                        	rs.getBoolean("user_p"), rs.getInt("user_id"));
                    }
                });

    	return userSystemPermissions;
    }

    private List<GroupSystemPermission> getGroupSystemPermissions(){
    	List<GroupSystemPermission> groupSystemPermissions = jt.query(
                "SELECT group_id, resource_p, reservation_p, user_p FROM groups;",
                new RowMapper<GroupSystemPermission>() {
                    public GroupSystemPermission mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new GroupSystemPermission(rs.getBoolean("resource_p"), rs.getBoolean("reservation_p"),
                        	rs.getBoolean("user_p"), rs.getInt("group_id"));
                    }
                });

    	return groupSystemPermissions;
    }

    private List<UserResourcePermission> getUserResourcePermissions(){
    	List<UserResourcePermission> userResourcePermissions = jt.query(
                "SELECT * FROM userresourcepermissions;",
                new RowMapper<UserResourcePermission>() {
                    public UserResourcePermission mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new UserResourcePermission(rs.getInt("resource_id"), rs.getInt("permission_level"),
                        		rs.getInt("user_id"));
                    }
                });

    	return userResourcePermissions;
    }

    private List<GroupResourcePermission> getGroupResourcePermissions(){
    	List<GroupResourcePermission> groupResourcePermissions = jt.query(
                "SELECT * FROM groupresourcepermissions;",
                new RowMapper<GroupResourcePermission>() {
                    public GroupResourcePermission mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new GroupResourcePermission(rs.getInt("resource_id"), rs.getInt("permission_level"),
                        		rs.getInt("group_id"));
                    }
                });

    	return groupResourcePermissions;
    }



}
