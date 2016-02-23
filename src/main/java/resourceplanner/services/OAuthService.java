package resourceplanner.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import resourceplanner.models.AuthUser;
import resourceplanner.models.OAuth;
import responses.StandardResponse;
import responses.data.Login;
import utilities.TokenCreator;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        variables.put("grant_type", "authorization_code");
        variables.put("code", authCode);
        variables.put("redirect_uri", "https://colab-sbx-304.oit.duke.edu/oauth");
        variables.put("client_id", 1234); // TODO
        variables.put("client_secret", "some client secret");

        OAuth res = rt.postForObject("https://oauth2.duke.edu/token", null, OAuth.class, variables);
        System.out.println(res);

        // get stuff from the OAuth object if possible
        String netId = "abc123";
        String dukeToken = res.getAccess_token();
        if (dukeToken == null || dukeToken.equals("")) {
            return new StandardResponse(true, "Failed to authenticate");
        }

        // check if netId exists in users table
        List<AuthUser> users = getUsers(netId);

        int userId = 0;
        if (users.size() != 1) {
            createUser(netId);
            users = getUsers(netId);
            if (users.size() != 1) {
                return new StandardResponse(true, "Failed to add first-time user to database");
            }
        }
        users.get(0).setUser_id(userId);

        String token = TokenCreator.generateToken(users.get(0), netId);
        Login login = new Login(token, users.get(0).getUser_id(), netId);
        return new StandardResponse(false, "Successfully authenticated", login);

    }

    private void createUser(final String netId) {
        jt.update(
                "INSERT INTO users (email, username, should_email) VALUES (?, ?, ?);",
                new Object[]{netId + "@duke.edu", netId, true});

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
