package resourceplanner.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.main.StandardResponse;
import resourceplanner.search.responsedata.SearchResource;
import resourceplanner.search.responsedata.SearchUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by jiaweizhang on 3/24/16.
 */
@Transactional
@Service
public class SearchService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse getUserId(String query) {
        List<SearchUser> users = jt.query(
                "SELECT user_id, username, email FROM users WHERE username LIKE ?;",
                new Object[]{"%" + query + "%"},
                new RowMapper<SearchUser>() {
                    public SearchUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SearchUser u = new SearchUser();
                        u.setUser_id(rs.getInt("user_id"));
                        u.setUsername(rs.getString("username"));
                        u.setEmail(rs.getString("email"));
                        return u;
                    }
                }
        );

        return new StandardResponse(false, "Successfully retrieved users", users);
    }

    public StandardResponse getResourceId(String query) {
        List<SearchResource> r = jt.query(
                "SELECT resource_id, name, description FROM resources WHERE name LIKE ? OR description LIKE ?;",
                new Object[]{"%" + query + "%", "%" + query + "%"},
                new RowMapper<SearchResource>() {
                    public SearchResource mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SearchResource r = new SearchResource();
                        r.setResource_id(rs.getInt("resource_id"));
                        r.setName(rs.getString("name"));
                        r.setDescription(rs.getString("description"));
                        return r;
                    }
                }
        );

        return new StandardResponse(false, "Successfully retrieved resources", r);
    }

    public StandardResponse getReservationId(String query) {
        // TODO
        return null;
    }

    public StandardResponse getGroupId(String query) {
        // TODO
        return null;
    }

}