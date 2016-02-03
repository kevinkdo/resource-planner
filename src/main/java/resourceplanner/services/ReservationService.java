package resourceplanner.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.*;
import responses.StandardResponse;
import responses.data.Resource;
import responses.data.Resources;
import responses.data.Reservation;
import responses.data.ReservationWithIDs;
import responses.data.ReservationWithIDsData;

import databases.JDBC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import requestdata.GetAllMatchingReservationRequest;
import requestdata.ReservationRequest;
import responses.StandardResponse;
import responses.data.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import resourceplanner.services.ReservationService;
import resourceplanner.controllers.Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Transactional
@Service
public class ReservationService{

	@Autowired
	private UserService userService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private JdbcTemplate jt;

	// Matches all reservations from ANY of the supplied users or ANY of the supplied resources, if
    // they overlap with the start-end times
    public StandardResponse getMatchingReservations(GetAllMatchingReservationRequest req){
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
        getMatchingReservations = getMatchingReservations + "((begin_time <= ? AND begin_time >= ?) OR (end_time <=? AND end_time >= ?) OR (begin_time <= ?  AND end_time >= ?));";

        Connection c = JDBC.connect();
        PreparedStatement st = null;

        try{
            st = c.prepareStatement(getMatchingReservations);
            st.setTimestamp(1, req.getEnd());
            st.setTimestamp(2, req.getStart());
            st.setTimestamp(3, req.getEnd());
            st.setTimestamp(4, req.getStart());
            st.setTimestamp(5, req.getStart());
            st.setTimestamp(6, req.getEnd());
            System.out.println(st);
            ResultSet rs = st.executeQuery();
            
            List<Reservation> reservations = new ArrayList<Reservation>();
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


    public StandardResponse deleteReservationByIdDB(int reservationId){
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


    public StandardResponse createReservationDB(ReservationRequest req){
        if(isOverlappingReservation(req.getBegin_time(), req.getEnd_time(), req.getResource_id())){
            return new StandardResponse(true, "Reservation for that resource overlaps with specified times");
        }
        if(getUserByID(req.getUser_id()) == null){
            return new StandardResponse(true, "User does not exist");
        }
        if(getResourceById(req.getResource_id()) == null){
            return new StandardResponse(true, "Resource does not exist");
        }

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
        	ReservationWithIDsData newReservation = new ReservationWithIDsData(reservation_id, req.getUser_id(), req.getResource_id(),
        		req.getBegin_time(), req.getEnd_time(), req.getShould_email());
            c.close();
        	return new StandardResponse(false, "Reservation inserted successfully", newReservation);
        }
        catch (Exception e){
        	return new StandardResponse(true, "Insert reservation commit failed");
        }

    }

    public StandardResponse updateReservationDB(ReservationRequest req, int reservationId, HttpServletRequest request){
        ReservationWithIDs existingRes = getReservationWithIdsObjectById(reservationId);

        if(req.getUser_id() != null){
            existingRes.setUser_id(req.getUser_id());
        }
        if(req.getResource_id() != null){
            existingRes.setResource_id(req.getResource_id());
        }
        if(req.getBegin_time() != null){
            existingRes.setBegin_time(req.getBegin_time());
        }
        if(req.getEnd_time() != null){
            existingRes.setEnd_time(req.getEnd_time());
        }
        if(req.getShould_email() != null){
            existingRes.setShould_email(req.getShould_email());
        }

        if(!existingRes.getBegin_time().before(existingRes.getEnd_time())){
        	return new StandardResponse(true, "Update causes begin time to occur after end time");
        }

        if(isOverlappingReservation(existingRes.getBegin_time(), existingRes.getEnd_time(), existingRes.getResource_id(), reservationId)){
            return new StandardResponse(true, "Resource is occupied during new reservation time");
        }
        if(getResourceById(existingRes.getResource_id()) == null || getUserByID(existingRes.getUser_id()) == null){
            return new StandardResponse(true, "New user or resource id is not valid");
        }

        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String query = "UPDATE reservations SET user_id = ?, resource_id = ?, begin_time = ?, end_time = ?, should_email = ? WHERE reservation_id = ?;";
        try {
            st = c.prepareStatement(query);
            st.setInt(1, existingRes.getUser_id());
            st.setInt(2, existingRes.getResource_id());
            st.setTimestamp(3, existingRes.getBegin_time());
            st.setTimestamp(4, existingRes.getEnd_time());
            st.setBoolean(5, existingRes.getShould_email());
            st.setInt(6, reservationId);

            int affectedRows = st.executeUpdate();
            if(affectedRows == 1){
            	ReservationWithIDsData reservationToReturn = new ReservationWithIDsData(existingRes);
                return new StandardResponse(false, "Successfully updated reservation", reservationToReturn);
            }
            else{
                return new StandardResponse(true, "Error updating database entry for reservation");
            }
        }
        catch (Exception e){
            return new StandardResponse(true, "Error issuing SQL update");
        }

    }

    private Boolean isOverlappingReservation(Timestamp start, Timestamp end, int resource_id, Integer currentReservation_id){
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String query = "SELECT * FROM reservations WHERE resource_id = ? AND ((? >= begin_time AND ? <= end_time) OR (? >= begin_time AND ? <= end_time) OR (? <= begin_time AND ? >= end_time))";

        if(currentReservation_id != null){
            query = query + " AND reservation_id != ?";
        }
        query = query + ";";

        try {
            st = c.prepareStatement(query);
            st.setInt(1, resource_id);
            st.setTimestamp(2, start);
            st.setTimestamp(3, start);
            st.setTimestamp(4, end);
            st.setTimestamp(5, end);
            st.setTimestamp(6, start);
            st.setTimestamp(7, end);
            if(currentReservation_id != null){
                st.setInt(8, currentReservation_id);
            }

            ResultSet rs = st.executeQuery();
            c.close();
            if(rs.next()){
                return true;
            }
            else{
                return false;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private Boolean isOverlappingReservation(Timestamp start, Timestamp end, int resource_id){
        return isOverlappingReservation(start, end, resource_id, null);
    }

    public StandardResponse getReservationByIdDB(int reservationId) {
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

    public ReservationWithIDs getReservationWithIdsObjectById(int reservationId){
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

    private User getUserByID(int userID){
    	StandardResponse userResponse = userService.getUserById(userID);
    	return (User) userResponse.getData();
    }

    private Resource getResourceById(int resourceId) {
    	StandardResponse resourceResponse = resourceService.getResourceById(resourceId);
    	return (Resource) resourceResponse.getData();
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