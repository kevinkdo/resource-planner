package utilities;
import utilities.EmailUtility;
import responses.data.ReservationWithIDsData;
import responses.data.Reservation;

/**
 * Created by Davis Treybig
 */

public class EmailScheduler implements Runnable{
	private EmailUtility emailUtility = new EmailUtility();
	private Reservation reservation;
	private String alertType;

	public static final String BEGIN_ALERT = "begin";
	public static final String END_ALERT = "end";

	private static final String fromString = "ResourceManagerAlerts@gmail.com";


	public EmailScheduler(Reservation reservation, String alertType){
		this.reservation = reservation;
		this.alertType = alertType;	
	}


	public void run(){
		String subject;
		String message;
		String beginTime = TimeUtility.prettyEST(reservation.getBegin_time());
		String endTime = TimeUtility.prettyEST(reservation.getEnd_time());
		if(alertType == BEGIN_ALERT){
			subject = "Reservation starting";
			message = "Hi,\n\nYour reservation for resource '" + reservation.getResource().getName() + "' on "+ beginTime +" has started.\n\nThanks,\nResource Manager";
			System.out.println("begin email being sent");
		}
		else if(alertType == END_ALERT){
			subject = "Reservation ended";
			message = "Hi,\n\nReservation for resource '" + reservation.getResource().getName() + "' on "+ endTime +" has ended.\n\nThanks,\nResource Manager";
			System.out.println("end email being sent");
		}
		else {
			return;
		}
		emailUtility.sendMessage(reservation.getUser().getEmail(), fromString, subject, message);
	}
}
