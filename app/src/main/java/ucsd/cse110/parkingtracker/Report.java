package ucsd.cse110.parkingtracker;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.widget.Button;
import android.widget.Toast;

/**
 * Activity which takes a list of ParkingStructures and allows the user to change one structure's
 *      availability.
 */
public class Report extends AppCompatActivity {
    private List<Integer> available_list;
    private boolean park_chosen;
    private ParkingStructure selected;

    private LinearLayout mainLayout;
    private ArrayList<View> viewList;
    private ArrayList<String> stringList;
    
    private ArrayList<ParkingStructure> parkingStructures;
    private ArrayList<String> parkingStructurePositions;
    private ArrayList<Button> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        try {
            Parse.initialize(this,
                    getString(R.string.parse_applicationID),
                    getString(R.string.parse_clientKey));
        } catch (Exception e) {
            Log.d("onCreate Report", e.toString());
        }
        try {
            initialize();
            Log.d("Report.psList", parkingStructures.toString());

            generateParkingStructureButtons();

            for (String s : stringList) {
                addSlider(s);
            }

            updateSeekbar();
        } catch (Exception e) {
            Log.d("onCreate Report", e.toString());
        }
    }

    /**
     * Initialize all member variables to default values.
     */
    private void initialize() {
        park_chosen = false;
        parkingStructures = (ArrayList<ParkingStructure>) getIntent().getSerializableExtra("Parking_Structures");
        parkingStructurePositions = new ArrayList<>();
        buttons = new ArrayList<>();
        viewList = new ArrayList<>();
        stringList = new ArrayList<>();
        stringList.add("V/VP");
        stringList.add("A");
        stringList.add("B");
        stringList.add("S");
        mainLayout = (LinearLayout)findViewById(R.id.parking_availability);
    }

    /**
     * Create a button for each parking structure passed in from the intent.
     */
    private void generateParkingStructureButtons() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.report_testing_array);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5,0,5,0);
        layoutParams.weight = 1.0f;
        for (ParkingStructure curr : parkingStructures) {
            Log.d("Report.it", curr.getName());
            Button button = new Button(this);
            button.setText(curr.getName());
            button.setBackgroundResource(R.drawable.button_pressed);
            button.setTextSize(36f);
            button.setPadding(10, 10, 10, 10);
            linearLayout.addView(button, layoutParams);
            parkingStructurePositions.add(curr.getName());
            buttons.add(button);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button b = (Button) v;
                    Log.d("buttonOnClick", b.getText().toString());
                    selected = parkingStructures.get(parkingStructurePositions.indexOf(b.getText().toString()));
                    park_chosen = true;
                    setButtonPressed(b);
                }
            });
            Log.d("Report.it", "after onclick");
            if (!park_chosen) {
                selected = parkingStructures.get(parkingStructurePositions.indexOf(button.getText().toString()));
                park_chosen = true;
                setButtonPressed(button);
            }
        }
    }

    /**
     * Update each seekbar with the current selected parking structure's values.
     */
    private void updateSeekbar() {
        try {
            int index = 0;
            int j = 0;
            ListIterator<View> walker = viewList.listIterator();
            View view;
            SeekBar seekBar;
            TextView seekbar_text;

            while (walker.hasNext()) {
                view = walker.next();
                seekBar = (SeekBar) view.findViewById(R.id.report_seekbar_availability);
                //Button button = (Button) view.findViewById(R.id.parkingtype_text);
                seekbar_text = (TextView) view.findViewById(R.id.seekbar_text);

                switch (stringList.get(j)) {
                    case "V/VP":
                        index = 0;
                        break;
                    case "A":
                        index = 1;
                        break;
                    case "B":
                        index = 2;
                        break;
                    case "S":
                        index = 3;
                        break;
                    default:
                        Log.d("report_updateSeekbar", "Invalid item ");
                        setResult(0x100);
                        finish();
                        break;
                }
                j++;

                if (selected != null) {
                    seekbar_text.setText(Integer.toString(selected.getAvailability().get(index)));
                    seekBar.setProgress(selected.getAvailability().get(index) / 10);
                }
            }
        } catch (Exception e) {
            Log.d("UpdateSeekbars", e.getMessage());
        }
    }

    /**
     * Set a parking structure to be the selected structure and deselects any other button.
     * @param b    A button in the set of parking structures generated on creation.
     */
    public void setButtonPressed(Button b) {
        try {
            for (Button button : buttons) {
                button.setBackgroundResource(R.drawable.button_pressed);
            }
            b.setBackgroundResource(R.drawable.button_normal);
            updateSeekbar();
        } catch (Exception e) {
            Log.d("setButtonPressed", e.getMessage());
        }
    }

    /**
     * Adds a row containing information for a given ParkingType s.
     * Information is shown as a button containing s and a slider showing the availability of
     * the s parking type in the structure.
     * @param s    A valid parkingType for the current parking structure.
     */
    private void addSlider(String s) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.availability_slider, null);

        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.report_seekbar_availability);
        final Button button = (Button) view.findViewById(R.id.report_button_parkingTypeText);
        final TextView seekbar_text = (TextView) view.findViewById(R.id.seekbar_text);

        viewList.add(view);
        mainLayout.addView(view);

        try {
            button.setText(s);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    seekbar_text.setText(Integer.toString(progress * 10));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        } catch (Exception e) {
            Log.d("addSlider", e.getMessage());
        }
    }

    /**
     * onClick handler for the report button.
     * Checks if a parking structure is selected and passes the parking structure and availability
     * information to the update function.
     * @param view    Report button.
     */
    public void onReportClick(View view) {
        if (!park_chosen) {
            Toast.makeText(getApplicationContext(), "Please select a parking structure.", Toast.LENGTH_SHORT).show();
        } else {
            available_list = selected.getAvailability();
            update(selected);
        }
    }

    /**
     * Updates a parking structure on the Parse database and finishes the activity.
     * @param park    A valid parking structure passed in through the startActivity intent.
     */
    public void update(ParkingStructure park) {
        try {
            Log.d("Update", park.getName());
            int index = 0;

            ListIterator<View> walker = viewList.listIterator();
            View view;
            SeekBar seekBar;

            while (walker.hasNext()) {
                view = walker.next();
                seekBar = (SeekBar) view.findViewById(R.id.report_seekbar_availability);
                Button button = (Button) view.findViewById(R.id.report_button_parkingTypeText);

                switch (button.getText().toString()) {
                    case "V/VP":
                        index = 0;
                        break;
                    case "A":
                        index = 1;
                        break;
                    case "B":
                        index = 2;
                        break;
                    case "S":
                        index = 3;
                        break;
                    default:
                        Log.d("report_update", "Invalid item ");
                        setResult(0x100);
                        finish();
                        break;
                }

                available_list.set(index, seekBar.getProgress() * 10);
            }

            park.setAvailability(available_list);
            Log.d("Update", park.getAvailability().toString());

            ParseObject parkingStructure = ParseQuery.getQuery("ParkingStructure").get(park.getObjectID());
            parkingStructure.put("AvailabilityArray", park.getAvailability());
            parkingStructure.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.d("Report", "Failed to save parking structure data");
                        Log.d("Report", e.getMessage());
                    } else {
                        setResult(getResources().getInteger(R.integer.RESULT_SHOWMAP_REPORT_SUCCESS));
                    }
                    finish();
                }
            });
        } catch (Exception e) {
            Log.d("Update", "Failed to save parking structure data");
            Log.d("Update", e.getMessage());
        }
    }
}
