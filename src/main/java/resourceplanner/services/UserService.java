package resourceplanner.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.UserRequest;
import responses.StandardResponse;
import responses.data.User;
import utilities.PasswordHash;

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

    public StandardResponse createUser(UserRequest req) {
        User committed = new User(req.getEmail(), req.getUsername(), req.isShould_email());
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during hashing in register", committed);
        }
        int emailExists = jt.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?;", Integer.class, req.getEmail());
        if (emailExists != 0) {
            return new StandardResponse(true, "Email already exists", committed);
        }

        int usernameExists = jt.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?;", Integer.class, req.getUsername());
        if (usernameExists != 0) {
            return new StandardResponse(true, "Username already exists", committed);
        }

        int returnedValue = jt.update(
                "INSERT INTO users (email, passhash, username, should_email) VALUES (?, ?, ?, ?);",
                req.getEmail(), passwordHash, req.getUsername(), req.isShould_email());
        return new StandardResponse(false, "Successfully registered.", committed);
    }

    // alternate implementation that is possibly faster
    public StandardResponse getUserById(int userId) {
        List<User> users = getUsers(userId);
        if (users.size() == 0) {
            return new StandardResponse(true, "User not found");
        }
        User user = users.get(0);
        return new StandardResponse(false, "successfully retrieved user", null, user);
    }

    public List<User> getUsers(int userId) {
        return jt.query(
                "SELECT email, username, should_email FROM users WHERE user_id = ?;",
                new Object[]{userId},
                new RowMapper<User>() {
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        User user = new User();
                        user.setEmail(rs.getString("email"));
                        user.setUsername(rs.getString("username"));
                        user.setShould_email(rs.getBoolean("should_email"));
                        return user;
                    }
                });
    }

    public StandardResponse updateUser(UserRequest req, int userId) {
        List<User> users = getUsers(userId);
        if (users.size() == 0) {
            return new StandardResponse(true, "User not found");
        }

        User committed = new User(req.getEmail(), req.getUsername(), req.isShould_email());
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during hashing in register", committed);
        }

        User user = users.get(0);
        if (!req.getEmail().equals(user.getEmail())) {
            // check if new email already exists
            int emailExists = jt.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE email = ?;", Integer.class, req.getEmail());
            if (emailExists != 0) {
                return new StandardResponse(true, "Email already exists", committed);
            }
        }

        if (!req.getUsername().equals(user.getUsername())) {
            // check if new username already exists
            int usernameExists = jt.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE username = ?;", Integer.class, req.getUsername());
            if (usernameExists != 0) {
                return new StandardResponse(true, "Username already exists", committed);
            }
        }

        // do update
        jt.update("UPDATE users SET email = ?, username = ?, passhash = ?, should_email = ? WHERE user_id = ?;",
                req.getEmail(),
                req.getUsername(),
                passwordHash,
                req.isShould_email(),
                userId);
        return new StandardResponse(false, "successfully updated", committed);
    }
}
