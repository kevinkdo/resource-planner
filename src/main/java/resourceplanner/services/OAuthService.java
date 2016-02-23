package resourceplanner.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import resourceplanner.models.OAuth;
import responses.StandardResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiaweizhang on 2/22/16.
 */

@Transactional
@Service
public class OAuthService {

    //@Autowired
    //private JdbcTemplate jt;

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
        return new StandardResponse(false, "Successfully authenticated", res);
    }

}
