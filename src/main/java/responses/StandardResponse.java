package responses;

/**
 * Created by jiawe on 1/18/2016.
 */
public class StandardResponse {
    private Boolean is_error;
    private String error_msg;
    private Object committed;
    private Object data;

    public StandardResponse(Boolean is_error, String message, Object committed, Object data) {
        this.is_error = is_error;
        this.error_msg = message;
        this.committed = committed;
        this.data = data;
    }

    public StandardResponse(Boolean is_error, String error_msg, Object committed) {
        this(is_error, error_msg, committed, null);
    }

    public StandardResponse(Boolean is_error, String error_msg) {
        this(is_error, error_msg, null, null);
    }

    public Boolean getIs_error() {
        return is_error;
    }

    public Object getCommitted() {
        return committed;
    }

    public Object getData() {
        return data;
    }

    public String getError_msg() {
        return error_msg;
    }
}
