package utilities;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import resourceplanner.authentication.AuthUser;

/**
 * Created by jiaweizhang on 1/17/2016.
 */
public class TokenCreator {
    public static String generateToken(AuthUser user, String username) {
        String jwt = Jwts.builder().setSubject(username)
                .claim("user_id", user.getUser_id())
                .claim("super_p", user.isSuper_p())
                .claim("resource_p", user.isResource_p())
                .claim("reservation_p", user.isReservation_p())
                .claim("user_p", user.isUser_p())
                .signWith(SignatureAlgorithm.HS256, "secretkey").compact();
        return jwt;
    }


}
