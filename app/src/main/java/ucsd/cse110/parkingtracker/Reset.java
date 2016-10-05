package ucsd.cse110.parkingtracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

/**
 * Reset Password activity
 */
public class Reset extends Activity {
    Button bReset;
    EditText etResetEmail, etReenterEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        etResetEmail = (EditText) findViewById(R.id.reset_editText_email);
        etReenterEmail = (EditText) findViewById(R.id.reset_editText_emailRepeat);
        bReset = (Button) findViewById(R.id.reset_button_reset);
    }

    /**
     * Reset Password onClick handler.
     * Checks if the password is entered twice correctly and changes the user's password.
     * @param view    Reset Password button
     */
    public void reset (View view) {
        String resetEmail = etResetEmail.getText().toString();
        String reenter = etReenterEmail.getText().toString();

        if (resetEmail.equals(reenter)) {
            ParseUser.requestPasswordResetInBackground(resetEmail, new RequestPasswordResetCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null) {
                        Toast.makeText(Reset.this, "Please check your E-mail to reset your password.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else {
                        Toast.makeText(Reset.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(Reset.this, "E-mail addresses don't match. Please try again.", Toast.LENGTH_LONG).show();
        }

    }

}
