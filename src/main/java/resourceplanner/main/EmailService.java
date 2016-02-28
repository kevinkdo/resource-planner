package resourceplanner.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.reservations.ReservationService;
import resourceplanner.reservations.ReservationData.Reservation;
import resourceplanner.reservations.ReservationData.ReservationWithIDsData;
import utilities.EmailScheduler;
import utilities.TimeUtility;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ScheduledFuture;


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