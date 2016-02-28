package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 2/22/16.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import resourceplanner.services.OAuthService;
import responses.StandardResponse;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    @Autowired
    private OAuthService oAuthService;

    @RequestMapping(value = "",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse auth(@RequestParam(value = "access_token", required=false) final String auth_code, @RequestParam(value="error", required=false) final String error) {
        if (error != null) {
            return new StandardResponse(true, "You are not authorized");
        }
        System.out.println(auth_code);
        if (auth_code == null) {
            return new StandardResponse(true, "Invalid authcode");
        }
        return oAuthService.auth(auth_code);
    }

}


