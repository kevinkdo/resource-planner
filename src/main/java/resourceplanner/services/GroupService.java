package resourceplanner.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.GroupRequest;
import responses.StandardResponse;
import responses.data.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiaweizhang on 2/12/2016.
 */

@Transactional
@Service
public class GroupService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse createGroup(final GroupRequest req) {
        if (req.isValid()) {
            return new StandardResponse(true, "Request not valid");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO groups (name) VALUES (?);",
                                new String[] {"user_id"});
                        ps.setString(1, req.getGroup_name());
                        return ps;
                    }
                },
                keyHolder);


        int groupId = keyHolder.getKey().intValue();

        List<Object[]> batch = new ArrayList<Object[]>();
        for (int userId : req.getUser_ids()) {
            Object[] values = new Object[]{
                    groupId,
                    userId};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO groupmembers (group_id, user_id) VALUES (?, ?);",
                batch);

        // TODO error message if repeated users
        return new StandardResponse(false, "Successfully created group.");
    }

    public StandardResponse getGroupById(int groupId) {
        List<String> groups = jt.query(
                "SELECT name FROM groups WHERE group_id_id = ?;",
                new Object[]{groupId},
                new RowMapper<String>() {
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString("name");
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
        groupResponse.setName(group);
        groupResponse.setGroup_id(groupId);
        groupResponse.setUser_ids(userIds);

        return new StandardResponse(false, "Successfully retrieved resource", groupResponse);
    }

    public StandardResponse updateGroup(GroupRequest req, int groupId) {
        if (req.isValid()) {
            return new StandardResponse(true, "Request not valid");
        }
        int groupExists = jt.queryForObject(
                "SELECT COUNT(*) FROM groups WHERE group_id = ?;", Integer.class, groupId);
        if (groupExists != 1) {
            return new StandardResponse(true, "Group does not exist");
        }

        jt.update("UPDATE groups SET name = ? WHERE group_id = ?;",
                req.getGroup_name(),
                groupId);

        jt.update("DELETE FROM groupmembers WHERE group_id = ?;", groupId);

        List<Object[]> batch = new ArrayList<Object[]>();
        for (int userId : req.getUser_ids()) {
            Object[] values = new Object[]{
                    groupId,
                    userId};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO groupmembers (group_id, user_id) VALUES (?, ?);",
                batch);

        return new StandardResponse(false, "Successfully updated group");
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
