package resourceplanner.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import resourceplanner.authentication.UserData.Login;
import resourceplanner.main.StandardResponse;
import utilities.TokenCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiaweizhang on 2/22/16.
 */

@Transactional
@Service
public class OAuthService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse auth(String authCode) {
        RestTemplate rt = new RestTemplate();
        Map<String, Object> variables = new HashMap<String, Object>();

        variables.put("access_token", authCode);

        OAuth res = null;
        try {
            res = rt.getForObject("https://oauth.oit.duke.edu/oauth/resource.php?access_token=" + authCode, OAuth.class);
        } catch (Exception e) {
            return new StandardResponse(true, "Failed to authenticate");
        }

        // get stuff from the OAuth object if possible
        if (res.getEppn() == null) {
            return new StandardResponse(true, "Failed to authenticate");
        }

        String netIdEmail = res.getEppn();

        String netId = netIdEmail.substring(0, netIdEmail.indexOf("@"));

        // check if netId exists in users table
        List<AuthUser> users = getUsers(netId);

        int userId = 0;
        if (users.size() != 1) {
            userId = createUser(netId);
            users = getUsers(netId);
            if (users.size() != 1) {
                return new StandardResponse(true, "Failed to add first-time user to database");
            }
        }
        //users.get(0).setUser_id(userId);
        String token = TokenCreator.generateToken(users.get(0), netId);
        Login login = new Login(token, users.get(0).getUser_id(), netId);
        return new StandardResponse(false, "Successfully authenticated", login);

    }

    private int createUser(final String netId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO users (email, username, should_email) VALUES (?, ?, ?);",
                                new String[]{"user_id"});
                        ps.setString(1, netId + "@duke.edu");
                        ps.setString(2, netId);
                        ps.setBoolean(3, true);
                        return ps;
                    }
                },
                keyHolder);

        addDefaultResourcePermissions(netId, netId + "@duke.edu");

        return keyHolder.getKey().intValue();

    }

    private void addDefaultResourcePermissions(String netId, String email){
        List<Integer> allResources = jt.query(
                "SELECT resource_id FROM resources;",
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("resource_id");
                    }
                });

        List<Integer> user = jt.query(
                "SELECT user_id FROM users WHERE username = '" + netId + "' AND email = '" + email + "';",
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("user_id");
                    }
                });

        List<Object[]> batchPermissions = new ArrayList<Object[]>();
        for(int i : allResources){
            batchPermissions.add(new Object[]{user.get(0), i, 0});
        }

        jt.batchUpdate(
            "INSERT INTO userresourcepermissions (user_id, resource_id, permission_level) VALUES (?, ?, ?);",
            batchPermissions
            );
    }

    private List<AuthUser> getUsers(final String netId) {
        List<AuthUser> users = jt.query(
                "SELECT user_id, super_p, resource_p, reservation_p, user_p FROM users WHERE username = ?;",
                new Object[]{netId},
                new RowMapper<AuthUser>() {
                    public AuthUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                        AuthUser user = new AuthUser();
                        user.setUser_id(rs.getInt("user_id"));
                        user.setSuper_p(rs.getBoolean("super_p"));
                        user.setResource_p(rs.getBoolean("resource_p"));
                        user.setReservation_p(rs.getBoolean("reservation_p"));
                        user.setUser_p(rs.getBoolean("user_p"));
                        return user;
                    }
                });
        return users;
    }

}