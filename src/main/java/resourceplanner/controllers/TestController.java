package resourceplanner.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import responses.StandardResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiaweizhang on 1/19/2016.
 */


@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse register(@RequestBody final Object rd, final HttpServletRequest request) {
        return new StandardResponse(false, "Successful POST request test", rd);
    }

    @RequestMapping(value = "",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse login(final HttpServletRequest request) {
        RestTemplate rt = new RestTemplate();
        Map<String, Object> variables = new HashMap<String, Object>();

        String res = rt.getForObject("https://httpbin.org/get", String.class, variables);
        System.out.println(res);
        return new StandardResponse(false, "Successfully authenticated", res);
    }
}
