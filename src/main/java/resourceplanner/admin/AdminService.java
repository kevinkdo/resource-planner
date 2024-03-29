package resourceplanner.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.main.StandardResponse;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by jiaweizhang on 2/3/16.
 */

@Transactional
@Service
public class AdminService {

    @Autowired
    private JdbcTemplate jt;

    public StandardResponse init() {
        String query;
        try {
            BufferedReader in = new BufferedReader(new FileReader("admin/setup.sql"));
            String str;
            StringBuffer sb = new StringBuffer();
            while ((str = in.readLine()) != null) {
                sb.append(str + " ");
            }
            in.close();

            query = sb.toString();
            //System.out.println(query);
        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(false, "Failed to read SQL file");
        }

        jt.execute(query);

        return new StandardResponse(false, "Successfully reset database");
    }

}
