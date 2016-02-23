package resourceplanner.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import responses.StandardResponse;

/**
 * Created by jiaweizhang on 2/22/16.
 */
@Transactional
@Service
public class OAuthService {
    @Autowired
    private JdbcTemplate jt;

    public StandardResponse auth(String authCode) {
        return null;
    }

}
