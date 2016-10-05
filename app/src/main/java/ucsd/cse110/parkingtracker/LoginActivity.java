package ucsd.cse110.parkingtracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;

/**
 * Login Activity using an Authenticator
 */
public class LoginActivity extends AppCompatActivity {

    private Authenticator authenticator;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authenticator = new UCSDAuthenticator();

        try {
            Parse.initialize(this,
                    getString(R.string.parse_applicationID),
                    getString(R.string.parse_clientKey));
        } catch (Exception e) {
            Log.d("onCreate Main", e.getMessage());
        }

        etEmail = (EditText) findViewById(R.id.main_editText_email);
        etPassword = (EditText) findViewById(R.id.main_editText_password);

        //Attempt to automatically log in bypassing authenticator
        ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
            @Override
            public void done(ParseSession object, ParseException e) {
                try {
                    // Check if current session is found on the Parse servers
                    ParseQuery<ParseSession> query = ParseQuery.getQuery("_Session");
                    if (query.get(object.getObjectId()) == null) {
                        throw new Exception("Invalid Session");
                    }
                } catch (Exception exception) {
                    // Session invalid, force any current local user out of the system
                    // User must log in
                    Log.d("AutoLogin errorexc", "Invalid session?");
                    ParseUser.logOut();
                    return;
                }
                // Get locally saved user
                ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    if (e == null) {
                        // If user exists and session is valid, check if email is verified
                        if (user.getBoolean("emailVerified")) {
                            Toast.makeText(getApplicationContext(), "Welcome, " + user.getUsername() + "!", Toast.LENGTH_LONG).show();
                            showMap();
                        } else {
                            // User is not email-verified. Force logout
                            Toast.makeText(getApplicationContext(), "Please confirm your E-mail address before log-in.", Toast.LENGTH_LONG).show();
                            ParseUser.logOut();
                        }
                    } else {
                        // User does not exist. Force logout
                        try {
                            ParseUser.logOut();
                        } catch (Exception e2) {
                            Log.d("AutoLogin error2", e2.getMessage());
                        }
                        Log.d("AutoLogin error1", e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * signIn: onClick method for signing in
     *          uses the provided Authenticator to sign in
     *          Requires an initialized Authenticator
     * @param view  The button that was clicked.
     */
    public void signIn(View view) {
        Log.d("signIn", view.toString());
        String email    = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        final Context context = getApplicationContext();

        try {
            authenticator.logIn(email, password);
            Toast.makeText(context, "Welcome, " + email + "!", Toast.LENGTH_LONG).show();
            showMap();
            etEmail.setText("");
            etPassword.setText("");
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("signIn", e.getMessage());
        }
    }

    /**
     * register: onClick method for registration
     *            opens the Registration activity for a result
     * @param view  The button that was clicked.
     */
    public void register(View view) {
        Intent intent = new Intent(getApplicationContext(), Registration.class);
        startActivityForResult(intent, getResources().getInteger(R.integer.REQUEST_MAIN_REGISTER));
        etEmail.setText("");
        etPassword.setText("");
    }

    /**
     * resetPassword: onClick method for resetting password
     *                 opens the Reset activity
     * @param view  The button that was clicked
     */
    public void resetPassword(View view) {
        Intent intent = new Intent(getApplicationContext(), Reset.class);
        startActivity(intent);
        etEmail.setText("");
        etPassword.setText("");
    }

    /**
     * showMap: starts the ShowMapActivity activity
     */
    private void showMap() {
        Intent intent = new Intent(this, ShowMapActivity.class);
        startActivity(intent);
    }

    /**
     * onActivityResult: Processes the result from any startActivityForResult() calls.
     * @param requestCode   Code provided on startActivityForResult()
     * @param resultCode    Code returned from activity with setResult()
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Resources resources = getResources();
        if (requestCode == resources.getInteger(R.integer.REQUEST_MAIN_REGISTER)) {
            if (resultCode == resources.getInteger(R.integer.RESULT_MAIN_REGISTER_SUCCESS)) {
                Toast.makeText(this, "Thanks! You must verify your email before logging in.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
