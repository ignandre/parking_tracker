package ucsd.cse110.parkingtracker;

import android.util.Log;

import com.parse.ParseUser;

/**
 * Authenticator using Parse services, tailored for UCSD students.
 */
public class UCSDAuthenticator implements Authenticator {

    public UCSDAuthenticator() {
    }

    /**
     * Check if a username is valid.
     * @param username  Some string
     * @return  true if username is a UCSD email (contains ucsd.edu)
     */
    @Override
    public boolean checkValidUsername(String username) {
        return username.contains("ucsd.edu");
    }

    /**
     * Check if a password is valid.
     * @param password  Some string
     * @return  true if password is not empty
     */
    @Override
    public boolean checkValidPassword(String password) {
        return password.length() > 0;
    }

    /**
     * logIn takes a username and password and attempts to log in to the Parse server
     * Parse must be initialized from activity
     * @param username  Valid username (use checkValidUsername)
     * @param password  Valid password (use checkValidPassword)
     * @throws  Exception with message if unsuccessful
     */
    @Override
    public void logIn(String username, String password) throws Exception {
        try {
            ParseUser user = ParseUser.logIn(username, password);
            if (user != null) {
                if (!user.getBoolean("emailVerified")) {
                    throw new Exception("Email not verified");
                }
            } else {
                throw new Exception("User not found");
            }
        } catch (Exception e) {
            Log.d("logIn", e.getMessage());
            throw new Exception("Invalid credentials");
        }
    }

    /**
     * register takes a username and password and attempts to register it in the Parse server
     * Parse must be initialized from activity
     * @param username  Username
     * @param password  Password
     * @throws  Exception with message on failure
     */
    @Override
    public void register(String username, String password) throws Exception {
        try {
            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setEmail(username);
            user.setPassword(password);
            user.put("parkingType", "A");
            user.signUp();
        } catch (Exception e) {
            Log.d("register", e.getMessage());
            throw e;
        }
    }
}
