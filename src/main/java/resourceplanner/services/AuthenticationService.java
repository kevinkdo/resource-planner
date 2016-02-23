package resourceplanner.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.UserRequest;
import resourceplanner.models.AuthUser;
import responses.StandardResponse;
import responses.data.Login;
import utilities.PasswordHash;
import utilities.TokenCreator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by jiaweizhang on 1/26/2016.
 */

@Transactional
@Service
public class AuthenticationService {
    @Autowired
    private JdbcTemplate jt;

    public StandardResponse login(UserRequest req) {
        List<AuthUser> users = jt.query(
                "SELECT user_id, passhash, super_p, resource_p, reservation_p, user_p FROM users WHERE username = ?;",
                new Object[]{req.getEmail(), req.getUsername()},
                new RowMapper<AuthUser>() {
                    public AuthUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                        AuthUser user = new AuthUser();
                        user.setUser_id(rs.getInt("user_id"));
                        user.setPasshash(rs.getString("passhash"));
                        user.setSuper_p(rs.getBoolean("super_p"));
                        user.setResource_p(rs.getBoolean("resource_p"));
                        user.setReservation_p(rs.getBoolean("reservation_p"));
                        user.setUser_p(rs.getBoolean("user_p"));
                        return user;
                    }
                });
        if (users.size() != 1) {
            return new StandardResponse(true, "Account does not exist");
        }
        try {
            if (PasswordHash.validatePassword(req.getPassword(), users.get(0).getPasshash())) {
                String token = TokenCreator.generateToken(users.get(0), "admin");
                Login login = new Login(token, users.get(0).getUser_id(), "admin");
                return new StandardResponse(false, "Successfully authenticated", login);
            }
        } catch (Exception e) {
            return new StandardResponse(true, "Failed to validate password");
        }
        return new StandardResponse(true, "Failed to validate password");
    }

}
