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

@Transactional
@Service
public class PermissionService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse getPermissionMatrix(int userId, boolean systemPermissions, boolean resourcePermissions){
    	return new StandardResponse(false, "not yet");
    }

    public StandardResponse updatePermissionMatrix(PermissionRequest req, int userId){
    	return new StandardResponse(false, "not yet");
    }

}
