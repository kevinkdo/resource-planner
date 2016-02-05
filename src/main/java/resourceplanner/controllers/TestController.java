package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/19/2016.
 */

import org.springframework.web.bind.annotation.*;
import responses.StandardResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class TestController {

    @RequestMapping(value = "/test",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse register(@RequestBody final Object rd, final HttpServletRequest request) {
        return new StandardResponse(false, "Successful POST request test", rd);
    }

    @RequestMapping(value = "/test",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse login(final HttpServletRequest request) {
        return new StandardResponse(false, "successful GET request test");
    }
}


