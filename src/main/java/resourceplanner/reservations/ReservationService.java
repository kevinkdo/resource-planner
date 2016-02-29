package resourceplanner.reservations;

import resourceplanner.main.JDBC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.resources.ResourceService;
import resourceplanner.main.EmailService;
import resourceplanner.authentication.UserService;
import resourceplanner.main.StandardResponse;
import resourceplanner.reservations.ReservationData.Reservation;
import resourceplanner.reservations.ReservationData.ReservationWithIDs;
import resourceplanner.reservations.ReservationData.ReservationWithIDsData;
import resourceplanner.resources.ResourceData.Resource;
import resourceplanner.authentication.UserData.User;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.List;

@Transactional
@Service
public class ReservationService{

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserService userService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private JdbcTemplate jt;

    public StandardResponse getMatchingReservations(GetAllMatchingReservationRequest req){
        String getMatchingReservations = "SELECT * FROM reservations WHERE ";

        //If any query parameters were specified, we need to limit our query to those resources and users
        if(req.matchOnIds()){
            getMatchingReservations = appendIDMatchString(getMatchingReservations, req);
            getMatchingReservations = getMatchingReservations + "AND ";
        }
        if(req.matchOnExcludedTags()){
        	getMatchingReservations = appendExcludedTagString(getMatchingReservations, req);
        	getMatchingReservations = getMatchingReservations + "AND ";
        }
        if(req.matchOnRequiredTags()){
        	getMatchingReservations = appendRequiredTagString(getMatchingReservations, req);
        	getMatchingReservations = getMatchingReservations + "AND ";
        }

        Timestamp startTime = req.getStart();
        Timestamp endTime = req.getEnd();

        getMatchingReservations = getMatchingReservations + "((begin_time <= ? AND begin_time >= ?) OR (end_time <=? AND end_time >= ?) OR (begin_time <= ?  AND end_time >= ?));";

        List<Reservation> reservations  = jt.query(getMatchingReservations,
        	new Object[]{req.getEnd(), req.getStart(), req.getEnd(), req.getStart(), req.getStart(), req.getEnd()},
        	new RowMapper<Reservation>(){
        		public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException{
        			ReservationWithIDs newRes = extractReservationFromResultSet(rs);
        			User user = getUserByID(newRes.getUser_id());
        			Resource resource = getResourceById(newRes.getResource_id());
        			return new Reservation(newRes, user, resource);
        		}
        	}

        );
		
		if(reservations == null){
			return new StandardResponse(true, "Error retrieving reservations");
		}

		return new StandardResponse(false, "Matching reservations retrieved", reservations);
    }

    private String appendIDMatchString(String baseQueryString, GetAllMatchingReservationRequest req){
        baseQueryString = baseQueryString + "(";

        Integer[] resource_ids = req.getResource_ids();
        Integer[] user_ids = req.getUser_ids();
        //First do an OR over every resource_ID to see if reservation matches
        if(resource_ids != null && resource_ids.length > 0){
            baseQueryString = baseQueryString + "(";
            for (int i = 0; i < resource_ids.length; i++){
                baseQueryString = baseQueryString + "resource_id = " + resource_ids[i];
                if (i != resource_ids.length - 1){
                    baseQueryString = baseQueryString + " OR ";
                }
                else{
                    baseQueryString = baseQueryString + ")";
                }
            }
        }
        
        //Next, do an OR over every user_id to see if reservation matches
        if(user_ids != null && user_ids.length > 0){
            if(resource_ids != null && resource_ids.length > 0){
                baseQueryString = baseQueryString + " OR (";
            }
            else{
                baseQueryString = baseQueryString + "(";
            }
            for (int i = 0; i < user_ids.length; i++){
                baseQueryString = baseQueryString + "user_id = " + user_ids[i];
                if (i != user_ids.length - 1){
                    baseQueryString = baseQueryString + " OR ";
                }
                else{
                    baseQueryString = baseQueryString + ")";
                }
            }
        }
        baseQueryString = baseQueryString + ")";
        return baseQueryString;
    }

    //Adding a string of the form (AND NOT EXISTS(SELECT * WHERE resource_id=resource_id AND tag IN (excluded tags)))
    public String appendExcludedTagString(String baseQueryString, GetAllMatchingReservationRequest req){
    	baseQueryString = baseQueryString + "(";

    	String[] excluded_tags = req.getExcluded_tags();

		baseQueryString = baseQueryString + "NOT EXISTS (SELECT * from resourcetags WHERE reservations.resource_id = resourcetags.resource_id AND tag IN (";
		for(int i = 0; i < excluded_tags.length; i++){
			baseQueryString = baseQueryString + "'" + excluded_tags[i] + "'";
			if(i != excluded_tags.length -1){
				baseQueryString = baseQueryString + ", ";
			}
			else{
				baseQueryString = baseQueryString + ")";
			}
		}										
    	
    	baseQueryString = baseQueryString + ")) ";
		return baseQueryString;
    }

