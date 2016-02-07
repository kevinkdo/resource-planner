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
import resourceplanner.services.ReservationService;
import resourceplanner.services.EmailService;

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

    @Autowired
    private EmailService emailService;


    public StandardResponse createUser(final UserRequest req) {
        
        if (!req.isValid()) {
            return new StandardResponse(true, "Invalid request");
        }
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Invalid password");
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

        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String finalPasswordHash = passwordHash;
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO users (email, passhash, username, should_email, permission) VALUES (?, ?, ?, ?, ?);",
                                new String[] {"user_id"});
                        ps.setString(1, req.getEmail());
                        ps.setString(2, finalPasswordHash);
                        ps.setString(3, req.getUsername());
                        ps.setBoolean(4, req.isShould_email());
                        ps.setInt(5, 0);
                        return ps;
                    }
                },
                keyHolder);

        User committed = new User(keyHolder.getKey().intValue(), req.getEmail(), req.getUsername(), req.isShould_email());
        return new StandardResponse(false, "Successfully registered.", committed);
    }

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
        if (!req.isUpdateValid()) {
            return new StandardResponse(true, "Invalid request", new User(userId, req.getEmail(), req.getUsername(), req.isShould_email()));
        }
        List<User> users = getUsers(userId);
        if (users.size() == 0) {
            return new StandardResponse(true, "User not found");
        }

        /*
        String passwordHash = null;
        try {
            passwordHash = PasswordHash.createHash(req.getPassword());
        } catch (Exception f) {
            return new StandardResponse(true, "Failed during hashing in register");
        }

        User user = users.get(0);
        if (!req.getEmail().equals(user.getEmail())) {
            // check if new email already exists
            int emailExists = jt.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE email = ?;", Integer.class, req.getEmail());
            if (emailExists != 0) {
                return new StandardResponse(true, "Email already exists");
            }
        }

        if (!req.getUsername().equals(user.getUsername())) {
            // check if new username already exists
            int usernameExists = jt.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE username = ?;", Integer.class, req.getUsername());
            if (usernameExists != 0) {
                return new StandardResponse(true, "Username already exists");
            }
        }
        */

        // do full update
        /*
        jt.update("UPDATE users SET email = ?, username = ?, passhash = ?, should_email = ? WHERE user_id = ?;",
                req.getEmail(),
                req.getUsername(),
                passwordHash,
                req.isShould_email(),
                userId);
                */
        jt.update("UPDATE users SET should_email = ? WHERE user_id = ?;",
                req.isShould_email(),
                userId);

        //User committed = new User(req.getEmail(), req.getUsername(), req.isShould_email());
        UserUpdate committed = new UserUpdate(req.isShould_email());
        emailService.upateEmailAfterUserChange(userId);
        return new StandardResponse(false, "Successfully updated email settings", committed);
    }

    public StandardResponse deleteUser(int userId) {
        emailService.cancelEmailsForReservationsOfUser(userId);
        jt.update("DELETE FROM reservations WHERE user_id = ?;", userId);
        jt.update("DELETE FROM users WHERE user_id = ?;", userId);
        return new StandardResponse(false, "Successfully deleted user");
        // TODO make sure that original admin cannot be deleted
    }
}
