package resourceplanner.search.responsedata;

/**
 * Created by jiaweizhang on 3/24/16.
 */
public class SearchReservation {
    private int reservation_id;
    private String title;
    private String description;

    public int getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(int reservation_id) {
        this.reservation_id = reservation_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
