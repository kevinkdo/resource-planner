package resourceplanner.models;

/**
 * Created by jiaweizhang on 2/22/16.
 */

public class OAuth {
    private String eppn;
    private String error;

    public String getEppn() {
        return eppn;
    }

    public void setEppn(String eppn) {
        this.eppn = eppn;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