    //Adding a string of strings of the form (AND EXISTS (SELECT * from tagTable where resource_id = resource_ID and tag = required_tag1))
    public String appendRequiredTagString(String baseQueryString, GetAllMatchingReservationRequest req){
    	baseQueryString = baseQueryString + "(";
    	String[] required_tags = req.getRequired_tags();

    	for(int i = 0; i < required_tags.length; i++){
    		baseQueryString = baseQueryString + "EXISTS (SELECT * FROM resourcetags WHERE reservations.resource_id = resourcetags.resource_id AND tag = '" + required_tags[i] + "') ";
			if(i != required_tags.length - 1){
				baseQueryString = baseQueryString + "AND ";
			}
			else{
				baseQueryString = baseQueryString + ")";
			}
    	}

    	return baseQueryString;

    }


    public StandardResponse deleteReservationByIdDB(int reservationId){
    	String deleteReservation = "DELETE FROM reservations WHERE reservation_id = ?;";

    	int rows = jt.update(deleteReservation, new Object[]{reservationId});
    	if(rows == 0){
    		return new StandardResponse(true, "No reservation with that ID exists");
    	}
    	else{
    		emailService.removeScheduledEmails(reservationId);
    		return new StandardResponse(false, "Reservation successfully deleted");
    	}
    }


