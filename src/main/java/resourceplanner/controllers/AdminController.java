package resourceplanner.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import resourceplanner.Application;
import resourceplanner.services.AdminService;
import responses.StandardResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jiaweizhang on 2/3/16.
 */

@RestController
@RequestMapping("/admin")
public class AdminController extends Controller {

    @Autowired
    private AdminService adminService;

    @RequestMapping(
            value = "/init",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse init(final HttpServletRequest request) throws Exception{
        return adminService.init();
    }

}