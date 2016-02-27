package resourceplanner.controllers;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class Controller {

	@Autowired
    private JdbcTemplate jt;

	public boolean isSuperAdmin(HttpServletRequest request){
		int userId = getRequesterID(request);
		List<Boolean> superAdmin = jt.query(
                "SELECT super_p FROM users WHERE user_id = " + userId + 
                " AND super_p = true;",
                new RowMapper<Boolean>() {
                    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getBoolean("super_p");
                    }
                });

		return superAdmin.size() > 0;
	}


	// Returns true if the user himself has resource_p, or if the user is part of a 
	// group with resource_p
	public boolean hasResourceP(HttpServletRequest request){
		int userId = getRequesterID(request);
		return hasSpecificPermission(userId, "resource_p");
	}

	// Returns true if the user himself has reservation_p, or if the user is part of a 
	// group with reservation_p
	public boolean hasReservationP(HttpServletRequest request){
		int userId = getRequesterID(request);
		return hasSpecificPermission(userId, "reservation_p");
	}

	// Returns true if the user himself has user_p, or if the user is part of a 
	// group with user permission
	public boolean hasUserP(HttpServletRequest request){
		int userId = getRequesterID(request);
		return hasSpecificPermission(userId, "user_p");
	}

	private boolean hasSpecificPermission(int userId, String permissionType){
		List<Boolean> individualPermission = jt.query(
                "SELECT " + permissionType + " FROM users WHERE user_id = " + userId + 
                " AND " + permissionType + " = true;",
                new RowMapper<Boolean>() {
                    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getBoolean(permissionType);
                    }
                });


		List<Boolean> groupPermission = jt.query(
                "SELECT " + permissionType + " FROM groups, groupmembers WHERE user_id = " + userId + 
                " AND groups.group_id = groupmembers.group_id AND " + permissionType +  " = true;",
                new RowMapper<Boolean>() {
                    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getBoolean("user_p");
                    }
                });

		return individualPermission.size() > 0 || groupPermission.size() > 0;
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