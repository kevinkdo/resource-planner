package resourceplanner.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.reservations.ReservationData.Reservation;
import resourceplanner.reservations.ReservationService;
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


    private void scheduleIncompleteEmail(Reservation res){
        EmailScheduler pendingEmail = new EmailScheduler(res, EmailScheduler.INCOMPLETE_PENDING_ALERT);
        EmailScheduler neverApprovedEmail = new EmailScheduler(res, EmailScheduler.INCOMPLETE_NEVER_APPROVED_ALERT);

        Timestamp beginTimestamp = TimeUtility.stringToTimestamp(res.getBegin_time());
        Date dateBegin = new Date(beginTimestamp.getTime());
        Timestamp pendingTimestamp = new Timestamp(beginTimestamp.getTime() - 10*60*1000);
        Date datePending = new Date(pendingTimestamp.getTime());

        if(!verifyDateInFuture(dateBegin)){
            return;
        }

        ScheduledFuture scheduledPendingEmail = concurrentTaskScheduler.schedule(pendingEmail, datePending);
        ScheduledFuture scheduledNeverApprovedEmail = concurrentTaskScheduler.schedule(neverApprovedEmail, dateBegin);

        if(scheduledEmailMap.containsKey(res.getReservation_id())){
            List<ScheduledFuture> existingFutures = scheduledEmailMap.get(res.getReservation_id());
            for (ScheduledFuture f : existingFutures){
                f.cancel(true);
            }
            existingFutures = new ArrayList<ScheduledFuture>();
            existingFutures.add(scheduledPendingEmail);
            existingFutures.add(scheduledNeverApprovedEmail);
        }
        else{
            List<ScheduledFuture> newFutures = new ArrayList<ScheduledFuture>();
            newFutures.add(scheduledPendingEmail);
            newFutures.add(scheduledNeverApprovedEmail);
            scheduledEmailMap.put(res.getReservation_id(), newFutures);
        }
    }


    public void sendDeniedEmail(int reservationId){
        Reservation res = reservationService.getReservationByIdAdmin(reservationId);
        if(res.getShould_email() && res.getUser().isShould_email()){
            EmailScheduler denyEmailScheduler = new EmailScheduler(res, EmailScheduler.DENY_ALERT);
            denyEmailScheduler.run();
        }  
    }

    public void sendCanceledEmail(int reservationId){
        Reservation res = reservationService.getReservationByIdAdmin(reservationId);
        if(res.getShould_email() && res.getUser().isShould_email()){
            EmailScheduler cancelEmailScheduler = new EmailScheduler(res, EmailScheduler.CANCEL_ALERT);
            cancelEmailScheduler.run();
        }
    }


	public void scheduleEmails(int reservationId){
        Reservation res = reservationService.getReservationByIdAdmin(reservationId);
        if(res.getComplete()){
            scheduleEmail(res);
        }
    	else{
            scheduleIncompleteEmail(res);
        }
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

    private void scheduleEmail(Reservation res){
    	if(res.getShould_email() && res.getUser().isShould_email()){
    		EmailScheduler startReservationEmailScheduler = new EmailScheduler(res, EmailScheduler.BEGIN_ALERT);
			EmailScheduler endReservationEmailScheduler = new EmailScheduler(res, EmailScheduler.END_ALERT);

			System.out.println("Begin time: "+res.getBegin_time());
			System.out.println("End   time: "+res.getEnd_time());

			Timestamp beginTimestamp = TimeUtility.stringToTimestamp(res.getBegin_time());
			Timestamp endTimestamp = TimeUtility.stringToTimestamp(res.getEnd_time());
			Date dateBegin = new Date(beginTimestamp.getTime());
			Date dateEnd = new Date(endTimestamp.getTime());

			System.out.println("Beg time: "+dateBegin);
			System.out.println("End time: "+dateEnd);

			if(!verifyDateInFuture(dateBegin)){
				return;
			}

			ScheduledFuture beginEmail = concurrentTaskScheduler.schedule(startReservationEmailScheduler, dateBegin);
			ScheduledFuture endEmail = concurrentTaskScheduler.schedule(endReservationEmailScheduler, dateEnd);
    		
    		if(scheduledEmailMap.containsKey(res.getReservation_id())){
    			List<ScheduledFuture> existingFutures = scheduledEmailMap.get(res.getReservation_id());
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
    			scheduledEmailMap.put(res.getReservation_id(), newFutures);
    		}
    	}
    }

    private boolean verifyDateInFuture(Date date){
		Date currentDate = new Date();
		return currentDate.before(date);
    }
}