package resourceplanner.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import utilities.ExceptionCreator;

/**
 * Created by jiaweizhang on 1/17/2016.
 */
public class JwtFilter extends GenericFilterBean {
    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        System.out.println("JwtFilter");
        final HttpServletRequest request = (HttpServletRequest) req;
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(ExceptionCreator.createJson("Missing Authorization header."));
            return;
        }
        if (!authHeader.startsWith("Bearer ")) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(ExceptionCreator.createJson("Invalid Authorization header."));
            return;
        }
        final String token = authHeader.substring(7);
        try {
            final Claims claims = Jwts.parser().setSigningKey("secretkey")
                    .parseClaimsJws(token).getBody();
            request.setAttribute("claims", claims);
        }
        catch (final SignatureException e) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(ExceptionCreator.createJson("Invalid token."));
            return;
        } catch (Exception f) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(ExceptionCreator.createJson("Invalid token."));
            return;
        }
        chain.doFilter(req, res);
    }

}