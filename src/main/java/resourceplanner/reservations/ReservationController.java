package resourceplanner.reservations;

/**
 * Created by Davis Treybig on 1/23/2016.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import resourceplanner.main.Controller;
import resourceplanner.main.StandardResponse;
import resourceplanner.reservations.ReservationData.ReservationWithIDs;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController extends Controller {

    @Autowired
    private ReservationService reservationService;

    @RequestMapping(value = "/test/{resourceId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse test(@PathVariable final int resourceId, final HttpServletRequest request) {
        return reservationService.reservationTest(resourceId);
    }

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse createReservation(@RequestBody final ReservationRequest req, final HttpServletRequest request){
        //An admin can make a reservation for anyone. A normal user can only make a reservation for himself.
        // Verify that the user_id in the reservation == current user OR the current user is the admin

        if(hasReservationP(request) || getRequesterID(request) == req.getUser_id()){
            return reservationService.createReservation(req);
        }
        else{
            return new StandardResponse(true, "Non-Admin user attempting to make reservation for another user");
        }
    }

    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getReservationById(@PathVariable final int reservationId, final HttpServletRequest request) {
        return reservationService.getReservationById(reservationId);
    }


    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public StandardResponse deleteReservationById(@PathVariable final int reservationId, final HttpServletRequest request) {
        return reservationService.deleteReservation(reservationId, hasReservationP(request), getRequesterID(request));

    }

    /*
	
    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getMatchingReservations(@RequestParam(value = "resource_ids", required = false) Integer[] resource_ids, 
        @RequestParam(value = "user_ids", required = false) Integer[] user_ids,
        @RequestParam(value = "required_tags", required = false) String[] required_tags,
        @RequestParam(value = "excluded_tags", required = false) String[] excluded_tags,
        @RequestParam(value = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam(value = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        final HttpServletRequest request) {

        //Cannonical way to convert LocalDateTime to Timestamp
        Timestamp startTimestamp = Timestamp.valueOf(start);
        Timestamp endTimestamp = Timestamp.valueOf(end);
        GetAllMatchingReservationRequest req = new GetAllMatchingReservationRequest(resource_ids, user_ids, 
            required_tags, excluded_tags, startTimestamp, endTimestamp);
        if(req.isValid()){
            return reservationService.getMatchingReservations(req, getRequesterID(request));
        }
        else{
            return new StandardResponse(true, "Invalid input parameters (Issue with start and end times)");
        }
    }

    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateReservation(@RequestBody final ReservationRequest req, final HttpServletRequest request,
            @PathVariable final int reservationId){
        ReservationWithIDs existingRes = reservationService.getReservationWithIdsObjectById(reservationId);
        if(existingRes == null){
            return new StandardResponse(true, "No reservation with given ID exists");
        }
        if(getRequesterID(request) != existingRes.getUser_id() && !hasReservationP(request)){
            return new StandardResponse(true, "Non-admin trying to alter another user's reservation");
        }

        return reservationService.updateReservationDB(req, reservationId, request);
    }

    */

}