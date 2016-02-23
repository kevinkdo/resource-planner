package resourceplanner.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.UserRequest;
import responses.StandardResponse;
import responses.data.User;
import responses.data.UserUpdate;
import utilities.PasswordHash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by jiaweizhang on 1/26/2016.
 */

@Transactional
@Service
public class UserService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse getUserById(int userId) {
        List<User> users = getUsers(userId);
        if (users.size() == 0) {
            return new StandardResponse(true, "User not found");
        }
        User user = users.get(0);
        return new StandardResponse(false, "Successfully retrieved user", user);
    }

    public List<User> getUsers(int userId) {
        return jt.query(
                "SELECT * FROM users WHERE user_id = ?;",
                new Object[]{userId},
                new RowMapper<User>() {
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        User user = new User();
                        user.setEmail(rs.getString("email"));
                        user.setUsername(rs.getString("username"));
                        user.setShould_email(rs.getBoolean("should_email"));
                        user.setUser_id(rs.getInt("user_id"));
                        return user;
                    }
                });
    }
}
