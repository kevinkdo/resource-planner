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
import resourceplanner.main.EmailService;
import resourceplanner.main.StandardResponse;
import resourceplanner.permissions.PermissionService;
import resourceplanner.reservations.ReservationData.Reservation;
import resourceplanner.reservations.ReservationData.ResourceReservations;
import resourceplanner.resources.ResourceData.Resource;
import resourceplanner.resources.ResourceService;
import resourceplanner.reservations.ReservationData.*;

import java.sql.*;
import java.util.*;

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
    

    public StandardResponse createReservation(final ReservationRequest req, int userId) {
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

        List<Integer> userReservable = permissionService.getUserReservableResources(userId);
        List<Integer> groupReservable = permissionService.getGroupReservableResources(userId);

        Set<Integer> allReservableResources = new HashSet<Integer>(userReservable);
        allReservableResources.addAll(groupReservable);

        // for each resource, find out what kind of overlap exists
        for (int i : req.getResource_ids()) {
            if (userId != 1) {
                if (!allReservableResources.contains(i)) {
                    return new StandardResponse(true, "You do not have reservation permission for resource with ID " + i);
                }
            }
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

        return new StandardResponse(false, "Reservation inserted successfully", res);

    }

    public Reservation getReservationByIdAdmin(int reservationId) {
        if (!reservationExists(reservationId)) {
            return null;
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

        return new Reservation(t.title, t.description, t.reservation_id, u, rList, t.begin_time, t.end_time, t.should_email, t.complete);
    }

    public StandardResponse getReservationById(int reservationId, int userId) {
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

        if (userId != 1) {
            List<Integer> userViewable = permissionService.getUserViewableResources(userId);
            List<Integer> groupViewable = permissionService.getGroupViewableResources(userId);

            Set<Integer> allViewableResources = new HashSet<Integer>(userViewable);
            allViewableResources.addAll(groupViewable);

            for (Resource r : rList) {
                if (!allViewableResources.contains(r.getResource_id())) {
                    return new StandardResponse(false, "You can't view this reservation because you don't have viewing rights for all resources in the reservation");
                }
            }
        }

        User u = getUser(t.user_id);

        Reservation r = new Reservation(t.title, t.description, t.reservation_id, u, rList, t.begin_time, t.end_time, t.should_email, t.complete);
        return new StandardResponse(false, "Successfully retrieved reservation", r);
    }

    public StandardResponse getReservations(QueryReservationRequest req, int userId) {
        // make sure resource_id is valid
        // TODO

        List<Integer> reservationIds = jt.queryForList("SELECT reservation_id FROM reservationresources WHERE resource_id = ?;",
                new Object[]{req.getResource_id()}, Integer.class);

        List<Reservation> reservations = new ArrayList<Reservation>();

        for (int reservationId : reservationIds) {
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

            List<Integer> userViewable = permissionService.getUserViewableResources(userId);
            List<Integer> groupViewable = permissionService.getGroupViewableResources(userId);

            Set<Integer> allViewableResources = new HashSet<Integer>(userViewable);
            allViewableResources.addAll(groupViewable);

            for (Resource r : rList) {
                if (!allViewableResources.contains(r.getResource_id())) {
                    continue;
                }
            }

            User u = getUser(t.user_id);

            Reservation r = new Reservation(t.title, t.description, t.reservation_id, u, rList, t.begin_time, t.end_time, t.should_email, t.complete);
            reservations.add(r);
        }

        ResourceReservations reservationResponse = new ResourceReservations(reservations);
        return new StandardResponse(false, "Successfully retrieved reservations for the resource", reservationResponse);
    }

    public StandardResponse updateReservation(ReservationRequest req, int reservationId, boolean isAdmin, int userId) {
        if (!req.isValid()) {
            return new StandardResponse(true, "Request is not valid");
        }

        if (!req.isValidTimes()) {
            return new StandardResponse(true, "Start time after end time");
        }

        // check that each resource exists
        for (int i : req.getResource_ids()) {
            if (!resourceExists(i)) {
                return new StandardResponse(true, "Resource with id " + i + " does not exist");
            }
        }

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
            return new StandardResponse(true, "You don't have permissions to edit this reservation");
        }

        if (req.getBegin_time().before(t.begin_time)) {
            return new StandardResponse(true, "Start time must be after old start time");
        }

        if (req.getEnd_time().after(t.end_time)) {
            return new StandardResponse(true, "End time must be before old end time");
        }

        List<Resource> rList = getResources(reservationId);
        List<Integer> rListInts = rListToInts(rList);
        User u = getUser(req.getUser_id());

        if (userId != 1) {
            List<Integer> userReservable = permissionService.getUserReservableResources(userId);
            List<Integer> groupReservable = permissionService.getGroupReservableResources(userId);

            Set<Integer> allReservableResources = new TreeSet<Integer>(userReservable);
            allReservableResources.addAll(groupReservable);

            for (int resourceId : req.getResource_ids()) {
                // for each resource, make sure that the resource exists in rList
                if (!rListInts.contains(resourceId)) {
                    return new StandardResponse(true, "You cannot add a new resource when updating a reservation");
                }

                if (!allReservableResources.contains(resourceId)) {
                    return new StandardResponse(true, "You do not have reservation permission for resource with ID "+resourceId);
                }
            }
        }

        String q = "UPDATE reservations SET title = ?, description = ?, user_id = ?, begin_time = ?, end_time = ?, should_email = ?, complete = ? WHERE reservation_id = ?;";

        jt.update(q, new Object[]{req.getTitle(), req.getDescription(), req.getUser_id(), req.getBegin_time(), req.getEnd_time(), req.getShould_email(), t.complete, reservationId});

        jt.update("DELETE FROM reservationresources WHERE reservation_id = ?;", reservationId);


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

        Reservation res = new Reservation(req.getTitle(), req.getDescription(), reservationId, u, rList, req.getBegin_time(), req.getEnd_time(), req.getShould_email(), t.complete);
        if (t.complete) {
            emailService.rescheduleEmails(reservationId);
        }
        return new StandardResponse(false, "Reservation successfully updated", res);
    }

    private List<Integer> rListToInts(List<Resource> rList) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i=0; i<rList.size(); i++) {
            list.add(rList.get(i).getResource_id());
        }
        return list;
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

        emailService.removeScheduledEmails(reservationId);
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

    /*
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
    */

    private List<Resource> getResources(int reservationId, int specifier){
        // 0 = all resources
        // 1 = only approved resources
        // 2 = only unnapproved resources
        String queryString = "SELECT resource_id FROM reservationresources WHERE reservation_id = ?";
        if(specifier == 1){
            queryString = queryString + " AND resource_approved = true";
        }
        else if (specifier == 2){
            queryString = queryString + " AND resource_approved = false";
        }
        queryString = queryString + ";";

        List<Integer> resourceIds = jt.queryForList(
                queryString,
                new Object[]{reservationId},
                Integer.class);

        List<Resource> rList = new ArrayList<Resource>();
        for (int resourceId : resourceIds) {
            rList.add(getResource(resourceId));
        }
        return rList;
    }

    private List<Resource> getUnnaprovedResources(int reservationId){
        return getResources(reservationId, 2);
    }
    private List<Resource> getResources(int reservationId){
        return getResources(reservationId, 0);
    }
    private List<Resource> getApprovedResources(int reservationId){
        return getResources(reservationId, 1);
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

    private Reservation getReservationFromTempRes(TempRes t){
        return new Reservation(t.title, t.description, t.reservation_id, getUser(t.user_id), getResources(t.reservation_id), t.begin_time, t.end_time, t.should_email, t.complete);
    }

    private List<Reservation> convertTempListToReservationList(List<TempRes> temps){
        List<Reservation> reservations = new ArrayList<Reservation>();
        for(TempRes t : temps){
            reservations.add(getReservationFromTempRes(t));
        }
        return reservations;
    }

     private TempRes getTempResFromId(int reservationId){
        List<TempRes> reservations = jt.query(
            "SELECT * FROM reservations WHERE reservation_id = " + reservationId + ";",
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
                });
        return reservations.get(0);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////Incomplete Reservation Endpoints//////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Returns all incomplete reservations for which the user can approve at least one non-approved resource
    //Briefly, this first gathers a list of ALL incomplete reservations
    //It also gathers a list of all restricted resources that the user can approve
    //The program then removes any reservations from the original list that have no resources the user can approve
    //Finally, it returns a final object that desginates, for each reservation, which resources the user can
    //approve, and which are already approved. 
    public StandardResponse getApprovableReservations(int userId, boolean hasResourceP){
        List<TempRes> incompleteReservations = getIncompleteReservations();
        List<TempRes> reservationsToRemove = new ArrayList<TempRes>();
       
        //If the user is admin or has system resource permission, he can approve anything
        //else, have to filter
        if(userId != 1 && !hasResourceP){
            //This is a list of all resources the user can approve that are restricted
            List<Integer> resourceManagerResources = permissionService.getAllRestrictedResourceManagerResources(userId);
            List<ApprovableResources> approvableResourcesList = new ArrayList<ApprovableResources>();

            for(TempRes t : incompleteReservations){
                //Each reservation must have at least 1 UNAPPROVED resource the user can approve
                List<Resource> approvableResources = new ArrayList<Resource>();
                List<Resource> resourcesForRes = getUnnaprovedResources(t.reservation_id);
                boolean hasApprovableResource = false;

                for(Resource r : resourcesForRes){
                    if(resourceManagerResources.contains(r.getResource_id())){
                        hasApprovableResource = true;
                        approvableResources.add(r);
                    }
                }

                if(!hasApprovableResource){
                    reservationsToRemove.add(t);
                }
                else{
                    approvableResourcesList.add(new ApprovableResources(t.reservation_id, approvableResources, getApprovedResources(t.reservation_id)));
                }
            }

            incompleteReservations.removeAll(reservationsToRemove);
            List<Reservation> approvableReservations = convertTempListToReservationList(incompleteReservations);
            ReservationsAndApprovableResources output = new ReservationsAndApprovableResources(approvableReservations, approvableResourcesList);
            return new StandardResponse(false, "Approvable reservations returned", output);
        }
        else{
            //For admin, you just get the list of unnaproved and approved resources. You can approve all unnaproved
            //for regular, you get list of unnaproved that you can fix
            List<ApprovableResources> approvableResourcesList = new ArrayList<ApprovableResources>();
            List<Reservation> approvableReservations = convertTempListToReservationList(incompleteReservations);
            for(Reservation r : approvableReservations){
                int res_id = r.getReservation_id();
                approvableResourcesList.add(new ApprovableResources(res_id, getUnnaprovedResources(res_id), getApprovedResources(res_id)));
            }
            ReservationsAndApprovableResources output = new ReservationsAndApprovableResources(approvableReservations, approvableResourcesList);
            return new StandardResponse(false, "Approvable reservations returned", output);
        }
    }

    //Returns all incomplete reservations which would be canceled if the given reservation were approved
    public StandardResponse getCanceledWithApproval(int reservationId){
        TempRes currentRes = getTempResFromId(reservationId);
        List<TempRes> overlapping = getOverlappingIncompleteReservations(currentRes);
        List<Reservation> overlappingReservations = convertTempListToReservationList(overlapping);
        return new StandardResponse(false, "To-be-canceled reservations returned", overlappingReservations);
    }

    //Approves or denies the resources for a given reservation that the user is allowed to approve/deny. 
    public StandardResponse approveReservation(ReservationApproval approval, int reservationId, int userId){
        if(approval.getApproved()){

        }
        else{
            //Delete the reservation "as an admin" to guarentee it is deleted. 
        }

        // TODO
        emailService.scheduleEmail(reservationId);

        return new StandardResponse(true, "Not yet implemented");
    }


    private List<TempRes> getIncompleteReservations(){
        return jt.query(
            "SELECT * FROM reservations WHERE complete = false;",
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
                });
    }

    private List<TempRes> getOverlappingIncompleteReservations(TempRes t){
        List<TempRes> reservations = jt.query(
            "SELECT * FROM reservations WHERE reservation_id != " + t.reservation_id + 
            " AND complete = false AND ((reservations.begin_time >= " + t.begin_time + " AND reservations.begin_time < " + t.end_time + 
            ") OR (reservations.end_time > " + t.begin_time + " AND reservations.end_time <= " + t.end_time + 
            ") OR (reservations.end_time > " + t.end_time + " AND reservations.begin_time < " + t.begin_time + 
            "));",
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
                });
        return reservations;
    }

}