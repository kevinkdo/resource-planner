package resourceplanner.services;

import databases.JDBC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import requestdata.GetAllMatchingReservationRequest;
import requestdata.ReservationRequest;
import responses.StandardResponse;
import responses.data.*;
import org.springframework.jdbc.core.RowMapper;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import resourceplanner.services.ReservationService;
import resourceplanner.controllers.Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import utilities.EmailScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import utilities.TimeUtility;

import java.util.concurrent.ScheduledFuture;
import java.text.SimpleDateFormat;


/**
 * Created by Davis Treybig
 */

@Transactional
@Service
public class EmailService {
	private ConcurrentTaskScheduler concurrentTaskScheduler = new ConcurrentTaskScheduler();
	private Map<Integer, List<ScheduledFuture>> scheduledEmailMap = new HashMap<Integer, List<ScheduledFuture>>();

	@Autowired
	private ReservationService reservationService;


	public void rescheduleEmails(ReservationWithIDsData reservation){
    	removeScheduledEmails(reservation.getReservation_id());
    	scheduleEmailUpdate(reservation);
    }


    public void removeScheduledEmails(int reservationId){
    	if(scheduledEmailMap.containsKey(reservationId)){
    		List<ScheduledFuture> futures = scheduledEmailMap.get(reservationId);
    		for(ScheduledFuture f : futures){
    			f.cancel(true);
    		}
    		scheduledEmailMap.remove(reservationId);
    	}
    }

    public void scheduleEmailUpdate(ReservationWithIDsData res){
    	Reservation completeReservation = reservationService.getReservationObjectById(res.getReservation_id());

    	if(completeReservation.getShould_email() && completeReservation.getUser().isShould_email()){
    		EmailScheduler startReservationEmailScheduler = new EmailScheduler(completeReservation, EmailScheduler.BEGIN_ALERT);
			EmailScheduler endReservationEmailScheduler = new EmailScheduler(completeReservation, EmailScheduler.END_ALERT);

			/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date dateBeginGWT = sdf.parse(completeReservation.getBegin_time(), new java.text.ParsePosition(0));
			Date dateEndGWT = sdf.parse(completeReservation.getEnd_time(), new java.text.ParsePosition(0));


			//Have to adjust dates to not be in GWT
			Date dateBegin = new Date(dateBeginGWT.getTime() + 5 * 3600 * 1000);
			Date dateEnd = new Date(dateEndGWT.getTime() + 5 * 3600 * 1000);
			*/

			System.out.println("Begin time: "+completeReservation.getBegin_time());
			System.out.println("End   time: "+completeReservation.getEnd_time());

			Timestamp beginTimestamp = TimeUtility.stringToTimestamp(completeReservation.getBegin_time());
			Timestamp endTimestamp = TimeUtility.stringToTimestamp(completeReservation.getEnd_time());
			Date dateBegin = new Date(beginTimestamp.getTime());
			Date dateEnd = new Date(endTimestamp.getTime());

			System.out.println("Beg time: "+dateBegin);
			System.out.println("End time: "+dateEnd);

			if(!verifyDateInFuture(dateBegin)){
				return;
			}

			ScheduledFuture beginEmail = concurrentTaskScheduler.schedule(startReservationEmailScheduler, dateBegin);
			ScheduledFuture endEmail = concurrentTaskScheduler.schedule(endReservationEmailScheduler, dateEnd);
    		
    		if(scheduledEmailMap.containsKey(completeReservation.getReservation_id())){
    			List<ScheduledFuture> existingFutures = scheduledEmailMap.get(completeReservation.getReservation_id());
    			for (ScheduledFuture f : existingFutures){
    				f.cancel(true);
    			}
    			existingFutures = new ArrayList<ScheduledFuture>();
    			existingFutures.add(beginEmail);
    			existingFutures.add(endEmail);
    		}
    		else{
    			List<ScheduledFuture> newFutures = new ArrayList<ScheduledFuture>();
    			newFutures.add(beginEmail);
    			newFutures.add(endEmail);
    			scheduledEmailMap.put(completeReservation.getReservation_id(), newFutures);
    		}
    	}
    }

    private boolean verifyDateInFuture(Date date){
		Date currentDate = new Date();
		return currentDate.before(date);
    }

    public void upateEmailAfterUserChange(int userId){
    	List<Integer> reservationIds = reservationService.getReservationsOfUser(userId);
    	List<ReservationWithIDsData> reservations = new ArrayList<ReservationWithIDsData>();
    	for(int i = 0; i < reservationIds.size(); i++){
    		reservations.add(reservationService.getReservationWithIDsDataObjectById(reservationIds.get(i)));
    	}
    	for(ReservationWithIDsData r : reservations){
    		rescheduleEmails(r);
    	}
    }

    public void cancelEmailsForReservationsOfUser(int userId){
    	List<Integer> reservationIds = reservationService.getReservationsOfUser(userId);
    	for(int i = 0; i < reservationIds.size(); i++){
    		removeScheduledEmails(reservationIds.get(i));
    	}
    }

    public void cancelEmailsForReservationsWithResource(int resourceId){
    	List<Integer> reservationIds = reservationService.getReservationsWithResource(resourceId);
    	for(int i = 0; i < reservationIds.size(); i++){
    		removeScheduledEmails(reservationIds.get(i));
    	}
    }
}