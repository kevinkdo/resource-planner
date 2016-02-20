package resourceplanner.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.GroupRequest;
import responses.StandardResponse;
import responses.data.Resource;
import responses.data.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
        return null;
    }

    public StandardResponse updateGroup(GroupRequest req, int groupId) {
        return null;
    }

    public StandardResponse deleteGroup(int groupId) {
        return null;
    }
}
