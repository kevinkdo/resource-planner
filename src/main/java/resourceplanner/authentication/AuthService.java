package resourceplanner.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.authentication.UserData.Login;
import resourceplanner.main.StandardResponse;
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
public class AuthService {
    @Autowired
    private JdbcTemplate jt;

    public StandardResponse login(UserRequest req) {
        List<AuthUser> users = jt.query(
                "SELECT user_id, passhash, super_p, resource_p, reservation_p, user_p FROM users WHERE username = ?;",
                new Object[]{req.getUsername()},
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
        AuthUser maxPermissionUser = getMaxPermissions(users.get(0));
        try {
            if (PasswordHash.validatePassword(req.getPassword(), maxPermissionUser.getPasshash())) {
                String token = TokenCreator.generateToken(maxPermissionUser, req.getUsername());
                Login login = new Login(token, maxPermissionUser.getUser_id(), req.getUsername());
                return new StandardResponse(false, "Successfully authenticated", login);
            }
        } catch (Exception e) {
            return new StandardResponse(true, "Failed to validate password");
        }
        return new StandardResponse(true, "Failed to validate password");
    }

    private AuthUser getMaxPermissions(AuthUser user) {
        AuthUser maxUser = new AuthUser();
        maxUser.setUser_id(user.getUser_id());
        maxUser.setPasshash(user.getPasshash());
        maxUser.setSuper_p(user.isSuper_p());

        List<AuthGroup> groups = jt.query(
                "SELECT groups.resource_p, groups.reservation_p, groups.user_p " +
                        "FROM groups INNER JOIN groupmembers " +
                        "ON groups.group_id = groupmembers.group_id " +
                        "WHERE groupmembers.user_id = ?;", new Object[]{user.getUser_id()},
                new RowMapper<AuthGroup>() {
                    public AuthGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
                        AuthGroup group = new AuthGroup();
                        group.setResource_p(rs.getBoolean("resource_p"));
                        group.setReservation_p(rs.getBoolean("reservation_p"));
                        group.setUser_p(rs.getBoolean("user_p"));
                        return group;
                    }
                });

        boolean hasResourceP = false;
        boolean hasReservationP = false;
        boolean hasUserP = false;

        //System.out.printf("Number of groupPermissions: %d\n", groups.size());

        for (AuthGroup g : groups) {
            if (g.isResource_p()) {
                hasResourceP = true;
                break;
            }
        }

        for (AuthGroup g : groups) {
            if (g.isReservation_p()) {
                hasReservationP = true;
                break;
            }
        }

        for (AuthGroup g : groups) {
            if (g.isUser_p()) {
                hasUserP = true;
                break;
            }
        }

        maxUser.setResource_p(hasResourceP);
        maxUser.setReservation_p(hasReservationP);
        maxUser.setUser_p(hasUserP);

        return maxUser;
    }
}
