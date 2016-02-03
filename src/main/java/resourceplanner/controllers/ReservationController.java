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
import responses.data.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import resourceplanner.services.ReservationService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController extends Controller{

    @Autowired
    private ReservationService reservationService;
	
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
            return reservationService.getMatchingReservations(req);
        }
        else{
            return new StandardResponse(true, "Invalid input parameters (Issue with start and end times");
        }
    }


	@RequestMapping(value = "/{reservationId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getReservationById(@PathVariable final int reservationId) {
        return reservationService.getReservationByIdDB(reservationId);
    }

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createReservation(@RequestBody final ReservationRequest req, final HttpServletRequest request){
    	//An admin can make a reservation for anyone. A normal user can only make a reservation for himself. 
    	// Verify that the user_id in the reservation == current user OR the current user is the admin
        if(!req.isValidCreateRequest()){
            return new StandardResponse(true, "Begin time after end time");
        }

    	if(isAdmin(request) || getRequesterID(request) == req.getUser_id()){
    		return reservationService.createReservationDB(req);
    	}
    	else{
    		return new StandardResponse(true, "Non-Admin user attempting to make reservation for another user");
    	}
    }

    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateReservation(@RequestBody final ReservationRequest req, final HttpServletRequest request,
            @PathVariable final int reservationId){
        return reservationService.updateReservationDB(req, reservationId, request);
    }

    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public StandardResponse deleteReservationById(@PathVariable final int reservationId, final HttpServletRequest request) {
    	//We must do an initial get to check for the user of the reservation.    
        ReservationWithIDs reservation = reservationService.getReservationWithIdsObjectById(reservationId);
        //Admin can delete ANY reservation, user can only delete his/her own
    	if(isAdmin(request) || getRequesterID(request) == reservation.getUser_id()){
    		return reservationService.deleteReservationByIdDB(reservationId);
    	}
    	else{
    		return new StandardResponse(true, "Non-Admin user attempting to delete reservation for another user");
    	}
    }
}