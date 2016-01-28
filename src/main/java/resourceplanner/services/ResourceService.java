package resourceplanner.services;

import databases.JDBC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.ResourceRequest;
import responses.StandardResponse;
import responses.data.Resource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

}
