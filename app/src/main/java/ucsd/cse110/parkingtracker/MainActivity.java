package ucsd.cse110.parkingtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.Serializable; //For ParkingStructure instantiation test

import bolts.Task;

/**
 * TODO: User stories for next iteration (Monday, November 09, 2015)
 * These are general ideas of how the features can be divided into sub-tasks.
 *
 * Category: Report function
 *      A. I can select a parking structure from the available parking structures to report about.
 *      Tasks:
 *          1. Create buttons for each ParkingStructure.
 *          2. When I click a button for a ParkingStructure,set it as the "selected" ParkingStructure.
 *          (Optional) 3. After I click a button, the sliders below show the current values
 *                          for the availability of the selected ParkingStructure.
 *
 *      B. I can choose a parking type from a dropdown box and set its availability with a slider.
 *      Tasks:
 *          1. Create a dropdown box to select the parking type.
 *          2. Create a slider to adjust the parking availability.
 *          To make it work with C, generate these buttons in the activity itself that contains a ScrollView
 *              and put the box/slider in a LinearLayout or some other Layout so we can add multiple sliders.
 *
 *      C. I can add more sliders to report multiple types of parking at once.
 *
 * Category: User Interface
 *      D. I can't see anything, it's all too small!
 *          1. Adjust UI elements depending on size, possibly as a percentage of screen or using weights/hints.
 *
 *      E. The map is centered improperly.
 *          1. Change the map centering.
 *
 * TODO: Design the app so that the back buttons work better.
 *
 */

public class MainActivity extends AppCompatActivity {

    private Boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);
        if (loggedIn) {
            showMap();
        } else {
            try {
                Parse.initialize(this, "yTLxIh9EyPiJlh0wI4WWqrlEGIW2nN7r5HoXgnks", "mNJRwbiS1ebk0O32aZeXlbJxjsEEF6oFfEarTRct");
            } catch (Exception e) {
                Log.d("Main OnCreate", e.toString());
            }
        }
    }

    /*
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvRegisterLink:
                startActivity(new Intent(this, Register.class));
                break;
            case R.id.bLogin:
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                Verification verifyUser = new Verification(email, password);
                if (verifyUser.verify() == true)
                    etEmail.setText("verify");
                else
                    etEmail.setText("false");
                break;
        }
    }
    */

    public void signIn(View view) {
        // If signin succeeds, call showMap()
        String email = ((EditText) findViewById(R.id.etEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();
        Authenticator user = new Authenticator(email, password);
        Context context = getApplicationContext();

        if (user.verify()) {
            Toast.makeText(context, "Welcome, " + email, Toast.LENGTH_LONG).show();
            loggedIn = true;
            showMap();
        } else {
            // Else make a popup saying "Invalid information" or something
            Toast.makeText(context, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
        }
    }

    public void register(View view) {
        Intent intent = new Intent(getApplicationContext(), Registration.class);
        startActivity(intent);
    }

    private void showMap() {
        Intent intent = new Intent(this, ShowMapActivity.class);
        startActivity(intent);
    }

    //Test ParkingInfo Activity from login screen
    /*
    public void showParking(View view)
    {
        try {
            Intent intent = new Intent(this, ParkingInfo.class);
            intent.putExtra("Parking_Structure_Object", (Serializable)mGilmanTest);
            startActivity(intent);
        } catch (Exception e) {
            Log.d("Failed on showParking", e.getMessage());
            Log.d("StackTrace?", Log.getStackTraceString(e));
        }
    }
    */
}
