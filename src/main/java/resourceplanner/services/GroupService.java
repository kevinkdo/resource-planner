package resourceplanner.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.GroupRequest;
import responses.StandardResponse;

/**
 * Created by jiaweizhang on 2/12/2016.
 */

@Transactional
@Service
public class GroupService {

    @Autowired
    private JdbcTemplate jt;


    public StandardResponse createGroup(GroupRequest req) {
        return null;
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
