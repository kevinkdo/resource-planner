package resourceplanner.controllers;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;

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
}