package responses.data;

/**
 * Created by Davis Treybig
 */

public class UserWithID {
    private Integer user_id;
    private String email;
    private String username;
    private boolean should_email;

    public UserWithID(int user_id, String email, String username, boolean should_email) {
        this.user_id = user_id; 
        this.email = email;
        this.username = username;
        this.should_email = should_email;
    }

    public UserWithID(User user, int user_id){
        this.user_id = user_id;
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.should_email = user.isShould_email();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setShould_email(boolean should_email) {
        this.should_email = should_email;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public boolean isShould_email() {
        return should_email;
    }

    public int getUser_id(){
        return user_id;
    }
    public void setUser_id(int user_id){
        this.user_id = user_id;
    }
}
