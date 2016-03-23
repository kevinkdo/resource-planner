package resourceplanner.reservations;

/**
 * Created by Davis Treybig on 1/23/2016.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import resourceplanner.main.Controller;
import resourceplanner.main.StandardResponse;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import resourceplanner.reservations.ReservationData.*;

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
            return reservationService.createReservation(req, getRequesterID(request));
        }
        else{
            return new StandardResponse(true, "Non-Admin user attempting to make reservation for another user");
        }
    }

    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getReservationById(@PathVariable final int reservationId, final HttpServletRequest request) {
        return reservationService.getReservationById(reservationId, getRequesterID(request));
    }

    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getReservations(@RequestParam(value = "resource_id", required = false) Integer resource_id,
                                                    @RequestParam(value = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                    @RequestParam(value = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                                                    final HttpServletRequest request) {

        //Cannonical way to convert LocalDateTime to Timestamp
        Timestamp startTimestamp = Timestamp.valueOf(start);
        Timestamp endTimestamp = Timestamp.valueOf(end);
        QueryReservationRequest req = new QueryReservationRequest(resource_id, startTimestamp, endTimestamp);
        return reservationService.getReservations(req, getRequesterID(request));
    }

    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.PUT,
            headers = {"Content-type=application/json"})
    @ResponseBody
    public StandardResponse updateReservation(@RequestBody final ReservationRequest req, final HttpServletRequest request,
                                              @PathVariable final int reservationId){
        return reservationService.updateReservation(req, reservationId, hasReservationP(request), getRequesterID(request));
    }


    @RequestMapping(value = "/{reservationId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public StandardResponse deleteReservationById(@PathVariable final int reservationId, final HttpServletRequest request) {
        return reservationService.deleteReservation(reservationId, hasReservationP(request), getRequesterID(request));
    }

    @RequestMapping(value = "/approvableReservations",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getApprovableReservations(final HttpServletRequest request) {
        return reservationService.getApprovableReservations(getRequesterID(request), hasResourceP(request));
    }

    @RequestMapping(value = "/canceledWithApproval/{reservationId}",
            method = RequestMethod.GET)
    @ResponseBody
    public StandardResponse getCanceledWithApproval(@PathVariable final int reservationId, final HttpServletRequest request) {
        return reservationService.getCanceledWithApproval(reservationId);
    }

    @RequestMapping(value = "/approveReservation/{reservationId}",
            method = RequestMethod.POST)
    @ResponseBody
    public StandardResponse approveReservation(@RequestBody final ReservationApproval req, @PathVariable final int reservationId, 
            final HttpServletRequest request) {
        return reservationService.approveReservation(req, reservationId, getRequesterID(request), hasResourceP(request));
    }


}