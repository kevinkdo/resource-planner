package resourceplanner.controllers;

import io.jsonwebtoken.Claims;
import org.postgresql.util.PSQLException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import responses.StandardResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Controller {
	public boolean isAdmin(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		if (claims.get("permission") == null) {
			return false;
		}
        int permission = Integer.parseInt(claims.get("permission").toString());
        return permission == 1;
	}

	public int getRequesterID(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		if (claims.get("user_id") == null) {
			return 0; // TODO decide what to do
		}
    	//Verify that the user_id in the request == current user
    	int requesterID = Integer.parseInt(claims.get("user_id").toString());
    	return requesterID;
	}

	/*
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

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public StandardResponse exception(Exception ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		return new StandardResponse(true, "Internal server error");
	}
	*/
}