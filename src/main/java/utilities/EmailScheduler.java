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
		if(alertType == "start"){
			subject = "Reservation starting";
			message = "Reservation for resource " + reservation.getResource().getName() + " has started";
		}
		else if(alertType == "end"){
			subject = "Reservation ended";
			message = "Reservation for resource " + reservation.getResource().getName() + " has ended";
		}
		else {
			return;
		}
		emailUtility.sendMessage(reservation.getUser().getEmail(), fromString, subject, message);
	}
}
