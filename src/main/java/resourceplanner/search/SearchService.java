package resourceplanner.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.main.StandardResponse;

/**
 * Created by jiaweizhang on 3/24/16.
 */
@Transactional
@Service
public class SearchService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse getUserId(String query) {

        return null;
    }

    public StandardResponse getResourceId(String query) {

        return null;
    }

    public StandardResponse getReservationId(String query) {

        return null;
    }

    public StandardResponse getGroupId(String query) {

        return null;
    }

}