package resourceplanner.reservations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resourceplanner.authentication.UserData.User;
import resourceplanner.authentication.UserService;
import resourceplanner.main.EmailService;
import resourceplanner.main.JDBC;
import resourceplanner.main.StandardResponse;
import resourceplanner.permissions.PermissionService;
import resourceplanner.reservations.ReservationData.Reservation;
import resourceplanner.reservations.ReservationData.ReservationWithIDs;
import resourceplanner.reservations.ReservationData.ReservationWithIDsData;
import resourceplanner.resources.ResourceData.Resource;
import resourceplanner.resources.ResourceService;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Transactional
@Service
public class ReservationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JdbcTemplate jt;

    /*

    public StandardResponse getMatchingReservations(GetAllMatchingReservationRequest req, int requesterId) {
        String getMatchingReservations = "SELECT * FROM reservations WHERE ";

        //If any query parameters were specified, we need to limit our query to those resources and users
        if (req.matchOnIds()) {
            getMatchingReservations = appendIDMatchString(getMatchingReservations, req);
            getMatchingReservations = getMatchingReservations + "AND ";
        }
        if (req.matchOnExcludedTags()) {
            getMatchingReservations = appendExcludedTagString(getMatchingReservations, req);
            getMatchingReservations = getMatchingReservations + "AND ";
        }
        if (req.matchOnRequiredTags()) {
            getMatchingReservations = appendRequiredTagString(getMatchingReservations, req);
            getMatchingReservations = getMatchingReservations + "AND ";
        }

        Timestamp startTime = req.getStart();
        Timestamp endTime = req.getEnd();

        getMatchingReservations = getMatchingReservations + "((begin_time <= ? AND begin_time >= ?) OR (end_time <=? AND end_time >= ?) OR (begin_time <= ?  AND end_time >= ?));";

        List<Reservation> reservations = jt.query(getMatchingReservations,
                new Object[]{req.getEnd(), req.getStart(), req.getEnd(), req.getStart(), req.getStart(), req.getEnd()},
                new RowMapper<Reservation>() {
                    public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ReservationWithIDs newRes = extractReservationFromResultSet(rs);
                        User user = getUserByID(newRes.getUser_id());
                        Resource resource = getResourceById(newRes.getResource_id());
                        return new Reservation(newRes, user, resource);
                    }
                }

        );

        if (reservations == null) {
            return new StandardResponse(true, "Error retrieving reservations");
        }

        if (requesterId != 1) {
            filterViewableReservations(reservations, requesterId);
        }
        return new StandardResponse(false, "Matching reservations retrieved", reservations);
    }

    private String appendIDMatchString(String baseQueryString, GetAllMatchingReservationRequest req) {
        baseQueryString = baseQueryString + "(";

        Integer[] resource_ids = req.getResource_ids();
        Integer[] user_ids = req.getUser_ids();
        //First do an OR over every resource_ID to see if reservation matches
        if (resource_ids != null && resource_ids.length > 0) {
            baseQueryString = baseQueryString + "(";
            for (int i = 0; i < resource_ids.length; i++) {
                baseQueryString = baseQueryString + "resource_id = " + resource_ids[i];
                if (i != resource_ids.length - 1) {
                    baseQueryString = baseQueryString + " OR ";
                } else {
                    baseQueryString = baseQueryString + ")";
                }
            }
        }

        //Next, do an OR over every user_id to see if reservation matches
        if (user_ids != null && user_ids.length > 0) {
            if (resource_ids != null && resource_ids.length > 0) {
                baseQueryString = baseQueryString + " OR (";
            } else {
                baseQueryString = baseQueryString + "(";
            }
            for (int i = 0; i < user_ids.length; i++) {
                baseQueryString = baseQueryString + "user_id = " + user_ids[i];
                if (i != user_ids.length - 1) {
                    baseQueryString = baseQueryString + " OR ";
                } else {
                    baseQueryString = baseQueryString + ")";
                }
            }
        }
        baseQueryString = baseQueryString + ")";
        return baseQueryString;
    }

    //Adding a string of the form (AND NOT EXISTS(SELECT * WHERE resource_id=resource_id AND tag IN (excluded tags)))
    public String appendExcludedTagString(String baseQueryString, GetAllMatchingReservationRequest req) {
        baseQueryString = baseQueryString + "(";

        String[] excluded_tags = req.getExcluded_tags();

        baseQueryString = baseQueryString + "NOT EXISTS (SELECT * from resourcetags WHERE reservations.resource_id = resourcetags.resource_id AND tag IN (";
        for (int i = 0; i < excluded_tags.length; i++) {
            baseQueryString = baseQueryString + "'" + excluded_tags[i] + "'";
            if (i != excluded_tags.length - 1) {
                baseQueryString = baseQueryString + ", ";
            } else {
                baseQueryString = baseQueryString + ")";
            }
        }

        baseQueryString = baseQueryString + ")) ";
        return baseQueryString;
    }

    //Adding a string of strings of the form (AND EXISTS (SELECT * from tagTable where resource_id = resource_ID and tag = required_tag1))
    public String appendRequiredTagString(String baseQueryString, GetAllMatchingReservationRequest req) {
        baseQueryString = baseQueryString + "(";
        String[] required_tags = req.getRequired_tags();

        for (int i = 0; i < required_tags.length; i++) {
            baseQueryString = baseQueryString + "EXISTS (SELECT * FROM resourcetags WHERE reservations.resource_id = resourcetags.resource_id AND tag = '" + required_tags[i] + "') ";
            if (i != required_tags.length - 1) {
                baseQueryString = baseQueryString + "AND ";
            } else {
                baseQueryString = baseQueryString + ")";
            }
        }

        return baseQueryString;

    }


    public StandardResponse updateReservationDB(ReservationRequest req, int reservationId, HttpServletRequest request) {
        ReservationWithIDs existingRes = getReservationWithIdsObjectById(reservationId);

        if (req.getUser_id() != null) {
            existingRes.setUser_id(req.getUser_id());
        }
        if (req.getResource_id() != null) {
            existingRes.setResource_id(req.getResource_id());
        }
        if (req.getBegin_time() != null) {
            existingRes.setBegin_time(req.getBegin_time());
        }
        if (req.getEnd_time() != null) {
            existingRes.setEnd_time(req.getEnd_time());
        }
        if (req.getShould_email() != null) {
            existingRes.setShould_email(req.getShould_email());
        }

        if (!existingRes.getBegin_time().before(existingRes.getEnd_time())) {
            return new StandardResponse(true, "Update causes begin time to occur after end time");
        }

        if (isOverlappingReservation(existingRes.getBegin_time(), existingRes.getEnd_time(), existingRes.getResource_id(), reservationId)) {
            return new StandardResponse(true, "Resource is occupied during new reservation time");
        }
        if (getResourceById(existingRes.getResource_id()) == null || getUserByID(existingRes.getUser_id()) == null) {
            return new StandardResponse(true, "New user or resource id is not valid");
        }

        if (reservableResource(req.getUser_id(), req.getResource_id())) {
            Object[] updateObject = new Object[]{existingRes.getUser_id(), existingRes.getResource_id(), existingRes.getBegin_time(),
                    existingRes.getEnd_time(), existingRes.getShould_email(), reservationId};

            int rows = jt.update("UPDATE reservations SET user_id = ?, resource_id = ?, begin_time = ?, end_time = ?, should_email = ? WHERE reservation_id = ?;",
                    updateObject);
            if (rows == 1) {
                ReservationWithIDsData reservationToReturn = new ReservationWithIDsData(existingRes);
                emailService.rescheduleEmails(reservationToReturn);
                return new StandardResponse(false, "Successfully updated reservation", reservationToReturn);
            } else {
                return new StandardResponse(true, "Error updating database entry for reservation");
            }
        } else {
            return new StandardResponse(true, "Resource not reservable by user");
        }
    }

    private boolean reservableResource(int userId, int resourceId) {
        List<Integer> userReservable = permissionService.getUserReservableResources(userId);
        List<Integer> groupReservable = permissionService.getGroupReservableResources(userId);

        Set<Integer> allReservableResources = new TreeSet<Integer>(userReservable);
        allReservableResources.addAll(groupReservable);

        return allReservableResources.contains(resourceId) || userId == 1;
    }

    private boolean viewableResource(int userId, int resourceId) {
        List<Integer> userViewable = permissionService.getUserViewableResources(userId);
        List<Integer> groupViewable = permissionService.getGroupViewableResources(userId);

        Set<Integer> allViewableResources = new TreeSet<Integer>(userViewable);
        allViewableResources.addAll(groupViewable);

        return allViewableResources.contains(resourceId);
    }


    private Boolean isOverlappingReservation(Timestamp start, Timestamp end, int resource_id, Integer currentReservation_id) {
        Connection c = JDBC.connect();
        PreparedStatement st = null;
        String query = "SELECT * FROM reservations WHERE resource_id = ? AND ((? >= begin_time AND ? < end_time) OR (? > begin_time AND ? <= end_time) OR (? <= begin_time AND ? >= end_time))";

        if (currentReservation_id != null) {
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
            if (currentReservation_id != null) {
                st.setInt(8, currentReservation_id);
            }

            ResultSet rs = st.executeQuery();
            c.close();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean isOverlappingReservation(Timestamp start, Timestamp end, int resource_id) {
        return isOverlappingReservation(start, end, resource_id, null);
    }

    public StandardResponse getReservationByIdDB(int reservationId, int requesterId) {
        Reservation reservation = getReservationObjectById(reservationId);

        if (reservation == null) {
            return new StandardResponse(true, "Reservation with given ID not found");
        } else {
            if (requesterId == 1 || viewableResource(reservation.getUser().getUser_id(), reservation.getResource().getResource_id())) {
                return new StandardResponse(false, "Reservation with given ID found", reservation);
            } else {
                return new StandardResponse(true, "Reservation is of non-viewable resource");
            }
        }
    }

    public Reservation getReservationObjectById(int reservationId) {
        String selectReservationsQuery = "SELECT * FROM reservations WHERE reservation_id = ?;";

        List<Reservation> reservations = jt.query(selectReservationsQuery,
                new Object[]{reservationId},
                new RowMapper<Reservation>() {
                    public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ReservationWithIDs newRes = extractReservationFromResultSet(rs);
                        User user = getUserByID(newRes.getUser_id());
                        Resource resource = getResourceById(newRes.getResource_id());
                        return new Reservation(newRes, user, resource);
                    }
                }

        );

        if (reservations.isEmpty()) {
            return null;
        }
        return reservations.get(0);
    }

    public ReservationWithIDs getReservationWithIdsObjectById(int reservationId) {
        String selectReservationsQuery = "SELECT * FROM reservations WHERE reservation_id = ?;";

        List<ReservationWithIDs> reservations = jt.query(selectReservationsQuery,
                new Object[]{reservationId},
                new RowMapper<ReservationWithIDs>() {
                    public ReservationWithIDs mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ReservationWithIDs newRes = extractReservationFromResultSet(rs);
                        return newRes;
                    }
                }

        );

        if (reservations.isEmpty()) {
            return null;
        }
        return reservations.get(0);
    }

    public ReservationWithIDsData getReservationWithIDsDataObjectById(int reservationId) {
        return new ReservationWithIDsData(getReservationWithIdsObjectById(reservationId));
    }

    private User getUserByID(int userID) {
        StandardResponse userResponse = userService.getUserById(userID);
        User user = (User) userResponse.getData();
        return user;
    }

    private Resource getResourceById(int resourceId) {
        StandardResponse resourceResponse = resourceService.getResourceByIdIgnoringPermissions(resourceId);
        return (Resource) resourceResponse.getData();
    }

    private ReservationWithIDs extractReservationFromResultSet(ResultSet rs) {
        try {
            int reservation_id = rs.getInt("reservation_id");
            int user_id = rs.getInt("user_id");
            int resource_id = rs.getInt("resource_id");
            Timestamp begin_time = rs.getTimestamp("begin_time");
            Timestamp end_time = rs.getTimestamp("end_time");
            boolean should_email = rs.getBoolean("should_email");

            ReservationWithIDs reservation = new ReservationWithIDs(reservation_id, user_id, resource_id, begin_time, end_time,
                    should_email);
            return reservation;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Integer> getReservationsOfUser(int userId) {
        String statement = "SELECT reservation_id FROM reservations WHERE user_id = " + userId + ";";

        List<Integer> reservationIds = jt.query(
                statement,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("reservation_id");
                    }
                });
        return reservationIds;
    }

    public List<Integer> getReservationsWithResource(int resourceId) {
        String statement = "SELECT reservation_id FROM reservations WHERE resource_id = " + resourceId + ";";

        List<Integer> reservationIds = jt.query(
                statement,
                new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("reservation_id");
                    }
                });
        return reservationIds;
    }

    private void filterViewableReservations(List<Reservation> reservations, int requesterId) {
        List<Integer> userViewable = permissionService.getUserViewableResources(requesterId);
        List<Integer> groupViewable = permissionService.getGroupViewableResources(requesterId);

        Set<Integer> allViewableResources = new TreeSet<Integer>(userViewable);
        allViewableResources.addAll(groupViewable);

        List<Reservation> reservationsToRemove = new ArrayList<Reservation>();
        for (Reservation r : reservations) {
            if (!allViewableResources.contains(r.getResource().getResource_id())) {
                reservationsToRemove.add(r);
            }
        }

        reservations.removeAll(reservationsToRemove);
    }

    */

    public StandardResponse createReservation(final ReservationRequest req) {
        if (!req.isValid()) {
            return new StandardResponse(true, "Request is not valid");
        }

        if (!req.isValidTimes()) {
            return new StandardResponse(true, "Begin time is after end time");
        }

        // check that each resource exists
        for (int i : req.getResource_ids()) {
            if (!resourceExists(i)) {
                return new StandardResponse(true, "Resource with id " + i + " does not exist");
            }
        }

        boolean complete = true;
        List<Resource> rList = new ArrayList<Resource>();

        // for each resource, find out what kind of overlap exists
        for (int i : req.getResource_ids()) {
            Resource r = getResource(i);
            rList.add(r);
            if (r.isRestricted()) {
                complete = false;
                // if the resource is 'restricted', add the request to the respective 'resource manager acceptance' tables
                // TODO
            }

            if (timeOpen(req.getBegin_time(), req.getEnd_time(), i) == 2) {
                // time not open
                return new StandardResponse(true, "Reservation for resource with id " + i + " already exists at that time");
            }
        }

        User u = getUser(req.getUser_id());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        final boolean finalComplete = complete;
        jt.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO reservations (title, description, user_id, begin_time, end_time, should_email, complete) VALUES (?, ?, ?, ?, ?, ?, ?);",
                                new String[]{"reservation_id"});
                        ps.setString(1, req.getTitle());
                        ps.setString(2, req.getDescription());
                        ps.setInt(3, req.getUser_id());
                        ps.setTimestamp(4, req.getBegin_time());
                        ps.setTimestamp(5, req.getEnd_time());
                        ps.setBoolean(6, req.getShould_email());
                        ps.setBoolean(7, finalComplete);
                        return ps;
                    }
                },
                keyHolder);


        int reservationId = keyHolder.getKey().intValue();

        List<Object[]> batch = new ArrayList<Object[]>();
        for (Integer resourceId : req.getResource_ids()) {
            Object[] values = new Object[]{
                    resourceId,
                    reservationId};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO reservationresources (resource_id, reservation_id) VALUES (?, ?);",
                batch);

        Reservation res = new Reservation(req.getTitle(), req.getDescription(), reservationId, u, rList, req.getBegin_time(), req.getEnd_time(), req.getShould_email(), complete);

        emailService.scheduleEmailUpdate(res);
        return new StandardResponse(false, "Reservation inserted successfully", res);

    }

    public StandardResponse getReservationById(int reservationId) {
        if (!reservationExists(reservationId)) {
            return new StandardResponse(true, "Reservation with that ID does not exist");
        }

        TempRes t = jt.query(
                "SELECT * FROM reservations WHERE reservation_id = ?;",
                new Object[]{reservationId},
                new RowMapper<TempRes>() {
                    public TempRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                        TempRes t = new TempRes();
                        t.reservation_id = rs.getInt("reservation_id");
                        t.title = rs.getString("title");
                        t.description = rs.getString("description");
                        t.user_id = rs.getInt("user_id");
                        t.begin_time = rs.getTimestamp("begin_time");
                        t.end_time = rs.getTimestamp("end_time");
                        t.should_email = rs.getBoolean("should_email");
                        t.complete = rs.getBoolean("complete");
                        return t;
                    }
                }).get(0);

        List<Resource> rList = getResources(reservationId);

        User u = getUser(t.user_id);

        Reservation r = new Reservation(t.title, t.description, t.reservation_id, u, rList, t.begin_time, t.end_time, t.should_email, t.complete);
        return new StandardResponse(false, "Successfully retrieved resource", r);
    }

    public StandardResponse deleteReservation(int reservationId, boolean isAdmin, int userId) {
        if (!reservationExists(reservationId)) {
            return new StandardResponse(true, "Reservation with that ID does not exist");
        }

        TempRes t = jt.query(
                "SELECT * FROM reservations WHERE reservation_id = ?;",
                new Object[]{reservationId},
                new RowMapper<TempRes>() {
                    public TempRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                        TempRes t = new TempRes();
                        t.reservation_id = rs.getInt("reservation_id");
                        t.title = rs.getString("title");
                        t.description = rs.getString("description");
                        t.user_id = rs.getInt("user_id");
                        t.begin_time = rs.getTimestamp("begin_time");
                        t.end_time = rs.getTimestamp("end_time");
                        t.should_email = rs.getBoolean("should_email");
                        t.complete = rs.getBoolean("complete");
                        return t;
                    }
                }).get(0);

        if (t.user_id != userId && !isAdmin) {
            return new StandardResponse(true, "You don't have permissions to delete this reservation");
        }

        jt.update("DELETE FROM reservationresources WHERE reservation_id = ?;", reservationId);
        jt.update("DELETE FROM reservations WHERE reservation_id = ?;", reservationId);
        return new StandardResponse(false, "Successfully deleted reservation");
    }

    public StandardResponse reservationTest(int resourceId) {
        return new StandardResponse(true, "test", resourceExists(resourceId));
    }

    private static class TimeRestrict {
        private Timestamp begin;
        private Timestamp end;
        private boolean complete;
    }

    private static class TempRes {
        private int reservation_id;
        private String title;
        private String description;
        private int user_id;
        private Timestamp begin_time;
        private Timestamp end_time;
        private boolean should_email;
        private boolean complete;
    }

    private int timeOpen(Timestamp wantedStart, Timestamp wantedEnd, int resourceId) {
        // 0 - open
        // 1 - some incomplete reservations overlap
        // 2 - not open

        // get all reservation_ids that use resourceId
        String q = "SELECT reservation_id FROM reservationresources WHERE resource_id = ?;";

        List<Integer> reservationIds = jt.queryForList(
                q,
                new Object[]{resourceId},
                Integer.class);

        int open = 0;

        for (int reservationId : reservationIds) {
            // for each reservationId, check if a reservation exists within the time frame
            String s = "SELECT begin_time, end_time, complete FROM reservations WHERE reservation_id = ?;";
            List<TimeRestrict> reservations = jt.query(
                s,
                new Object[]{reservationId},
                new RowMapper<TimeRestrict>() {
                    public TimeRestrict mapRow(ResultSet rs, int rowNum) throws SQLException {
                        TimeRestrict r = new TimeRestrict();
                        r.begin = rs.getTimestamp("begin_time");
                        r.end = rs.getTimestamp("end_time");
                        r.complete = rs.getBoolean("complete");
                        return r;
                    }
                });
            TimeRestrict reservation = reservations.get(0);

            boolean isOverlap = true;
            if (reservation.end.before(wantedStart) | reservation.end.equals(wantedStart) | reservation.begin.after(wantedEnd) | reservation.begin.equals(wantedEnd)) {
                isOverlap = false;
            }

            if (!isOverlap) {
                continue;
            }

            // if complete
            if (reservation.complete) {
                return 2;
            }

            // if not complete
            else {
                open = 1;
            }
        }

        return open;
    }

    private boolean resourceExists(int resourceId) {
        String statement = "SELECT COUNT(*) FROM resources WHERE resource_id = ?;";

        Integer cnt = jt.queryForObject(
                statement, Integer.class, resourceId);
        return cnt != null && cnt > 0;
    }

    private boolean reservationExists(int reservationId) {
        String statement = "SELECT COUNT(*) FROM reservations WHERE reservation_id = ?;";

        Integer cnt = jt.queryForObject(
                statement, Integer.class, reservationId);
        return cnt != null && cnt > 0;
    }

    private List<Resource> getResources(int reservationId) {
        List<Integer> resourceIds = jt.queryForList(
                "SELECT resource_id FROM reservationresources WHERE reservation_id = ?;",
                new Object[]{reservationId},
                Integer.class);

        List<Resource> rList = new ArrayList<Resource>();
        for (int resourceId : resourceIds) {
            rList.add(getResource(resourceId));
        }
        return rList;
    }

    private Resource getResource(int resourceId) {
        List<Resource> resources = jt.query(
                "SELECT name, description, restricted FROM resources WHERE resource_id = ?;",
                new Object[]{resourceId},
                new RowMapper<Resource>() {
                    public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Resource resource = new Resource();
                        resource.setName(rs.getString("name"));
                        resource.setDescription(rs.getString("description"));
                        resource.setRestricted(rs.getBoolean("restricted"));
                        return resource;
                    }
                });

        // TODO no need to check because already confirmed existance earlier

        Resource resource = resources.get(0);

        List<String> tags = jt.queryForList(
                "SELECT tag FROM resourcetags WHERE resource_id = ?;",
                new Object[]{resourceId},
                String.class);
        resource.setTags(tags);
        resource.setResource_id(resourceId);
        return resource;
    }

    public User getUser(int userId) {
        return jt.query(
                "SELECT * FROM users WHERE user_id = ?;",
                new Object[]{userId},
                new RowMapper<User>() {
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        User user = new User();
                        user.setEmail(rs.getString("email"));
                        user.setUsername(rs.getString("username"));
                        user.setShould_email(rs.getBoolean("should_email"));
                        user.setUser_id(rs.getInt("user_id"));
                        user.setResource_p(rs.getBoolean("resource_p"));
                        user.setUser_p(rs.getBoolean("user_p"));
                        user.setReservation_p(rs.getBoolean("reservation_p"));
                        return user;
                    }
                }).get(0);
    }

}