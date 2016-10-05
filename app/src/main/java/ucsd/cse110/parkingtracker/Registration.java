package ucsd.cse110.parkingtracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Registration activity using an Authenticator
 */
public class Registration extends Activity {

    Button bRegister;
    EditText etPassword, etEmail;

    Authenticator authenticator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etEmail = (EditText) findViewById(R.id.registration_editText_email);
        etPassword = (EditText) findViewById(R.id.registration_editText_password);
        bRegister = (Button) findViewById(R.id.registration_button_register);

        authenticator = new UCSDAuthenticator();
    }

    /**
     * register: onClick for registration
     *            Attempts to register an email/password combination using an Authenticator
     *            Authenticator must be initialized before use
     * @param view  The button that was clicked
     */
    public void register(View view) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (authenticator.checkValidUsername(email)) {
            if (authenticator.checkValidPassword(password)) {
                try {
                    authenticator.register(email, password);
                    setResult(getResources().getInteger(R.integer.RESULT_MAIN_REGISTER_SUCCESS));
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Invalid password.", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "Non-UCSD E-mail. Please enter a valid UCSD E-mail.", Toast.LENGTH_LONG).show();
        }
    }
}
