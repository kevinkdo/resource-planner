package resourceplanner;

import org.postgresql.util.PSQLException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import responses.StandardResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by jiaweizhang on 2/4/16.
 */

@ControllerAdvice
public class ServiceExceptionHandler {
    @ExceptionHandler(PSQLException.class)
    @ResponseBody
    public StandardResponse psqlException(Exception ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new StandardResponse(true, "Database error");
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    @ResponseBody
    public StandardResponse sqlException(Exception ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new StandardResponse(true, "Database error");
    }


}

/*
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
*/