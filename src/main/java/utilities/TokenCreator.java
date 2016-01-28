package utilities;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Created by jiaweizhang on 1/17/2016.
 */
public class TokenCreator {
    public static String generateToken(int userId, int permission) {
        String jwt = Jwts.builder().setSubject(Integer.toString(userId))
                .claim("user_id", userId)
                .claim("permission", permission)
                .signWith(SignatureAlgorithm.HS256, "secretkey").compact();
        return jwt;
    }


}
