package resourceplanner.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.main.StandardResponse;
import resourceplanner.search.responsedata.SearchGroup;
import resourceplanner.search.responsedata.SearchReservation;
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
        List<SearchReservation> r = jt.query(
                "SELECT reservation_id, title, description FROM reservations WHERE title LIKE ? OR description LIKE ?;",
                new Object[]{"%" + query + "%", "%" + query + "%"},
                new RowMapper<SearchReservation>() {
                    public SearchReservation mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SearchReservation r = new SearchReservation();
                        r.setReservation_id(rs.getInt("reservation_id"));
                        r.setTitle(rs.getString("title"));
                        r.setDescription(rs.getString("description"));
                        return r;
                    }
                }
        );

        return new StandardResponse(false, "Successfully retrieved reservations", r);
    }

    public StandardResponse getGroupId(String query) {
        List<SearchGroup> g = jt.query(
                "SELECT group_id, group_name FROM groups WHERE group_name LIKE ?;",
                new Object[]{"%" + query + "%"},
                new RowMapper<SearchGroup>() {
                    public SearchGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SearchGroup g = new SearchGroup();
                        g.setGroup_id(rs.getInt("group_id"));
                        g.setGroup_name(rs.getString("group_name"));
                        return g;
                    }
                }
        );

        return new StandardResponse(false, "Successfully retrieved groups", g);
    }

}