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
    public StandardResponse auth(@RequestParam(value = "code") final String auth_code) {
        System.out.println(auth_code);
        return null;
    }

}


