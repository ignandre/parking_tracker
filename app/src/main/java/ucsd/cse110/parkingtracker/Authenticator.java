package ucsd.cse110.parkingtracker;

/**
 * Interface for checking usernames and passwords with login and register functions.
 */
public interface Authenticator {

    /**
     * Implementation should check if a given username is valid
     * @param username    Given username
     * @return  True if valid, false otherwise
     */
    boolean checkValidUsername(String username);

    /**
     * Implementation should check if a given password is valid
     * @param password    Given password
     * @return  True if valid, false otherwise
     */
    boolean checkValidPassword(String password);

    /**
     * Implementation takes a username and password and attempts to log in
     * @param username    Given username
     * @param password    Given password
     * @throws Exception with details if unsuccessful
     */
    void logIn(String username, String password) throws Exception;

    /**
     * Implementation takes a username and password and attempts to register
     * @param username    Given username
     * @param password    Given password
     * @throws Exception with details if unsuccessful
     */
    void register(String username, String password) throws Exception;

}