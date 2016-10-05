package ucsd.cse110.parkingtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Activity to change user settings and logout.
 */
public class Settings extends AppCompatActivity {

    private String parkingType;
    RadioGroup radioGroup;
    RadioButton radioButton1;
    RadioButton radioButton2;
    RadioButton radioButton3;
    RadioButton radioButton4;
    Button button;
    EditText etChangePassword, etReenterPassword;

    ParseUser user;

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        try {
            Parse.initialize(this,
                    getString(R.string.parse_applicationID),
                    getString(R.string.parse_clientKey));
        } catch (Exception e) {
            Log.d("onCreate Settings", e.toString());
        }

        initialize();
    }

    /**
     * Initialize member variables to default values.
     */
    private void initialize() {
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        radioButton3 = (RadioButton) findViewById(R.id.radioButton3);
        radioButton4 = (RadioButton) findViewById(R.id.radioButton4);
        button = (Button) findViewById(R.id.settings_button_changePassword);
        etChangePassword = (EditText) findViewById(R.id.etChangePassword);
        etReenterPassword = (EditText) findViewById(R.id.etReenterPassword);
        user = ParseUser.getCurrentUser();
        try {
            user.fetch();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        switch (user.getString("parkingType")) {
            case "S":
                radioGroup.check(R.id.radioButton1);
                break;
            case "B":
                radioGroup.check(R.id.radioButton2);
                break;
            case "A":
                radioGroup.check(R.id.radioButton3);
                break;
            case "V/VP":
                radioGroup.check(R.id.radioButton4);
                break;
            default:
                radioGroup.check(R.id.radioButton1);
                break;
        }
    }

    /**
     * onClick handler for Set Parking Type button.
     * Gets the selected radio button and sets the current user's parkingType to its value.
     * @param view    Set Parking Type button id.settings_button_setParkingType
     */
    public void settype (View view) {
        if (radioButton1.isChecked())
            parkingType = "S";
        else if (radioButton2.isChecked())
            parkingType = "B";
        else if (radioButton3.isChecked())
            parkingType = "A";
        else if (radioButton4.isChecked())
            parkingType = "V/VP";

        try {
            user.fetch();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        user.put("parkingType", parkingType);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "Parking Preference is updated.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * onClick handler for Change Password button.
     * Checks if the inputs are equal and changes the current user's password to the input.
     * @param view    Change Password button id settings_button_changePassword
     */
    public void changePassword(View view) {
        String password = etChangePassword.getText().toString();
        String reenter = etReenterPassword.getText().toString();

        if (password.equals(reenter)) {
            user.setPassword(password);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(getApplicationContext(), "Password is updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(),"Passwords do not match. Please Try again.",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * onClick handler for Logout button.
     * Asks the user if they wish to log out with an alert dialog.
     * If the user accepts, the app will log the user out and return to the login page.
     * <br>
     * Also sets alertDialog to the created alertDialog.
     * @param view    Logout button id settings_button_logout
     */
    public void logout(View view) {
        alertDialog = new AlertDialog.Builder(this)
                .setMessage("Logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Settings.this.alertDialog = null;
                        Settings.this.setResult(getResources().getInteger(R.integer.REQUEST_SETTINGS));
                        Settings.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Settings.this.alertDialog = null;
                    }
                })
                .create();
        alertDialog.show();
    }

    /**
     * Gets the last created AlertDialog, if it is still available.
     * @return  An alert dialog if it exists or null.
     */
    public AlertDialog getLastDialog() {
        return alertDialog;
    }
}
