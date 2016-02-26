package resourceplanner.services;

/**
 * Created by jiaweizhang on 2/22/16.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.PermissionRequest;
import responses.StandardResponse;
import responses.data.*;
import responses.data.PermissionData.*;
import java.util.*;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.ResultSetExtractor;


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

    	SystemPermissions systemPermissions = new SystemPermissions(userSystemPermissions, groupSystemPermissions);
    	ResourcePermissions resourcePermissions = new ResourcePermissions(userResourcePermissions, groupResourcePermissions); 

    	List<UserAndID> users =  jt.query(
                "SELECT user_id, username FROM users;",
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

    public StandardResponse updatePermissionMatrix(PermissionRequest req, int userId){
    	return new StandardResponse(true, "Not implemented yet");
    }


    private List<ResourceAndID> getViewableResourcesAndIDs(ResourcePermissions resourcePermissions){
    	/*
    	List<ResourceAndID> allResources = jt.query(
                "SELECT resource_id, name FROM resources;",
                new RowMapper<ResourceAndID>() {
                    public ResourceAndID mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new ResourceAndID(rs.getInt("resource_id"), rs.getInt("name"));
                    }
                });
        */

    	Map<Integer, String> allResources = (Map) jt.query(
    			"SELECT resource_id, name FROM resources;", new ResultSetExtractor() {
			        public Object extractData(ResultSet rs) throws SQLException {
			            Map map = new HashMap();
			            while (rs.next()) {
			                Integer id = rs.getInt("resource_id");
			                String name = rs.getString("name");
			                map.put(id, name);
			            }
			            return map;
			        };
			    });

    	List<ResourceAndID> viewableResources = new ArrayList<ResourceAndID>();

    	for(UserResourcePermission u : resourcePermissions.getUser_permissions()){
    		viewableResources.add(new ResourceAndID(u.getResource_id(), allResources.get(u.getResource_id())));
    	}
    	for(GroupResourcePermission g : resourcePermissions.getGroup_permissions()){
    		viewableResources.add(new ResourceAndID(g.getResource_id(), allResources.get(g.getResource_id())));
    	}

    	return viewableResources;  	
    }

    // Filters resource permissions to only include those that the user is allowed to see/reserve. 
    private void filterResourcePermissions(int userId, List<UserResourcePermission> userResourcePermissions, List<GroupResourcePermission> groupResourcePermissions){
    	List<Integer> userViewableResources = getUserViewableResources(userId);
    	List<Integer> groupViewableResources = getGroupViewableResources(userId);
    	
    	Set<Integer> allViewableResources = new TreeSet<Integer>(userViewableResources); 
		allViewableResources.addAll(groupViewableResources);

		for(UserResourcePermission u : userResourcePermissions){
			if(!allViewableResources.contains(u.getResource_id())){
				userResourcePermissions.remove(u);
			}
		}

		for(GroupResourcePermission g : groupResourcePermissions){
			if(!allViewableResources.contains(g.getResource_id())){
				groupResourcePermissions.remove(g);
			}
		}
    }

    //Returns all resources that a user can view due to personal permissions
    //(NOT INCLUDED ARE RESOURCES THE USER CAN VIEW FROM A GROUP HE IS IN)
    private List<Integer> getUserViewableResources(int userId){
    	String userViewableQueryString = "SELECT resource_id FROM userresourcepermissions WHERE user_id = " + userId +
    		" AND permission_level > 0;"; 

    	List<Integer> userViewableResources = jt.query(
                userViewableQueryString,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Integer(rs.getInt("resource_id"));
                    }
                });

    	return userViewableResources;
    }

    //Returns all resources a user can view due to group memberships
    //(NOT INCLUDED ARE RESOURCES THE USER CAN VIEW FROM PERSONAL PERMISSIONS)
    private List<Integer> getGroupViewableResources(int userId){
    	String groupViewableQueryString = "SELECT resource_id FROM groupresourcepermissions WHERE group_id IN " +
    			"(SELECT group_id FROM groupmembers WHERE groupmembers.user_id = " + userId + ") AND " + 
    			"permission_level > 0;";
    	
    	List<Integer> groupViewableResources = jt.query(
                groupViewableQueryString,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Integer(rs.getInt("resource_id"));
                    }
                });
    	return groupViewableResources;
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
