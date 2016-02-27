package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import resourceplanner.main.StandardResponse;

/**
 * Created by jiaweizhang on 1/19/2016.
 */
public class ExceptionCreator {
    public static String createJson(String message) {
        ObjectMapper mapper = new ObjectMapper();
        StandardResponse response = new StandardResponse(true, message);
        try {
            String json = mapper.writeValueAsString(response);
            return json;
        } catch (Exception e) {
            return "";
        }
    }
}
