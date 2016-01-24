package resourceplanner.controllers;

/**
 * Created by Davis Treybig on 1/23/2016.
 */

import databases.JDBC;
import org.springframework.web.bind.annotation.*;
import requestdata.UserRequest;
import responses.StandardResponse;
import responses.data.Token;
import utilities.PasswordHash;
import utilities.TokenCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.jsonwebtoken.Claims;
import requestdata.*;
import responses.StandardResponse;
import responses.data.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController{
	
	@RequestMapping(value = "/{reservationId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getReservationById(@PathVariable final int reservationId) {
        return getReservationByIdDB(reservationId);
    }

    // Anyone can create reservation. They specify resource, start time, end time. Must check it does
    // not overlap with another reservation
    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createReservation(@RequestBody final ReservationRequest req, final HttpServletRequest request){
    	final Claims claims = (Claims) request.getAttribute("claims");
    	//Verify that the user_id in the requesrt == current user
    	int userId = Integer.parseInt(claims.get("user_id").toString());
    	int reservationUserId = req.getUser_id();
    	if(userId != reservationUserId){
    		return new StandardResponse(true, "User attempting to make reservation for another user");
    	}
    	else{
    		return createReservationDB(req);
    	}
    }

    private StandardResponse createReservationDB(ReservationRequest req){
    	Connection c = JDBC.connect();
        try {
            c.setAutoCommit(false);
        } catch (SQLException e) {
            return new StandardResponse(true, "failed to disable autocommit", req);
        }
        PreparedStatement st = null;
        String reservationsInsert = "INSERT INTO reservations (user_id, resource_id, begin_time, end_time, should_email) VALUES (?, ?, ?, ?, ?);";
		int reservation_id;
        try {
            st = c.prepareStatement(reservationsInsert, Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, req.getUser_id());
            st.setInt(2, req.getResource_id());
            st.setTimestamp(3, req.getBegin_time());
            st.setTimestamp(4, req.getEnd_time());
            st.setBoolean(5, req.getShould_email());
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                return new StandardResponse(true, "No new reservation created");
            }
        } catch (Exception e) {
            return new StandardResponse(true, "Failed to add reservation");
        }

        try {
            ResultSet generatedKeys = st.getGeneratedKeys();
            if (generatedKeys.next()) {
                reservation_id = generatedKeys.getInt(1);
            } else {
                return new StandardResponse(true, "Creating reservation failed, no ID obtained.");
            }
        } catch (Exception e) {
            return new StandardResponse(true, "insert reservation failed - no ID obtained.");
        }

        try{
        	c.commit();
        	ReservationWithIDs newReservation = new ReservationWithIDs(reservation_id, req.getUser_id(), req.getResource_id(),
        		req.getBegin_time(), req.getEnd_time(), req.getShould_email());
        	return new StandardResponse(false, "Reservation inserted successfully", newReservation);
        }
        catch (Exception e){
        	return new StandardResponse(true, "Insert reservation commit failed");
        }
        

    }

    private StandardResponse getReservationByIdDB(int reservationId) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectResourcesQuery = "SELECT * FROM reservations WHERE reservation_id = ?;";
        int reservation_id, user_id, resource_id;
        Timestamp begin_time, end_time;
        boolean should_email;

        try {
            st = c.prepareStatement(selectResourcesQuery);
            st.setInt(1, reservationId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                reservation_id = rs.getInt("reservation_id");
                user_id = rs.getInt("user_id");
                resource_id = rs.getInt("resource_id");
                begin_time = rs.getTimestamp("begin_time");
                end_time = rs.getTimestamp("end_time");
                should_email = rs.getBoolean("should_email");
            } else {
                return new StandardResponse(true, "No reservation found with given id");
            }
        } catch (Exception f) {
            return new StandardResponse(true, "No reservation found with given id 2");
        }

        User user = getUserByID(user_id);
        Resource resource = getResourceById(resource_id);
        Reservation reservation = new Reservation(reservation_id, user, resource, begin_time, end_time,
        	should_email);


        return new StandardResponse(false, "success", null, reservation);
    }

    public User getUserByID(int userID){
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectUsersQuery = "SELECT email, username, should_email FROM users WHERE user_id = ?;";
        String email;
        String username;
        boolean should_email;

        try {
            st = c.prepareStatement(selectUsersQuery);
            st.setInt(1, userID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                email = rs.getString("email");
                username = rs.getString("username");
                should_email = rs.getBoolean("should_email");
            } else {
                return null;
            }
        } catch (Exception f) {
            return null;
        }

        return new User(email, username, should_email);
    }

    private Resource getResourceById(int resourceId) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectResourcesQuery = "SELECT name, description FROM resources WHERE resource_id = ?;";
        String selectResourceTagsQuery = "SELECT tag FROM resourcetags WHERE resource_id = ?";
        String name;
        String description;
        List<String> tags = new ArrayList<String>();
        try {
            st = c.prepareStatement(selectResourcesQuery);
            st.setInt(1, resourceId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
                description = rs.getString("description");
            } else {
                return null;
            }
        } catch (Exception f) {
            return null;
        }

        try {
            st = c.prepareStatement(selectResourceTagsQuery);
            st.setInt(1, resourceId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String tag = rs.getString("tag");
                tags.add(tag);
            }
        } catch (Exception f) {
            return null;
        }

        return new Resource(resourceId, name, description, tags);
    }
}