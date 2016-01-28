package responses;

/**
 * Created by jiaweizhang on 1/18/2016.
 */

public class StandardResponse {
    private Boolean is_error;
    private String error_msg;
    private Object data;

    public StandardResponse(Boolean is_error, String message, Object data) {
        this.is_error = is_error;
        this.error_msg = message;
        this.data = data;
    }

    public StandardResponse(Boolean is_error, String error_msg) {
        this(is_error, error_msg, null);
    }

    public Boolean getIs_error() {
        return is_error;
    }

    public Object getData() {
        return data;
    }

    public String getError_msg() {
        return error_msg;
    }
}
