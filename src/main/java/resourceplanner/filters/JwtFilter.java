package resourceplanner.filters;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            createResponse(res, "Missing Authorization header");
            return;
        }
        else if (!authHeader.startsWith("Bearer ")) {
            createResponse(res, "Invalid Authorization header");
            return;
        }
        final String token = authHeader.substring(7);
        try {
            final Claims claims = Jwts.parser().setSigningKey("secretkey")
                    .parseClaimsJws(token).getBody();
            request.setAttribute("claims", claims);
        }
        catch (Exception f) {
            createResponse(res, "Invalid token");
            return;
        }

        chain.doFilter(req, res);
    }

    private void createResponse(ServletResponse res, String message) {
        try {
            PrintWriter out = res.getWriter();
            CharArrayWriter caw = new CharArrayWriter();
            caw.write(ExceptionCreator.createJson(message));
            res.setContentType("application/json;charset=UTF-8"); // must be before close
            res.setContentLength(caw.toString().getBytes().length);
            out.write(caw.toString());
            out.close();
        } catch (IOException e) {
            return;
        }
    }
}