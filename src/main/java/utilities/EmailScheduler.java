package utilities;

import resourceplanner.reservations.ReservationData.Reservation;
import resourceplanner.resources.ResourceData.Resource;

import java.util.List;

/**
 * Created by Davis Treybig
 */

//@Configuration
//@PropertySource("classpath:config.properties")
public class EmailScheduler implements Runnable{
	private EmailUtility emailUtility = new EmailUtility();
	private Reservation reservation;
	private String alertType;

	public static final String BEGIN_ALERT = "begin";
	public static final String END_ALERT = "end";

	//@Value("${fromString}")
	private String fromString ="ResourceManagerAlerts@gmail.com";

	public EmailScheduler(Reservation reservation, String alertType){
		this.reservation = reservation;
		this.alertType = alertType;	
	}


	public void run(){

		String subject;
		String message;
		String beginTime = TimeUtility.prettyEST(reservation.getBegin_time());
		String endTime = TimeUtility.prettyEST(reservation.getEnd_time());

		List<Resource> rList = reservation.getResources();
		String rString = "";
		for (int i=0; i<rList.size(); i++) {
			rString += rList.get(i).getName() + ", ";
		}
		rString = rString.substring(0, rString.length()-2);

		if((alertType != null) && (alertType.equals(BEGIN_ALERT))){
			subject = "Reservation starting";
			message = "Hi,\n\nThe following reservation is starting: \n\nTitle: "+reservation.getTitle()+"\nDescription: "+reservation.getDescription()+ "\nResources: " + rString + "\nStart Time: "+ beginTime +"\n\nThanks,\nResource Manager";
			System.out.println("begin email being sent");
		}
		else if((alertType != null) && (alertType.equals(END_ALERT))){
			subject = "Reservation ended";
			message = "Hi,\n\nThe following reservation is ending: \n\nTitle: "+reservation.getTitle()+"\nDescription: "+reservation.getDescription()+ "\nResources: " + rString + "\nStart Time: "+ endTime +"\n\nThanks,\nResource Manager";
			System.out.println("end email being sent");
		}
		else {
			return;
		}
		emailUtility.sendMessage(reservation.getUser().getEmail(), fromString, subject, message);

	}

}
