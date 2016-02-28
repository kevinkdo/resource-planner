package resourceplanner.main;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class Controller {

	@Autowired
    private JdbcTemplate jt;

	public boolean isSuperAdmin(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
        return Boolean.parseBoolean(claims.get("super_p").toString());
	}


	// Returns true if the user himself has resource_p, or if the user is part of a 
	// group with resource_p
	public boolean hasResourceP(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		return Boolean.parseBoolean(claims.get("resource_p").toString()) || Boolean.parseBoolean(claims.get("super_p").toString());
	}

	// Returns true if the user himself has reservation_p, or if the user is part of a 
	// group with reservation_p
	public boolean hasReservationP(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		return Boolean.parseBoolean(claims.get("reservation_p").toString()) || Boolean.parseBoolean(claims.get("super_p").toString());
	}

	// Returns true if the user himself has user_p, or if the user is part of a 
	// group with user permission
	public boolean hasUserP(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
		return Boolean.parseBoolean(claims.get("user_p").toString()) || Boolean.parseBoolean(claims.get("super_p").toString());
	}

	/*
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
	*/


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