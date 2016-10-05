package ucsd.cse110.parkingtracker;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Displays a parking structure passed in with an Intent.
 */
public class ParkingInfo extends AppCompatActivity {

    //private String parkingType;
    private TableLayout mainLayout;
    private List<Integer> availability_info;

    private ParkingStructure parkingStructure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_parkingstructure);

        try {
            Parse.initialize(this,
                    getString(R.string.parse_applicationID),
                    getString(R.string.parse_clientKey));
        } catch (Exception e) {
            Log.d("onCreate parkingInfo", e.toString());
        }

        // Get the serialized Parking_Structure object by pulling the info out of the intent that
        // called this activity.
        try {
            parkingStructure = (ParkingStructure) getIntent().getSerializableExtra("Parking_Structure_Object");
            /*mainLayout where we will add our floor and information table into*/

            TextView title = (TextView) findViewById(R.id.parkingInfo_textView_title);
            title.setText(parkingStructure.getName());

            mainLayout = (TableLayout) findViewById(R.id.table_layout);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            availability_info = parkingStructure.getAvailability();

            //get parse UpdatedAt
            final Date parseUpdatedAt = parkingStructure.getUpdatedTime();

            ParseUser user = ParseUser.getCurrentUser();
            user = user.fetch();

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            try {
                if (day == Calendar.SATURDAY || day == Calendar.SUNDAY ||
                        hour > 16 || hour < 7 ||
                        (hour == 16 && minute > 30) ||
                        (hour == 7 && minute < 30)) {
                    addParkingInfoCell("V/VP");
                    addParkingInfoCell("A");
                    addParkingInfoCell("B");
                    addParkingInfoCell("S");
                } else {
                    addParkingInfoCell("V/VP");
                    switch (user.getString("parkingType")) {
                        case "V/VP":
                            addParkingInfoCell("B");
                            addParkingInfoCell("S");
                            break;
                        case "A":
                            addParkingInfoCell("A");
                            addParkingInfoCell("B");
                            addParkingInfoCell("S");
                            break;
                        case "B":
                            addParkingInfoCell("B");
                            addParkingInfoCell("S");
                            break;
                        case "S":
                            addParkingInfoCell("S");
                            break;
                    }
                }
            } catch (Exception e) {
                Log.d("ParkingInfo", e.getMessage());
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            TextView tv_updated_time = (TextView) findViewById(R.id.updated_time);

            //set format of updated time
            SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm z", Locale.US);
            //change timezone
            formattedDate.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            String AfterFormatedd = formattedDate.format(parseUpdatedAt);
            tv_updated_time.setText(AfterFormatedd);
        } catch (Exception e) {
            Log.d("ParkingInfoOnCreate", e.getMessage());
            Toast.makeText(getApplicationContext(), "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Add cell into ParkingInfo for ParkingType s.
     * Background and text color will be set and availability will be filled accordingly.
     */
    /**
     * Add cell into display for a ParkingType s.
     * Background, text color, and availability will be set according to the current parking structure.
     * @param s    A valid parking type in the parking structure (S, A, B, V/VP)
     */
    public void addParkingInfoCell(String s){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.floor_layout_single, null);
        TextView tv_text = (TextView) view.findViewById(R.id.text);
        TextView tv_availability = (TextView) view.findViewById(R.id.availability);
        TextView tv_indicator = (TextView) view.findViewById(R.id.indicator);

        switch (s) {
            case "V/VP":
                tv_availability.setText(Integer.toString(availability_info.get(0)));
                break;
            case "A":
                tv_text.setText(s);
                tv_text.setTextColor(0xFFFFFFFF);
                tv_text.setBackgroundColor(0xFFFF0000);
                tv_availability.setText(Integer.toString(availability_info.get(1)));

                break;
            case "B":
                tv_text.setText(s);
                tv_text.setTextColor(0xFFFFFFFF);
                tv_text.setBackgroundColor(0xFF00CD63);
                tv_availability.setText(Integer.toString(availability_info.get(2)));
                break;
            case "S":
                tv_text.setText(s);
                tv_text.setTextColor(0xFF000000);
                tv_text.setBackgroundColor(0xFFFFEA00);
                tv_availability.setText(Integer.toString(availability_info.get(3)));
                break;
        }
        setIndicator(tv_availability, tv_indicator);
        mainLayout.addView(view);
    }

    /**
     * setIndicator will assign colour into the last column according to the availability.
     * In order of availability, white(empty), blue, green, yellow, orange, and red.*/
    /**
     * Assign colors to the last column of a ParkingInfo row.
     * Color ranges from red to yellow to green depending on availability.
     * @param tv_a    TextView containing the availability of a parking type
     * @param tv_i    TextView to change containing a block of color indicating availability
     */
    public void setIndicator(TextView tv_a, TextView tv_i) {
        double weightOfGreen = Double.parseDouble(tv_a.getText().toString()) / 100.0;
        int red, green;
        if (weightOfGreen >= 0.5) {
            //add scaled red + flat green
            red = (int)(0xFF * (1 - (weightOfGreen - 0.5)/0.5f)) << 16;
            green = 0xFF00;
        } else {
            // add flat red + scaled green
            red = 0xFF0000;
            green = (int)(0xFF * weightOfGreen/0.5f) << 8;
        }

        int color = 0xFF000000 + red + green;
        Log.d("ParkingInfo", "weightOfGreen is " + Double.toString(weightOfGreen));
        Log.d("ParkingInfo", "color is " + Integer.toHexString(color));

        tv_i.setBackgroundColor(color);
    }

    public ParkingStructure getParkingStructure() {
        return parkingStructure;
    }
}

