package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/20/2016.
 */

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import responses.StandardResponse;
import responses.data.Tags;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController extends Controller {

    @Autowired
    private JdbcTemplate jt;

    public void setDataSource(DataSource dataSource) {
        jt = new JdbcTemplate(dataSource);
    }

    @RequestMapping(value = "",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getTags(final HttpServletRequest request) {
        List<String> tags = jt.queryForList(
                "SELECT DISTINCT tag FROM resourcetags;", String.class);
        return new StandardResponse(false, "Successfully retrieved tags", new Tags(tags));
    }

}

