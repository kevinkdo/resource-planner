package resourceplanner.services;

/**
 * Created by jiaweizhang on 2/22/16.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class PermissionService {

    @Autowired
    private JdbcTemplate jt;



}
