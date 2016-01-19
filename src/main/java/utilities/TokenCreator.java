package utilities;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Created by jiaweizhang on 1/17/2016.
 */
public class TokenCreator {
    public static String generateToken(int userId, String email) {
        String jwt = Jwts.builder().setSubject(email)
                .claim("email", email)
                .claim("user_id", userId)
                .signWith(SignatureAlgorithm.HS256, "secretkey").compact();
        return jwt;
    }


}
