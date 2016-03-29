package utilities;

import resourceplanner.reservations.ReservationData.Reservation;
import resourceplanner.resources.ResourceData.Resource;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import resourceplanner.reservations.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


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
	public static final String DENY_ALERT = "deny";
	public static final String CANCEL_ALERT = "cancel";
	public static final String INCOMPLETE_NEVER_APPROVED_ALERT = "incomplete_never_approved";
	public static final String INCOMPLETE_PENDING_ALERT = "incomplete_pending";

	private String url = "jdbc:postgresql://localhost:5432/";
    private String dbName = "rp";
    private String driver = "org.postgresql.Driver";
    private String username = "postgres";
    private String password = "password";

	//@Value("${fromString}")
	private String fromString ="ResourceManagerAlerts@gmail.com";

	private JdbcTemplate jt;

	public EmailScheduler(Reservation reservation, String alertType){
		this.reservation = reservation;
		this.alertType = alertType;	

		if((alertType != null) && (alertType.equals(INCOMPLETE_NEVER_APPROVED_ALERT))){
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName(driver);
			dataSource.setUrl(url + dbName);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
			jt = new JdbcTemplate(dataSource);
		}
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
			message = "Hi,\n\nThe following reservation is ending: \n\nTitle: "+reservation.getTitle()+"\nDescription: "+reservation.getDescription()+ "\nResources: " + rString + "\nEnd Time: "+ endTime +"\n\nThanks,\nResource Manager";
			System.out.println("end email being sent");
		}
		else if((alertType != null) && (alertType.equals(DENY_ALERT))){
			subject = "Reservation denied";
			message = "Hi, \n\nThe following reservation was just denied for a resource approval: \n\nTitle: " + reservation.getTitle() + "\nDescription: " + reservation.getDescription() + "\nResources: " + rString + "\n\n As such, this reservation was deleted. \n\nThanks, \nResource Manager";
			System.out.println("deny email being sent");
		}
		else if((alertType != null) && (alertType.equals(CANCEL_ALERT))){
			subject = "Reservation canceled";
			message = "Hi, \n\nThe following reservation was just canceled due to another reservation being approved: \n\nTitle: " + reservation.getTitle() + "\nDescription: " + reservation.getDescription() + "\nResources: " + rString + "\n\n Thanks, \nResource Manager";
			System.out.println("cancel email being sent");
		}
		else if((alertType != null) && (alertType.equals(INCOMPLETE_NEVER_APPROVED_ALERT))){
			subject = "Incomplete reservation never approved";
			message = "Hi, \n\nThe following reservation was never fully approved, and as such was canceled at its start time: \n\nTitle: " + reservation.getTitle() + "\nDescription: " + reservation.getDescription() + "\nResources: " + rString + "\nStart Time: "+ beginTime + "\n\n Thanks, \nResource Manager";
			deletePendingReservation();
			System.out.println("incomplete never approved email being sent");
		}
		else if((alertType != null) && (alertType.equals(INCOMPLETE_PENDING_ALERT))){
			subject = "Incomplete reservation not yet approved";
			message = "Hi, \n\nThe following reservation is scheduled to begin soon, but still has not been approved. It will be canceled if it is not approved in time: \n\nTitle: " + reservation.getTitle() + "\nDescription: " + reservation.getDescription() + "\nResources: " + rString + "\nStart Time: "+ beginTime + "\n\n Thanks, \nResource Manager";
			System.out.println("incomplete pending email being sent");
		}
		else {
			return;
		}
		emailUtility.sendMessage(reservation.getUser().getEmail(), fromString, subject, message);
	}

	private void deletePendingReservation(){
		jt.update("DELETE FROM reservationresources WHERE reservation_id = ?;", reservation.getReservation_id());
        jt.update("DELETE FROM reservations WHERE reservation_id = ?;", reservation.getReservation_id());
	}


}
