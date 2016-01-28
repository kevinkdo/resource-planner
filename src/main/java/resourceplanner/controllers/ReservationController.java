package resourceplanner.controllers;

/**
 * Created by Davis Treybig on 1/23/2016.
 */

import databases.JDBC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import requestdata.GetAllMatchingReservationRequest;
import requestdata.ReservationRequest;
import responses.StandardResponse;
import responses.data.Reservation;
import responses.data.ReservationWithIDs;
import responses.data.Resource;
import responses.data.User;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController extends Controller{
	
    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getMatchingReservations(@RequestParam(value = "resource_ids", required = false) Integer[] resource_ids, 
        @RequestParam(value = "user_ids", required = false) Integer[] user_ids,
        @RequestParam(value = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam(value = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        //Cannonical way to convert LocalDateTime to Timestamp
        Timestamp startTimestamp = Timestamp.valueOf(start);
        Timestamp endTimestamp = Timestamp.valueOf(end);

        GetAllMatchingReservationRequest req = new GetAllMatchingReservationRequest(resource_ids, user_ids, startTimestamp, endTimestamp);
        if(req.isValid()){
            return getMatchingReservations(req);
        }
        else{
            return new StandardResponse(true, "Invalid input parameters (Issue with start and end times");
        }
    }


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
    	//An admin can make a reservation for anyone. A normal user can only make a reservation for himself. 
    	// Verify that the user_id in the reservation == current user OR the current user is the admin
    	if(isAdmin(request) || getRequesterID(request) == req.getUser_id()){
    		return createReservationDB(req);
    	}
    	else{
    		return new StandardResponse(true, "Non-Admin user attempting to make reservation for another user");
    	}
    }

    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public StandardResponse deleteReservationById(@PathVariable final int reservationId, final HttpServletRequest request) {
    	//We must do an initial get to check for the user of the reservation.    
        ReservationWithIDs reservation = getReservationWithIdsObjectById(reservationId);
        //Admin can delete ANY reservation, user can only delete his/her own
    	if(isAdmin(request) || getRequesterID(request) == reservation.getUser_id()){
    		return deleteReservationByIdDB(reservationId);
    	}
    	else{
    		return new StandardResponse(true, "Non-Admin user attempting to delete reservation for another user");
    	}
    }

    // Matches all reservations from ANY of the supplied users or ANY of the supplied resources, if
    // they overlap with the start-end times
    private StandardResponse getMatchingReservations(GetAllMatchingReservationRequest req){
        String getMatchingReservations = "SELECT * FROM reservations WHERE ";

        //If any query parameters were specified, we need to limit our query to those resources and users
        if(req.matchOnIds()){
            getMatchingReservations = appendIDMatchString(getMatchingReservations, req);
            getMatchingReservations = getMatchingReservations + "AND ";
        }

        Timestamp startTime = req.getStart();
        Timestamp endTime = req.getEnd();

        //Two cases where it overlaps. 
        //1. Start time between other reservations start/end
        //2. End time between other reservations start/end
        getMatchingReservations = getMatchingReservations + "((begin_time < ? AND begin_time > ?) OR (end_time < ? AND end_time > ?));";
        System.out.println(getMatchingReservations);

        Connection c = JDBC.connect();
        PreparedStatement st = null;

        try{
            st = c.prepareStatement(getMatchingReservations);
            st.setTimestamp(1, req.getEnd());
            st.setTimestamp(2, req.getStart());
            st.setTimestamp(3, req.getEnd());
            st.setTimestamp(4, req.getStart());
            System.out.println("Start " + req.getStart());
            System.out.println("End " + req.getEnd());
            System.out.println(st);
            ResultSet rs = st.executeQuery();
            
            List<Reservation> reservations = new ArrayList<Reservation>();
            System.out.println("Got here");
            while (rs.next()){
                ReservationWithIDs newRes = extractReservationFromResultSet(rs);
                if(newRes == null){
                    return new StandardResponse(true, "Error parsing retrieved reservations");
                }
                User user = getUserByID(newRes.getUser_id());
                Resource resource = getResourceById(newRes.getResource_id());
                reservations.add(new Reservation(newRes, user, resource));
            }
            c.close();
            return new StandardResponse(false, "Matching reservations retrieved", reservations);
        }
        catch(Exception e){
            return new StandardResponse(true, "Failed to retrieve reservations. SQL Query failed." + e);
        }

    }

    private String appendIDMatchString(String baseQueryString, GetAllMatchingReservationRequest req){
        baseQueryString = baseQueryString + "(";

        Integer[] resource_ids = req.getResource_ids();
        Integer[] user_ids = req.getUser_ids();
        //First do an OR over every resource_ID to see if reservation matches
        if(resource_ids != null){
            baseQueryString = baseQueryString + "(";
            for (int i = 0; i < resource_ids.length; i++){
                baseQueryString = baseQueryString + "resource_id = " + resource_ids[i];
                if (i != resource_ids.length - 1){
                    baseQueryString = baseQueryString + " OR ";
                }
                else{
                    baseQueryString = baseQueryString + ")";
                }
            }
        }
        
        //Next, do an OR over every user_id to see if reservation matches
        if(user_ids != null){
            if(resource_ids != null){
                baseQueryString = baseQueryString + " OR (";
            }
            else{
                baseQueryString = baseQueryString + "(";
            }
            for (int i = 0; i < user_ids.length; i++){
                baseQueryString = baseQueryString + "user_id = " + user_ids[i];
                if (i != user_ids.length - 1){
                    baseQueryString = baseQueryString + " OR ";
                }
                else{
                    baseQueryString = baseQueryString + ")";
                }
            }
        }
        baseQueryString = baseQueryString + ")";
        return baseQueryString;
    }


    private StandardResponse deleteReservationByIdDB(int reservationId){
    	Connection c = JDBC.connect();
    	PreparedStatement st = null;
    	String deleteReservation = "DELETE FROM reservations WHERE reservation_id = ?;";
    	try{
    		st = c.prepareStatement(deleteReservation);
    		st.setInt(1, reservationId);
    		int affectedRows = st.executeUpdate();
    		if(affectedRows == 0){
    			return new StandardResponse(true, "No reservation with that ID exists");
    		}
            c.close();
    		return new StandardResponse(false, "Reservation successfully deleted");
    	}
    	catch(Exception e){
    		return new StandardResponse(true, "Failed to delete reservation");
    	}
    }


    private StandardResponse createReservationDB(ReservationRequest req){
    	//****NEED TO ADD OVERLAP CHECK***///
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
            c.close();
        	return new StandardResponse(false, "Reservation inserted successfully", newReservation);
        }
        catch (Exception e){
        	return new StandardResponse(true, "Insert reservation commit failed");
        }
        

    }

    private StandardResponse getReservationByIdDB(int reservationId) {
    	Reservation reservation = getReservationObjectById(reservationId);
    	if (reservation == null){
    		return new StandardResponse(true, "Reservation with given ID not found");
    	}
    	else{
    		return new StandardResponse(false, "Reservation with given ID found", reservation);
    	}
    }

    private Reservation getReservationObjectById(int reservationId){
    	Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectResourcesQuery = "SELECT * FROM reservations WHERE reservation_id = ?;";
        ReservationWithIDs matchingReservation;

        try {
            st = c.prepareStatement(selectResourcesQuery);
            st.setInt(1, reservationId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                matchingReservation = extractReservationFromResultSet(rs);
            } else {
                return null;
            }
            c.close();
        } catch (Exception f) {
            return null;
        }


        User user = getUserByID(matchingReservation.getUser_id());
        Resource resource = getResourceById(matchingReservation.getResource_id());
        if(user == null || resource == null){
        	return null;
        }

        Reservation reservation = new Reservation(matchingReservation, user, resource);
        return reservation;
    }

    private ReservationWithIDs getReservationWithIdsObjectById(int reservationId){
    	Connection c = JDBC.connect();
        PreparedStatement st = null;
        String selectResourcesQuery = "SELECT * FROM reservations WHERE reservation_id = ?;";
        ReservationWithIDs matchingReservation;

        try {
            st = c.prepareStatement(selectResourcesQuery);
            st.setInt(1, reservationId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                matchingReservation = extractReservationFromResultSet(rs);
            } else {
                return null;
            }
            c.close();
        } catch (Exception f) {
            return null;
        }
        return matchingReservation;
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
            c.close();
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
            c.close();
        } catch (Exception f) {
            return null;
        }
        return new Resource(resourceId, name, description, tags);
    }

    private ReservationWithIDs extractReservationFromResultSet(ResultSet rs){
        try{
            int reservation_id = rs.getInt("reservation_id");
            int user_id = rs.getInt("user_id");
            int resource_id = rs.getInt("resource_id");
            Timestamp begin_time = rs.getTimestamp("begin_time");
            Timestamp end_time = rs.getTimestamp("end_time");
            boolean should_email = rs.getBoolean("should_email");

            ReservationWithIDs reservation = new ReservationWithIDs(reservation_id, user_id, resource_id, begin_time, end_time, 
                should_email);
            return reservation;
        }
        catch(Exception e){
            return null;
        }
        
    }
}