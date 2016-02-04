package resourceplanner;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiaweizhang on 2/4/16.
 */


@EnableWebMvc
@ControllerAdvice
public class ServiceExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    ResponseEntity<Object> handleControllerException(HttpServletRequest req, Throwable ex) {
        Map<String,Object> responseBody = new HashMap<String, Object>();
        responseBody.put("is_error",true);
        if (ex instanceof SQLException) {
            responseBody.put("message", "Database error");
            return new ResponseEntity<Object>(responseBody,HttpStatus.I_AM_A_TEAPOT);
        } else {
            responseBody.put("message","Internal server error");
            return new ResponseEntity<Object>(responseBody,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String,Object> responseBody = new HashMap<String, Object>();
        responseBody.put("is_error",true);
        responseBody.put("message","Endpoint not found");
        return new ResponseEntity<Object>(responseBody,HttpStatus.NOT_FOUND);
    }
}