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

    public StandardResponse register(UserRequest req) {
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during password hashing.");
        }
        int emailExists = jt.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?;", Integer.class, req.getEmail());
        if (emailExists != 0) {
            return new StandardResponse(true, "Email already exists");
        }
        int usernameExists = jt.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?;", Integer.class, req.getUsername());
        if (usernameExists != 0) {
            return new StandardResponse(true, "Username already exists");
        }
        int returnedValue = jt.update(
                "INSERT INTO users (email, username, passhash, permission, should_email) VALUES (?, ?, ?, 0, true);",
                req.getEmail(), req.getUsername(), passwordHash);
        return new StandardResponse(false, "Successfully registered.");
    }

    public StandardResponse login(UserRequest req) {
        List<AuthUser> users = jt.query(
                "SELECT user_id, passhash, permission FROM users WHERE email = ? OR username = ?;",
                new Object[]{req.getEmail(), req.getUsername()},
                new RowMapper<AuthUser>() {
                    public AuthUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                        AuthUser user = new AuthUser();
                        user.setUser_id(rs.getInt("user_id"));
                        user.setPasshash(rs.getString("passhash"));
                        user.setPermission(rs.getInt("permission"));
                        return user;
                    }
                });
        if (users.size() != 1) {
            return new StandardResponse(true, "Email and username do not exist");
        }
        try {
            if (PasswordHash.validatePassword(req.getPassword(), users.get(0).getPasshash())) {
                String token = TokenCreator.generateToken(users.get(0).getUser_id(), users.get(0).getPermission());
                Login login = new Login(token, users.get(0).getUser_id());
                return new StandardResponse(false, "Successfully authenticated.", login);
            }
        } catch (Exception e) {
            return new StandardResponse(true, "Failed to validate password.");
        }
        return new StandardResponse(true, "Failed to validate password.");
    }

}
