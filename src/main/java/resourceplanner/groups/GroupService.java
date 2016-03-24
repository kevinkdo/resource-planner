package resourceplanner.groups;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.groups.GroupData.Group;
import resourceplanner.groups.GroupData.SimpleGroup;
import resourceplanner.main.StandardResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jiaweizhang on 2/12/2016.
 */

@Transactional
@Service
public class GroupService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse createGroup(final GroupRequest req) {
        if (!req.isValid()) {
            return new StandardResponse(true, "Request not valid");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO groups (group_name) VALUES (?);",
                                new String[]{"group_id"});
                        ps.setString(1, req.getGroup_name());
                        return ps;
                    }
                },
                keyHolder);


        int groupId = keyHolder.getKey().intValue();

        // remove all duplicate user ids
        Set<Integer> userIdSet = new HashSet<Integer>();
        userIdSet.addAll(req.getUser_ids());

        // validate all user ids
        for (int userId : userIdSet) {
            int userNum = jt.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = ?;", new Object[]{userId}, Integer.class);
            if (userNum == 0) {
                return new StandardResponse(true, "User with id " + userId + " does not exist");
            }
        }

        List<Object[]> batch = new ArrayList<Object[]>();
        for (int userId : userIdSet) {
            Object[] values = new Object[]{
                    groupId,
                    userId};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO groupmembers (group_id, user_id) VALUES (?, ?);",
                batch);


        addDefaultResourcePermissions(groupId);

        Group returnGroup = new Group();
        returnGroup.setGroup_id(groupId);
        returnGroup.setGroup_name(req.getGroup_name());
        returnGroup.setUser_ids(req.getUser_ids());

        // TODO error message if repeated users
        return new StandardResponse(false, "Successfully created group.", returnGroup);
    }

    private void addDefaultResourcePermissions(int groupId){
        List<Integer> allResources = jt.query(
                "SELECT resource_id FROM resources;",
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("resource_id");
                    }
                });

        List<Object[]> batchGroupPermissions = new ArrayList<Object[]>();
        for(int i : allResources){
            batchGroupPermissions.add(new Object[]{groupId, i, 0});
        }

        jt.batchUpdate(
            "INSERT INTO groupresourcepermissions (group_id, resource_id, permission_level) VALUES (?, ?, ?);",
            batchGroupPermissions
            );
    }

    public StandardResponse getGroups() {
        List<SimpleGroup> groups = jt.query(
                "SELECT group_name, group_id FROM groups;",
                new RowMapper<SimpleGroup>() {
                    public SimpleGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SimpleGroup group = new SimpleGroup();
                        group.setGroup_id(rs.getInt("group_id"));
                        group.setGroup_name(rs.getString("group_name"));
                        return group;
                    }
                });
        return new StandardResponse(false, "Successfully retrieved groups", groups);
    }

    public StandardResponse getGroupById(int groupId) {
        List<String> groups = jt.query(
                "SELECT group_name FROM groups WHERE group_id = ?;",
                new Object[]{groupId},
                new RowMapper<String>() {
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString("group_name");
                    }
                });

        if (groups.size() != 1) {
            return new StandardResponse(true, "Group does not exist");
        }

        String group = groups.get(0);

        List<Integer> userIds = jt.queryForList(
                "SELECT user_id FROM groupmembers WHERE group_id = ?;",
                new Object[]{groupId},
                Integer.class);

        Group groupResponse = new Group();
        groupResponse.setGroup_name(group);
        groupResponse.setGroup_id(groupId);
        groupResponse.setUser_ids(userIds);

        return new StandardResponse(false, "Successfully retrieved group", groupResponse);
    }

    public StandardResponse updateGroup(GroupRequest req, int groupId) {
        if (!req.isValid()) {
            return new StandardResponse(true, "Request not valid");
        }
        int groupExists = jt.queryForObject(
                "SELECT COUNT(*) FROM groups WHERE group_id = ?;", Integer.class, groupId);
        if (groupExists != 1) {
            return new StandardResponse(true, "Group does not exist");
        }

        jt.update("UPDATE groups SET group_name = ? WHERE group_id = ?;",
                req.getGroup_name(),
                groupId);

        jt.update("DELETE FROM groupmembers WHERE group_id = ?;", groupId);

        // remove all duplicate user ids
        Set<Integer> userIdSet = new HashSet<Integer>();
        userIdSet.addAll(req.getUser_ids());

        // validate all user ids
        for (int userId : userIdSet) {
            int userNum = jt.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = ?;", new Object[]{userId}, Integer.class);
            if (userNum == 0) {
                return new StandardResponse(true, "User with id " + userId + " does not exist");
            }
        }

        List<Object[]> batch = new ArrayList<Object[]>();
        for (int userId : userIdSet) {
            Object[] values = new Object[]{
                    groupId,
                    userId};
            batch.add(values);
        }
        try{
            int[] updateCounts = jt.batchUpdate(
                "INSERT INTO groupmembers (group_id, user_id) VALUES (?, ?);",
                batch);
            return new StandardResponse(false, "Successfully updated group");
        }
        catch(Exception e){
            return new StandardResponse(true, "Tried to add non-existant users");
        }
    }

    public StandardResponse deleteGroup(int groupId) {
        int groupExists = jt.queryForObject(
                "SELECT COUNT(*) FROM groups WHERE group_id = ?;", Integer.class, groupId);
        if (groupExists != 1) {
            return new StandardResponse(true, "Group does not exist");
        }

        jt.update("DELETE FROM groupresourcepermissions WHERE group_id = ?;", groupId);
        jt.update("DELETE FROM groupmembers WHERE group_id = ?;", groupId);
        jt.update("DELETE FROM groups WHERE group_id = ?;", groupId);
        return new StandardResponse(false, "Successfully deleted group");
    }
}
