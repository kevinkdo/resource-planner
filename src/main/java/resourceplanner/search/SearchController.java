package resourceplanner.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import resourceplanner.main.Controller;
import resourceplanner.main.StandardResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jiaweizhang on 3/24/16.
 */
@RestController
@RequestMapping("/api/search")
public class SearchController extends Controller {

    @Autowired
    private SearchService searchService;

    @RequestMapping(value = "/user",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse getUserId(@RequestBody final SearchRequest req, final HttpServletRequest request) {
        if (!hasUserP(request) && getRequesterID(request) != 1) {
            return new StandardResponse(true, "No user permission");
        }
        return searchService.getUserId(req.getQuery());
    }

    @RequestMapping(value = "/resource",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse getResourceId(@RequestBody final SearchRequest req, final HttpServletRequest request) {
        if (!hasResourceP(request) && getRequesterID(request) != 1) {
            return new StandardResponse(true, "No resource permission");
        }
        return searchService.getResourceId(req.getQuery());
    }

    @RequestMapping(value = "/reservation",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse getReservationId(@RequestBody final SearchRequest req, final HttpServletRequest request) {
        if (!hasReservationP(request) && getRequesterID(request) != 1) {
            return new StandardResponse(true, "No reservation permission");
        }
        return searchService.getReservationId(req.getQuery());
    }

    @RequestMapping(value = "/group",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse getGroupId(@RequestBody final SearchRequest req, final HttpServletRequest request) {
        if (!hasUserP(request) && getRequesterID(request) != 1) {
            return new StandardResponse(true, "No user permission");
        }
        return searchService.getGroupId(req.getQuery());
    }
}