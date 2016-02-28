package resourceplanner.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import resourceplanner.main.StandardResponse;

/**
 * Created by jiaweizhang on 2/22/16.
 */

@RestController
@RequestMapping("/serveroauth")
public class OAuthController {

    @Autowired
    private OAuthService oAuthService;

    @RequestMapping(value = "",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse auth(@RequestParam(value = "access_token", required = false) final String auth_code) {
        System.out.println(auth_code);
        if (auth_code == null) {
            return new StandardResponse(true, "Invalid authcode");
        }
        return oAuthService.auth(auth_code);
    }

}

