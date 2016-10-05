package ucsd.cse110.parkingtracker;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseUser;

public class LoginActivityTester extends ActivityInstrumentationTestCase2<LoginActivity> {

    private LoginActivity mLoginActivity;
    private EditText mEmail, mPassword;
    private Button mLogin;

    public LoginActivityTester() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(true);

        mLoginActivity = getActivity();

        mEmail = (EditText) mLoginActivity.findViewById(R.id.main_editText_email);
        mPassword = (EditText) mLoginActivity.findViewById(R.id.main_editText_password);
        mLogin = (Button) mLoginActivity.findViewById(R.id.main_button_login);
    }

    @Override
    protected void tearDown() throws Exception {
        ParseUser.logOut();
        super.tearDown();
    }


    public void testPreconditions() {
        assertNotNull("Main Activity is null", mLoginActivity);
        assertEquals("", mEmail.getText().toString());
        assertEquals("", mPassword.getText().toString());
        assertNotNull(mLogin);
    }


    /**
     * Given: I am on login page
     * When: I entered invalid username and/or password
     * Then: The application stays on login page.
     */
    public void test_Login_InvalidCredentials() {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ShowMapActivity.class.getName(), null, false);
        //--GIVEN--
        given_onLoginPage();
        //--WHEN--
        when_enteredInvalidInfo();
        //--THEN--
        then_applicationOnLogin(activityMonitor);
    }

    //Given: I am on login page
    public void given_onLoginPage() {
        assertNotNull("Main Activity is null", mLoginActivity);
        assertEquals("", mEmail.getText().toString());
        assertEquals("", mPassword.getText().toString());
        assertNotNull(mLogin);
    }

    //When: I entered invalid username and/or password
    public void when_enteredInvalidInfo(){
        mLoginActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmail.setText("");
                mPassword.setText("");
                mLogin.performClick();
            }
        });
    }

    //Then: The application stays on login page.
    public void then_applicationOnLogin(Instrumentation.ActivityMonitor am) {
        ShowMapActivity showMapActivity = (ShowMapActivity) getInstrumentation().waitForMonitorWithTimeout(am, 3000);
        assertNull(showMapActivity);
    }


    /**
     * Given: I am in login page
     * When: I entered valid username and password
     * Then: The application shows the google map  with correct user information.
     */
    public void test_Login_ValidCredentials() {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ShowMapActivity.class.getName(), null, false);
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        //--GIVEN--
        given_onLoginPage();
        //--WHEN--
        when_enteredValidInfo();
        //--THEN--
        then_showMap(email, password, activityMonitor);
    }

    //When: I entered valid username and password
    public void when_enteredValidInfo(){
        mLoginActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmail.setText("schoang@ucsd.edu");
                mPassword.setText("password");
                mLogin.performClick();
            }
        });
    }

    //Then: The application shows the google map  with correct user information.
    public void then_showMap(String email, String password, Instrumentation.ActivityMonitor am) {
        ShowMapActivity showMapActivity = (ShowMapActivity) getInstrumentation().waitForMonitorWithTimeout(am, 3000);
        assertNotNull(showMapActivity);
        showMapActivity.logout();

        assertEquals(email, mEmail.getText().toString());
        assertEquals(password, mPassword.getText().toString());

    }

    /**
     * Given: I am in login page
     * When: I start registration process
     * Then: The application shows the registration page.
     */
    public void test_StartRegistration() {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(Registration.class.getName(), null, false);
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();

        //--GIVEN--
        given_onLoginPage();
        //--WHEN--
        when_startRegistration();
        //--THEN--
        then_showRegistration(email, password, activityMonitor);

    }

    //When: I start registration process
    public void when_startRegistration(){
        mLoginActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoginActivity.findViewById(R.id.main_textView_register).performClick();
            }
        });
    }

    //Then: The application shows the registration page.
    public void then_showRegistration(String email, String password, Instrumentation.ActivityMonitor activityMonitor) {
        Registration registration = (Registration) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 3000);
        assertNotNull(registration);
        registration.finish();

        assertEquals(email, mEmail.getText().toString());
        assertEquals(password, mPassword.getText().toString());
    }

    /**
     * Given: I am in login page
     * When: I start forgot password process
     * Then: The application shows the reset page.
     */
    public void test_StartForgotPassword() {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(Reset.class.getName(), null, false);

        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();

        //--GIVEN--
        given_onLoginPage();
        //--WHEN--
        when_startForgotPassword();
        //--THEN--
        then_showReset(email, password, activityMonitor);

    }

    //When: I start forgot password process
    public void when_startForgotPassword(){
        mLoginActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoginActivity.findViewById(R.id.main_textView_resetPassword).performClick();
            }
        });
    }

    //Then: The application shows the reset page.
    public void then_showReset(String email, String password, Instrumentation.ActivityMonitor activityMonitor){
        Reset reset = (Reset) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 3000);
        assertNotNull(reset);
        reset.finish();

        assertEquals(email, mEmail.getText().toString());
        assertEquals(password, mPassword.getText().toString());
    }

}
