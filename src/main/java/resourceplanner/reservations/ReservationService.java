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
import resourceplanner.reservations.ReservationData.*;
import resourceplanner.resources.ResourceData.Resource;
import resourceplanner.resources.ResourceService;

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

        if (!req.isValidText()) {
            return new StandardResponse(true, "Request title or description length is not valid");
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
                if(!canReserveWithSharedInTimespan(i, req.getBegin_time(), req.getEnd_time())){
                    //Time not available for this resource
                    return new StandardResponse(true, "Reservation for resource with id " + i + " already exists at that time");
                }
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
            boolean resource_approved = !isRestricted(resourceId);
            Object[] values = new Object[]{
                    resourceId,
                    reservationId,
                    resource_approved};
            batch.add(values);
        }
        int[] updateCounts = jt.batchUpdate(
                "INSERT INTO reservationresources (resource_id, reservation_id, resource_approved) VALUES (?, ?, ?);",
                batch);

        Reservation res = new Reservation(req.getTitle(), req.getDescription(), reservationId, u, rList, req.getBegin_time(), req.getEnd_time(), req.getShould_email(), complete);
        emailService.scheduleEmails(reservationId);
        return new StandardResponse(false, "Reservation inserted successfully", res);
    }

    private boolean isRestricted(int resourceId) {
        String statement = "SELECT COUNT(*) FROM resources WHERE resource_id = ? AND restricted = true;";

        Integer cnt = jt.queryForObject(
                statement, Integer.class, resourceId);
        return cnt != null && cnt > 0;
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

    public Reservation getReservationByIdHelper(int reservationId, int userId) {

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
                    return null;
                }
            }
        }

        User u = getUser(t.user_id);

        return new Reservation(t.title, t.description, t.reservation_id, u, rList, t.begin_time, t.end_time, t.should_email, t.complete);
    }

    private List<Reservation> getAllReservations(int userId, Timestamp start, Timestamp end) {
        List<Integer> reservationIds = jt.queryForList("SELECT reservation_id FROM reservations WHERE NOT (end_time < ? OR begin_time > ?);",
                new Object[]{start, end}, Integer.class);

        List<Reservation> reservations = new ArrayList<Reservation>();

        for (int reservationId : reservationIds) {
            Reservation r = getReservationByIdHelper(reservationId, userId);
            if (r != null) {
                reservations.add(r);
            }
        }
        return reservations;
    }

    private List<Integer> getAllReservationIds(Timestamp start, Timestamp end) {
        List<Integer> reservationIds = jt.queryForList("SELECT reservation_id FROM reservations WHERE NOT (end_time < ? OR begin_time > ?);",
                new Object[]{start, end}, Integer.class);
        return reservationIds;
    }

    private List<Reservation> getReservationsByIds(Set<Integer> reservationIds, int userId) {
        List<Reservation> reservations = new ArrayList<Reservation>();

        for (int reservationId : reservationIds) {
            Reservation r = getReservationByIdHelper(reservationId, userId);
            if (r != null) {
                reservations.add(r);
            }
        }
        return reservations;
    }

    private List<Integer> getReservationIdsByResourceId(int resourceId, Timestamp start, Timestamp end) {
        List<Integer> reservationIds = jt.queryForList(
                "SELECT reservationresources.reservation_id " +
                        "FROM reservationresources, reservations " +
                        "WHERE reservationresources.reservation_id = reservations.reservation_id " +
                        "AND reservationresources.resource_id = ?" +
                        "AND NOT (reservations.end_time < ? OR reservations.begin_time > ?);",
                new Object[]{resourceId, start, end}, Integer.class);
        return reservationIds;
    }

    private Set<String> getTagsByReservation(Reservation r) {
        List<Resource> resources = r.getResources();

        Set<String> tags = new HashSet<String>();
        for (Resource resource : resources) {
            tags.addAll(resource.getTags());
        }
        return tags;
    }

    public StandardResponse getReservations(QueryReservationRequest req, int userId) {
        if (!req.isValid()) {
            return new StandardResponse(true, "Request is not valid");
        }

        if (req.getResource_ids().length == 0 && req.getRequired_tags().length == 0 && req.getExcluded_tags().length == 0) {
            return new StandardResponse(false, "Successfully retrieved reservations given no parameters", new ComplexReservations(getAllReservations(userId, req.getStart(), req.getEnd())));
        }

        // get all matching reservations first according to resource ids
        Set<Integer> reservationIds = new HashSet<Integer>();
        if (req.getResource_ids().length == 0) {
            reservationIds.addAll(getAllReservationIds(req.getStart(), req.getEnd()));
        } else {
            for (int resourceId : req.getResource_ids()) {
                reservationIds.addAll(getReservationIdsByResourceId(resourceId, req.getStart(), req.getEnd()));
            }
        }

        List<Reservation> reservations = getReservationsByIds(reservationIds, userId);

        if (req.getRequired_tags().length == 0 && req.getExcluded_tags().length == 0) {

            return new StandardResponse(false, "Successfully retrieved reservations", new ComplexReservations(reservations));
        }

        Set<Reservation> removeSet = new HashSet<Reservation>();

        // filter out all the reservations that don't include all the required tags
        // filter out all the reservations that include any of the excluded tags
        for (Reservation r : reservations) {
            Set<String> tags = getTagsByReservation(r);
            boolean remove = false;
            for (String requiredTag : req.getRequired_tags()) {
                if (!tags.contains(requiredTag)) {
                    // remove
                    remove = true;
                }
            }
            for (String excludedTag : req.getExcluded_tags()) {
                if (tags.contains(excludedTag)) {
                    // remove
                    remove = true;
                }
            }
            if (remove) {
                removeSet.add(r);
            }
        }

        reservations.removeAll(removeSet);

        ComplexReservations reservationResponse = new ComplexReservations(reservations);
        return new StandardResponse(false, "Successfully retrieved reservations", reservationResponse);
    }

    public StandardResponse updateReservation(ReservationRequest req, int reservationId, boolean isAdmin, int userId) {
        if (!req.isValid()) {
            return new StandardResponse(true, "Request is not valid");
        }

        if (!req.isValidText()) {
            return new StandardResponse(true, "Request title or description length is not valid");
        }

        if (!req.isValidTimes()) {
            return new StandardResponse(true, "Start time after end time");
        }

        if (!reservationExists(reservationId)) {
            return new StandardResponse(true, "Reservation with that ID does not exist");
        }

        // check that each resource exists
        for (int i : req.getResource_ids()) {
            if (!resourceExists(i)) {
                return new StandardResponse(true, "Resource with id " + i + " does not exist");
            }
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

        for (int resourceId : req.getResource_ids()) {
            if (!rListInts.contains(resourceId)) {
                return new StandardResponse(true, "Cannot add new resources to reservation");
            }
        }

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

        List<TempReservationResource> approvedStatusList = getApprovedStatus(reservationId);

        Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
        for (TempReservationResource trr : approvedStatusList) {
            map.put(trr.resourceId, trr.approved);
        }

        boolean notComplete = false;
        for (Resource r : rList) {
            if (r.isRestricted() && !map.get(r.getResource_id())) {
                // if restricted and not approved
                notComplete = true;
            }
        }

        t.complete = !notComplete;

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
        emailService.scheduleEmails(reservationId);
        return new StandardResponse(false, "Reservation successfully updated", res);
    }

    private List<TempReservationResource> getApprovedStatus(int reservationId) {
        return jt.query(
                "SELECT resource_id, resource_approved FROM reservationresources WHERE reservation_id = ?;",
                new Object[]{reservationId},
                new RowMapper<TempReservationResource>() {
                    public TempReservationResource mapRow(ResultSet rs, int rowNum) throws SQLException {
                        TempReservationResource t = new TempReservationResource();
                        t.resourceId = rs.getInt("resource_id");
                        t.approved = rs.getBoolean("resource_approved");
                        return t;
                    }
                });
    }

    private class TempReservationResource {
        private int resourceId;
        private boolean approved;
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


    private boolean canReserveWithSharedInTimespan(int resourceId, Timestamp beginTime, Timestamp endTime){
        int shared_count = resourceService.getSharedCount(resourceId);
        if(shared_count == 0){
            return true;
        }
        else{
            //Get list of all overlapping, complete reservations which use this resource.
            List<TempRes> overlappingCompleteWithSameResource = jt.query(
                "SELECT * from reservations, reservationresources WHERE reservations.reservation_id = reservationresources.reservation_id " + 
                "AND reservations.complete = true AND reservationresources.resource_id = ?" +
                " AND ((reservations.begin_time >= ? AND reservations.begin_time < ?)" + 
                " OR (reservations.end_time > ? AND reservations.end_time <= ?)" + 
                " OR (reservations.end_time >= ? AND reservations.begin_time <= ?));",
                new Object[]{resourceId, beginTime, endTime, beginTime, endTime, endTime, beginTime},
                new RowMapper<TempRes>() {
                    public TempRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                        TempRes t = new TempRes();
                        t.reservation_id = rs.getInt("reservations.reservation_id");
                        t.title = rs.getString("title");
                        t.description = rs.getString("description");
                        t.user_id = rs.getInt("user_id");
                        t.begin_time = rs.getTimestamp("begin_time");
                        t.end_time = rs.getTimestamp("end_time");
                        t.should_email = rs.getBoolean("should_email");
                        t.complete = rs.getBoolean("complete");
                        return t;
                    }
                }
                );
            //Need to find the max overlap in this list of reservations. Max overlap must occur
            //during the start time of at least one reservation. We should only check start times after
            //the start time of the potential reservation, however. 
            int maxOverlap = 0;
            for(TempRes r : overlappingCompleteWithSameResource){
                if(r.begin_time.before(beginTime)){
                    continue;
                }
                int overlapping = jt.queryForObject(
                    "SELECT COUNT(*) FROM reservations, reservationresources WHERE reservations.reservation_id = reservationresources.reservation_id " + 
                    "AND reservations.reservation_id != ? AND reservations.complete = true AND reservationresources.resource_id = ?" +
                    " AND (? >= reservations.begin_time AND ? < reservations.end_time);",
                    new Object[]{r.reservation_id, resourceId, r.begin_time, r.begin_time},
                    Integer.class
                    );
                if((overlapping + 1) > maxOverlap){
                    //You add 1 here to accomodate the fact that the count doesn't consider the reservation that is the base of the query
                    maxOverlap = overlapping + 1;
                }
            }

            //Must also check the start time of this potential new reservation
            int overlapping = jt.queryForObject(
                    "SELECT COUNT(*) FROM reservations, reservationresources WHERE reservations.reservation_id = reservationresources.reservation_id " + 
                    "AND reservations.complete = true AND reservationresources.resource_id = ?" +
                    " AND (? >= reservations.begin_time AND ? < reservations.end_time);",
                    new Object[]{resourceId, beginTime, beginTime},
                    Integer.class
                    );
            if(overlapping > maxOverlap){
                maxOverlap = overlapping;
            }

            return maxOverlap < shared_count;
        }
    }

    private int findMaximumConcurrentOnResource(int resourceId, TempRes r, Timestamp originalStart, Timestamp originalEnd){
        int shared_count = resourceService.getSharedCount(resourceId);
        Timestamp beginTime = r.begin_time;
        Timestamp endTime = r.end_time;

        //Get list of all overlapping, complete reservations which use this resource.
        List<TempRes> overlappingCompleteWithSameResource = jt.query(
            "SELECT * from reservations, reservationresources WHERE reservations.reservation_id = reservationresources.reservation_id " + 
            "AND reservations.complete = true AND reservationresources.resource_id = ?" +
            " AND ((reservations.begin_time >= ? AND reservations.begin_time < ?)" + 
            " OR (reservations.end_time > ? AND reservations.end_time <= ?)" + 
            " OR (reservations.end_time >= ? AND reservations.begin_time <= ?));",
            new Object[]{resourceId, beginTime, endTime, beginTime, endTime, endTime, beginTime},
            new RowMapper<TempRes>() {
                public TempRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                    TempRes t = new TempRes();
                    t.reservation_id = rs.getInt("reservations.reservation_id");
                    t.title = rs.getString("title");
                    t.description = rs.getString("description");
                    t.user_id = rs.getInt("user_id");
                    t.begin_time = rs.getTimestamp("begin_time");
                    t.end_time = rs.getTimestamp("end_time");
                    t.should_email = rs.getBoolean("should_email");
                    t.complete = rs.getBoolean("complete");
                    return t;
                }
            }
        );

        //Need to find the max overlap in this list of reservations. Max overlap must occur
        //during the start time of at least one reservation. We should only consider 'max points'
        //that are BOTH during the current reservation (temp) and during the to-be-approved
        int maxOverlap = 0;
        for(TempRes temp : overlappingCompleteWithSameResource){
            if(temp.begin_time.before(beginTime)){
                    continue;
            }
            if(temp.begin_time.before(originalStart) || temp.begin_time.after(originalEnd)){
                continue;
            }

            int overlapping = jt.queryForObject(
                "SELECT COUNT(*) FROM reservations, reservationresources WHERE reservations.reservation_id = reservationresources.reservation_id " + 
                "AND reservations.reservation_id != ? AND reservations.complete = true AND reservationresources.resource_id = ?" +
                " AND (? >= reservations.begin_time AND ? < reservations.end_time);",
                new Object[]{temp.reservation_id, resourceId, temp.begin_time, temp.begin_time},
                Integer.class
                );
            if((overlapping + 1) > maxOverlap){
                //You add 1 here to accomodate the fact that the count doesn't consider the reservation that is the base of the query
                maxOverlap = overlapping + 1;
            }
        }

        if(beginTime.after(originalStart)){
            int overlapping = jt.queryForObject(
                    "SELECT COUNT(*) FROM reservations, reservationresources WHERE reservations.reservation_id = reservationresources.reservation_id " + 
                    "AND reservations.complete = true AND reservationresources.resource_id = ?" +
                    " AND (? >= reservations.begin_time AND ? < reservations.end_time);",
                    new Object[]{resourceId, beginTime, beginTime},
                    Integer.class
                    );
            if(overlapping > maxOverlap){
                maxOverlap = overlapping;
            }
        }

        if(originalStart.after(beginTime) && originalStart.before(endTime)){
            int overlapping = jt.queryForObject(
                    "SELECT COUNT(*) FROM reservations, reservationresources WHERE reservations.reservation_id = reservationresources.reservation_id " + 
                    "AND reservations.complete = true AND reservationresources.resource_id = ?" +
                    " AND (? >= reservations.begin_time AND ? < reservations.end_time);",
                    new Object[]{resourceId, originalStart, originalStart},
                    Integer.class
                    );
            if(overlapping > maxOverlap){
                maxOverlap = overlapping;
            }
        }

        return maxOverlap;

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
        if(currentRes.complete){
            return new StandardResponse(true, "Reservation is already approved");
        }
        System.out.println("Checking to be canceled");
        List<TempRes> overlapping = getOverlappingIncompleteReservations(currentRes);
        List<Reservation> overlappingReservations = convertTempListToReservationList(overlapping);
        return new StandardResponse(false, "To-be-canceled reservations returned", overlappingReservations);
    }

    //Approves or denies the resources for a given reservation that the user is allowed to approve/deny. 
    public StandardResponse approveReservation(ReservationApproval approval, int reservationId, int userId, boolean hasResourceP){
        if(approval.getApproved()){
            //for all resources in reservation that are not approved, if included in user permission, alter
            //then check if the reservation is 100% completed, if so update it. 

            //If admin or if has resource permission, automatically aprove entire reservation. 
            if(userId == 1 || hasResourceP){
                fullyApproveReservation(reservationId);
                return new StandardResponse(false, "Reservation approved for resources you have permission on");
            }   
            else{
                List<Integer> resourceManagerResources = permissionService.getAllRestrictedResourceManagerResources(userId);
                partiallyApproveReservation(reservationId, resourceManagerResources);
                return new StandardResponse(false, "Reservation approved for resources you have permission on");
            }
        }
        else{
            //Delete the reservation "as an admin" to guarentee it is deleted. 
            emailService.sendDeniedEmail(reservationId);
            deleteReservation(reservationId, true, 1);
            return new StandardResponse(false, "Reservation denied and deleted");
        }
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
        Set<TempRes> finalOutput = new HashSet<TempRes>();

        List<Resource> originalResources = getResources(t.reservation_id);
        for(Resource r : originalResources){
            //If unlimited, we don't care about overlaps on this resource
            int shared_count = resourceService.getSharedCount(r.getResource_id());
            if(shared_count == 0){
                continue;
            }

            List<TempRes> incompleteReservationsWithResource = jt.query(
            "SELECT * from reservations, reservationresources WHERE reservations.reservation_id = reservationresources.reservation_id " + 
            "AND reservations.reservation_id != ? AND reservations.complete = false AND reservationresources.resource_id = ?" +
            " AND ((reservations.begin_time >= ? AND reservations.begin_time < ?)" + 
            " OR (reservations.end_time > ? AND reservations.end_time <= ?)" + 
            " OR (reservations.end_time >= ? AND reservations.begin_time <= ?));",
            new Object[]{t.reservation_id, r.getResource_id(), t.begin_time, t.end_time, t.begin_time, t.end_time, t.end_time, t.begin_time},
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

            //FOR each of these reservations, 
            //find maximum number of concurrent reserves on this resource (so long as that max number occurs during
            //the time frame of the potential reservation)
            //if max number is greater than shared count, this will be removed. 

            for(TempRes temp : incompleteReservationsWithResource){
                if(findMaximumConcurrentOnResource(r.getResource_id(), temp, t.begin_time, t.end_time) >= shared_count - 1){
                    finalOutput.add(temp);
                }
            }
        }

        List<TempRes> returnObject = new ArrayList<TempRes>();
        returnObject.addAll(finalOutput);

        return returnObject;
    }

    private void fullyApproveReservation(int reservationId){
        String resourceUpdateString = "UPDATE reservationresources SET resource_approved = true WHERE reservation_id = " + reservationId +
            ";";
        jt.update(resourceUpdateString);

        String reservationUpdateString = "UPDATE reservations SET complete = true WHERE reservation_id = " + reservationId + ";";
        jt.update(reservationUpdateString);

        emailService.scheduleEmails(reservationId);
        deleteOverlappingIncompleteReservations(reservationId);
    }

    private void partiallyApproveReservation(int reservationId, List<Integer> approvableResources){
        Map<String,List<Integer>> params = Collections.singletonMap("ids", approvableResources);

       // String resourceUpdateString = "UPDATE reservationresources SET resource_approved = true WHERE reservation_id = " + reservationId +
        //    " AND reservation_id IN (:ids);";
        String resourceUpdateString = "UPDATE reservationresources SET resource_approved = true WHERE reservation_id = " + reservationId +
            " AND resource_id IN (";

        for(int i = 0; i < approvableResources.size(); i++){
            resourceUpdateString = resourceUpdateString + approvableResources.get(i);
            if(i < approvableResources.size() - 1 ){
                resourceUpdateString = resourceUpdateString + ", ";
            }
        }
        resourceUpdateString = resourceUpdateString + ");";

        jt.update(resourceUpdateString);


        String shouldUpdateReservation = "SELECT resource_id FROM reservationresources where reservation_id = " + reservationId +
            " AND resource_approved = false;";
        List<Integer> remainingUnnaproved = jt.query(shouldUpdateReservation,
                        new RowMapper<Integer>() {
                            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                                return rs.getInt("resource_id");
                            }
                     });

        //Some remaining resources must be approved, do not change reservation status
        if(remainingUnnaproved.size() > 0){
            return;
        }
        else{ //all resources approved, can modify reservation status
            String reservationUpdateString = "UPDATE reservations SET complete = true WHERE reservation_id = " + reservationId + ";";
            jt.update(reservationUpdateString);
            emailService.scheduleEmails(reservationId);
            deleteOverlappingIncompleteReservations(reservationId);
        }
    }

    private void deleteOverlappingIncompleteReservations(int reservationId){
        TempRes t = getTempResFromId(reservationId);
        List<TempRes> overlappingIncomplete = getOverlappingIncompleteReservations(t);
        for(TempRes toCancel : overlappingIncomplete){
            emailService.sendCanceledEmail(toCancel.reservation_id);
            deleteReservation(toCancel.reservation_id, true, 1);
        }
    }

}