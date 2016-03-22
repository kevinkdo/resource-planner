package resourceplanner.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.reservations.ReservationService;
import resourceplanner.reservations.ReservationData.Reservation;
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


	public void rescheduleEmails(int reservationId){
    	removeScheduledEmails(reservationId);
    	scheduleEmail(reservationId);
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

    public void scheduleEmail(int reservationId){
		Reservation res = reservationService.getReservationByIdHelper(reservationId, 1);


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