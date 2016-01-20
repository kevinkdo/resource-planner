package resourceplanner.controllers;

/**
 * Created by jiaweizhang on 1/20/2016.
 */
import databases.JDBC;
import org.springframework.web.bind.annotation.*;
import responses.StandardResponse;
import responses.data.Tags;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @RequestMapping(value = "",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getTags(final HttpServletRequest request) {
        return getTagsDB();
    }

    private StandardResponse getTagsDB() {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectTagsQuery = "SELECT DISTINCT tag FROM resourcetags;";
        List<String> tags = new ArrayList<String>();
        try {
            st = c.prepareStatement(selectTagsQuery);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String tag = rs.getString("tag");
                tags.add(tag);
            }
            return new StandardResponse(false, "successfully retrieved tags", null, new Tags(tags));
        } catch (Exception f) {
            return new StandardResponse(true, "Failed to fetch tags");
        }
    }
}

