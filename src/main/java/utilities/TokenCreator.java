package utilities;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Created by jiawe on 1/17/2016.
 */
public class TokenCreator {
    public static String generateToken(String email) {
        String jwt = Jwts.builder().setSubject(email)
                .signWith(SignatureAlgorithm.HS256, "secretkey").compact();
        return jwt;
    }
}
