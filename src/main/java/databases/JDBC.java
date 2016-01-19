package databases;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by jiawe on 1/16/2016.
 */
public class JDBC {

    public static Connection connect() {

        String url = "jdbc:postgresql://localhost:5432/";
        String dbName = "rp";
        String driver = "org.postgresql.Driver";
        String username = "postgres";
        String password = "password";

        Connection c = null;
        try {
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            c = DriverManager.getConnection(url + dbName, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("Connected to database successfully");
        return c;
    }
}