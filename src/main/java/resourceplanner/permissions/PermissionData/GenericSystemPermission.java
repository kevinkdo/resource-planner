package resourceplanner.permissions.PermissionData;

/**
 * Created by Davis Treybig
 */

public class GenericSystemPermission {
    private boolean resource_p;
    private boolean reservation_p;
    private boolean user_p;

    public GenericSystemPermission(boolean resource_p, boolean reservation_p, boolean user_p){
        this.resource_p = resource_p;
        this.reservation_p = reservation_p;
        this.user_p = user_p;
    }

    public boolean getResource_p(){
        return resource_p;
    }

    public boolean getReservation_p(){
        return reservation_p;
    }

    public boolean getUser_p(){
        return user_p;
    }
}