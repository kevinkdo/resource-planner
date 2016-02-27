package resourceplanner.controllers;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;

public class Controller {

	public boolean isSuperAdmin(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
        return Boolean.parseBoolean(claims.get("super_p").toString());
	}


	public boolean hasResourceP(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		return Boolean.parseBoolean(claims.get("resource_p").toString());
	}

	public boolean hasReservationP(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		return Boolean.parseBoolean(claims.get("reservation_p").toString());
	}

	public boolean hasUserP(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		return Boolean.parseBoolean(claims.get("user_p").toString());
	}


	public int getRequesterID(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		if (claims.get("user_id") == null) {
			return 0; // TODO decide what to do
		}
    	//Verify that the user_id in the request == current user
		return Integer.parseInt(claims.get("user_id").toString());
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