    public StandardResponse createReservationDB(ReservationRequest req){
        if(isOverlappingReservation(req.getBegin_time(), req.getEnd_time(), req.getResource_id())){
            return new StandardResponse(true, "Reservation for that resource overlaps with specified times");
        }
        if(getUserByID(req.getUser_id()) == null){
            return new StandardResponse(true, "User does not exist");
        }
        if(getResourceById(req.getResource_id()) == null){
            return new StandardResponse(true, "Resource does not exist");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO reservations (user_id, resource_id, begin_time, end_time, should_email) VALUES (?, ?, ?, ?, ?);",
                                new String[]{"reservation_id"});
                        ps.setInt(1, req.getUser_id());
                        ps.setInt(2, req.getResource_id());
                        ps.setTimestamp(3, req.getBegin_time());
                        ps.setTimestamp(4, req.getEnd_time());
                        ps.setBoolean(5, req.getShould_email());
                        return ps;
                    }
                },
                keyHolder);


        int resId = keyHolder.getKey().intValue();
        ReservationWithIDsData newReservation = new ReservationWithIDsData(resId, req.getUser_id(), req.getResource_id(),
                req.getBegin_time(), req.getEnd_time(), req.getShould_email());
        emailService.scheduleEmailUpdate(newReservation);
        return new StandardResponse(false, "Reservation inserted successfully", newReservation);

    }

    public StandardResponse updateReservationDB(ReservationRequest req, int reservationId, HttpServletRequest request){
        ReservationWithIDs existingRes = getReservationWithIdsObjectById(reservationId);

        if(req.getUser_id() != null){
            existingRes.setUser_id(req.getUser_id());
        }
        if(req.getResource_id() != null){
            existingRes.setResource_id(req.getResource_id());
        }
        if(req.getBegin_time() != null){
            existingRes.setBegin_time(req.getBegin_time());
        }
        if(req.getEnd_time() != null){
            existingRes.setEnd_time(req.getEnd_time());
        }
        if(req.getShould_email() != null){
            existingRes.setShould_email(req.getShould_email());
        }

        if(!existingRes.getBegin_time().before(existingRes.getEnd_time())){
        	return new StandardResponse(true, "Update causes begin time to occur after end time");
        }

        if(isOverlappingReservation(existingRes.getBegin_time(), existingRes.getEnd_time(), existingRes.getResource_id(), reservationId)){
            return new StandardResponse(true, "Resource is occupied during new reservation time");
        }
        if(getResourceById(existingRes.getResource_id()) == null || getUserByID(existingRes.getUser_id()) == null){
            return new StandardResponse(true, "New user or resource id is not valid");
        }

        Object[] updateObject = new Object[]{existingRes.getUser_id(), existingRes.getResource_id(), existingRes.getBegin_time(),
                            existingRes.getEnd_time(), existingRes.getShould_email(), reservationId};

        int rows = jt.update("UPDATE reservations SET user_id = ?, resource_id = ?, begin_time = ?, end_time = ?, should_email = ? WHERE reservation_id = ?;",
                            updateObject);
        if(rows == 1){
            ReservationWithIDsData reservationToReturn = new ReservationWithIDsData(existingRes);
            emailService.rescheduleEmails(reservationToReturn);
            return new StandardResponse(false, "Successfully updated reservation", reservationToReturn);
        }
        else{
            return new StandardResponse(true, "Error updating database entry for reservation");
        }

    }

    private Boolean isOverlappingReservation(Timestamp start, Timestamp end, int resource_id, Integer currentReservation_id){
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String query = "SELECT * FROM reservations WHERE resource_id = ? AND ((? >= begin_time AND ? < end_time) OR (? > begin_time AND ? <= end_time) OR (? <= begin_time AND ? >= end_time))";

        if(currentReservation_id != null){
            query = query + " AND reservation_id != ?";
        }
        query = query + ";";

        try {
            st = c.prepareStatement(query);
            st.setInt(1, resource_id);
            st.setTimestamp(2, start);
            st.setTimestamp(3, start);
            st.setTimestamp(4, end);
            st.setTimestamp(5, end);
            st.setTimestamp(6, start);
            st.setTimestamp(7, end);
            if(currentReservation_id != null){
                st.setInt(8, currentReservation_id);
            }

            ResultSet rs = st.executeQuery();
            c.close();
            if(rs.next()){
                return true;
            }
            else{
                return false;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private Boolean isOverlappingReservation(Timestamp start, Timestamp end, int resource_id){
        return isOverlappingReservation(start, end, resource_id, null);
    }

    public StandardResponse getReservationByIdDB(int reservationId) {
    	Reservation reservation = getReservationObjectById(reservationId);
    	if (reservation == null){
    		return new StandardResponse(true, "Reservation with given ID not found");
    	}
    	else{
    		return new StandardResponse(false, "Reservation with given ID found", reservation);
    	}
    }

    public Reservation getReservationObjectById(int reservationId){
        String selectReservationsQuery = "SELECT * FROM reservations WHERE reservation_id = ?;";

        List<Reservation> reservations  = jt.query(selectReservationsQuery,
        	new Object[]{reservationId},
        	new RowMapper<Reservation>(){
        		public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException{
        			ReservationWithIDs newRes = extractReservationFromResultSet(rs);
        			User user = getUserByID(newRes.getUser_id());
        			Resource resource = getResourceById(newRes.getResource_id());
        			return new Reservation(newRes, user, resource);
        		}
        	}

        );

        if(reservations.isEmpty()){
        	return null;
        }
        return reservations.get(0);
    }

    public ReservationWithIDs getReservationWithIdsObjectById(int reservationId){
        String selectReservationsQuery = "SELECT * FROM reservations WHERE reservation_id = ?;";

        List<ReservationWithIDs> reservations  = jt.query(selectReservationsQuery,
        	new Object[]{reservationId},
        	new RowMapper<ReservationWithIDs>(){
        		public ReservationWithIDs mapRow(ResultSet rs, int rowNum) throws SQLException{
        			ReservationWithIDs newRes = extractReservationFromResultSet(rs);
        			return newRes;
        		}
        	}

        );

        if(reservations.isEmpty()){
        	return null;
        }
        return reservations.get(0);
    }

    public ReservationWithIDsData getReservationWithIDsDataObjectById(int reservationId){
    	return new ReservationWithIDsData(getReservationWithIdsObjectById(reservationId));
    }

    private User getUserByID(int userID){
    	StandardResponse userResponse = userService.getUserById(userID);
    	User user = (User) userResponse.getData();
    	return user;
    }

    private Resource getResourceById(int resourceId) {
    	StandardResponse resourceResponse = resourceService.getResourceById(resourceId);
    	return (Resource) resourceResponse.getData();
    }

    private ReservationWithIDs extractReservationFromResultSet(ResultSet rs){
        try{
            int reservation_id = rs.getInt("reservation_id");
            int user_id = rs.getInt("user_id");
            int resource_id = rs.getInt("resource_id");
            Timestamp begin_time = rs.getTimestamp("begin_time");
            Timestamp end_time = rs.getTimestamp("end_time");
            boolean should_email = rs.getBoolean("should_email");

            ReservationWithIDs reservation = new ReservationWithIDs(reservation_id, user_id, resource_id, begin_time, end_time, 
                should_email);
            return reservation;
        }
        catch(Exception e){
            return null;
        }
    }

    public List<Integer> getReservationsOfUser(int userId){
    	String statement = "SELECT reservation_id FROM reservations WHERE user_id = " + userId +";";

    	List<Integer> reservationIds = jt.query(
                statement,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("reservation_id");
                    }
                });
    	return reservationIds;
    }

    public List<Integer> getReservationsWithResource(int resourceId){
    	String statement = "SELECT reservation_id FROM reservations WHERE resource_id = " + resourceId +";";

    	List<Integer> reservationIds = jt.query(
                statement,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("reservation_id");
                    }
                });
    	return reservationIds;
    }

}