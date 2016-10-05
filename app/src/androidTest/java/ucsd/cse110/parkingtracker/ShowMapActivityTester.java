package ucsd.cse110.parkingtracker;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

public class ShowMapActivityTester extends ActivityInstrumentationTestCase2<ShowMapActivity> {

    private ShowMapActivity mShowMapActivity;

    public ShowMapActivityTester() {
        super(ShowMapActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(true);

        mShowMapActivity = getActivity();
        ParseUser.logIn("schoang@ucsd.edu", "password");
        mShowMapActivity.setCurrentUser(ParseUser.getCurrentUser());
        //wait for map to initialize
        try {
            synchronized (this) {
                wait(3000);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void tearDown() throws Exception {
        ParseUser.logOut();
        super.tearDown();
    }

    /* Scenario Test #1:
     * Given: I'm logged in as a user and the map, buttons and the parking structures are all showing
     * When: I scrolls the map away form ucsd
     * Then: The map should return to ucsd
     */
    public void test_Map_ScrollAway() {
        //- - GIVEN - -
        given_LoginAndPreconditionsSuccessed();
        //- - WHEN - -
        when_scrollsMappAway();
        //- - THEN- -
        then_returnFromScrollAwary();
    }

    /* Given: I'm logged in as a user and the map, buttons and the parking structures are all showing
     * preconditions: map is showing, report button is showing, settings button is showing, parking structures have been loaded
     * parking structures are visible on map, user is logged in as schoang@ucsd.edu
     */
    public void given_LoginAndPreconditionsSuccessed() {
        assertNotNull(mShowMapActivity);
        final GoogleMap map = mShowMapActivity.getmMap();
        final Button reportButton = (Button) mShowMapActivity.findViewById(R.id.showMap_button_report);
        final Button settingsButton = (Button) mShowMapActivity.findViewById(R.id.showMap_button_settings);
        final ArrayList<ParkingStructure> structures = mShowMapActivity.getParkingStructures();
        final ArrayList<Marker> markers = mShowMapActivity.getMarkers();
        final ParseUser user = mShowMapActivity.getCurrentUser();

        assertNotNull(map);
        assertTrue(reportButton.isShown());
        assertTrue(settingsButton.isShown());
        assertEquals(5, structures.size());
        assertEquals(5, markers.size());

        mShowMapActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Pair<ParkingStructure, Polygon>> polygons = mShowMapActivity.getPolygons();
                assertEquals(5, polygons.size());
                for (Pair<ParkingStructure, Polygon> ps : polygons) {
                    assertTrue(ps.second.isVisible());
                }
            }
        });

        assertEquals("schoang@ucsd.edu", user.getUsername());
    }

    //When: I scrolls the map away form ucsd
    public void when_scrollsMappAway() {
        // Set initial press times
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        // Create instrumentation
        Instrumentation instrumentation = getInstrumentation();

        float xStart = 200;
        float yStart = 200;

        float x0 = 300;
        float y0 = 300;

        try {
            // Action down to start gesture
            MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, xStart, yStart, 0);
            instrumentation.sendPointerSync(event);

            // Action move to swipe
            // Set event time
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x0, y0, 0);
            instrumentation.sendPointerSync(event);
            // Hold position
            eventTime = SystemClock.uptimeMillis() + 1000;
            // Release
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x0, y0, 0);
            instrumentation.sendPointerSync(event);
        } catch (Exception e) {
            Log.d("test_moving", e.getMessage());
        }
    }

    //Then: The map should return to ucsd
    public void then_returnFromScrollAwary() {
        try {
            // Wait for map to move back.
            synchronized (this) {
                wait(3000);
            }
            mShowMapActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final LatLngBounds mapVisibleRegion = mShowMapActivity.getmMap().getProjection().getVisibleRegion().latLngBounds;
                    final LatLngBounds ucsdBoundaries = mShowMapActivity.getBoundsUCSD();
                    assertTrue(mapVisibleRegion.contains(ucsdBoundaries.getCenter()));
                }
            });
        } catch (Exception e) {
            Log.d("test_map_scrollaway", e.getMessage());
            assertTrue(false);
        }
    }
    /**************************************END SCENARIO TEST #1*********************************************************************************/

    /* Scenario Test #2:
     * Given: I'm logged in as a user and the map, buttons and the parking structures are all showing
     * When: I zooms out pass maximum zoom
     * Then: The map should return to initial state
     */
    public void test_Map_ZoomOut() {
        //- - GIVEN - -
        given_LoginAndPreconditionsSuccessed();
        //- - WHEN - -
        final float zoomlevel = when_MaxZoom();
        //- - THEN - -
        then_returnInitialState(zoomlevel);
    }

    //Given function is implemented above
    //When: I zooms out pass maximum zoom
    public float when_MaxZoom() {
        // Set initial press times
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        // Create instrumentation
        Instrumentation instrumentation = getInstrumentation();

        float xStart = 200;
        float yStart = 700;
        float y0 = 705;
        float y1 = 635;
        float y2 = 585;
        float y3 = 440;
        final float zoomLevel = mShowMapActivity.getZoomMin();

        try {
            // Action down to start gesture
            MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, xStart, yStart, 0);
            instrumentation.sendPointerSync(event);
            // Release
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, xStart, yStart, 0);
            instrumentation.sendPointerSync(event);

            downTime = SystemClock.uptimeMillis() + 100;
            eventTime = SystemClock.uptimeMillis() + 100;
            // Action down to start gesture
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, xStart, yStart, 0);
            instrumentation.sendPointerSync(event);
            // Wait
            eventTime = SystemClock.uptimeMillis() + 200;
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y0, 0);
            instrumentation.sendPointerSync(event);
            eventTime = SystemClock.uptimeMillis() + 100;
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y1, 0);
            instrumentation.sendPointerSync(event);
            eventTime = SystemClock.uptimeMillis() + 100;
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y2, 0);
            instrumentation.sendPointerSync(event);
            eventTime = SystemClock.uptimeMillis() + 100;
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y3, 0);
            instrumentation.sendPointerSync(event);
            // Wait
            eventTime = SystemClock.uptimeMillis() + 2000;
            // Release
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, xStart, y3, 0);
            instrumentation.sendPointerSync(event);
        } catch (Exception e) {
            Log.d("test_moving", e.getMessage());
        }
        return zoomLevel;
    }

    //Then: The map should return to initial state
    public void then_returnInitialState(float zoomLevel) {
        final float zoomlevel = zoomLevel;
        try {
            // Wait for map to move back.
            synchronized (this) {
                wait(3000);
            }
            mShowMapActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final LatLngBounds mapVisibleRegion = mShowMapActivity.getmMap().getProjection().getVisibleRegion().latLngBounds;
                    final LatLngBounds ucsdBoundaries = mShowMapActivity.getBoundsUCSD();
                    assertTrue(mapVisibleRegion.contains(ucsdBoundaries.getCenter()));
                    assertEquals(zoomlevel, mShowMapActivity.getmMap().getCameraPosition().zoom);
                }
            });
        } catch (Exception e) {
            Log.d("test_map_scrollaway", e.getMessage());
            assertTrue(false);
        }
    }
    /**************************************END SCENARIO TEST #2*********************************************************************************/

    /* Scenario Test #3:
     * Given: I'm logged in as a user and the map, buttons and the parking structures are all showing
     * When: I tap on a parking sturucture
     * Then: the app should start parkinginfo activity with parking structure extra
     */
    public void test_ParkingInfo_TapOnStructure() {
        //- - GIVEN - -
        given_LoginAndPreconditionsSuccessed();
        //- - WHEN - -
        Marker marker = when_clickStructure();
        //- - THEN - -
        then_startParkinginfoActivity(marker);
    }

    //When: I tap on a parking sturucture
    public Marker when_clickStructure() {
        // get a marker from the map
        final Marker marker = mShowMapActivity.getMarkers().get(0);
        return marker;
    }

    //Then: the app should start parkinginfo activity with parking structure extra
    public void then_startParkinginfoActivity(Marker marker) {
        final Instrumentation.ActivityMonitor infoMonitor = getInstrumentation().addMonitor(ParkingInfo.class.getName(), null, false);
        // Forcefully startActivity
        // @// FIXME: 11/30/2015 Click on marker instead of calling the method
        final Marker myMarker = marker;
        mShowMapActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingStructure");
                query.whereEqualTo("Name", myMarker.getTitle());
                ParkingStructure ps = null;
                try {
                    ps = new ParkingStructure(query.getFirst());
                } catch (Exception e) {
                    Log.d("tapOnStruct", e.getMessage());
                }

                assertNotNull(ps);
                mShowMapActivity.showParking(ps);
                ParkingInfo info = (ParkingInfo) getInstrumentation().waitForMonitorWithTimeout(infoMonitor, 3000);
                assertNotNull(info);

                final ParkingStructure infoStructure = info.getParkingStructure();
                assertEquals(ps.getName(), infoStructure.getName());
                assertTrue(ps.getAvailability().equals(infoStructure.getAvailability()));
            }
        });
    }
    /**************************************END SCENARIO TEST #3*********************************************************************************/


    // scenario: user taps on the map but not on a structure - do not start activity
    public void test_ParkingInfo_TapAwayFromStructure() {

    }

    // scenario: user checks a parking structure and returns to map, another user updates structure, user checks structure again
    public void test_ParkingInfo_WasUpdatedByOther() {

    }

    // scenario: user taps report button while gps is off - warning appears to turn on GPS
    public void test_Report_TapNoGPS() {

    }

    // scenario: user taps report button while gps is on but is not near a parking structure - warning appears: not near structure
    public void test_Report_TapGPS_NoStructureNear() {

    }

    // scenario: user taps report button while gps is on and in a parking structure - start report activity with list of nearby structures extra
    public void test_Report_TapGPS_StructureNear() {

    }

    // scenario: user taps report button and reports availability to the nearest structure - that structure is updated
    public void test_Report_UpdateStructure() {

    }

    // scenario: user backs out of report activity without reporting - no change to structures
    public void test_Report_TapBack() {

    }

    // scenario: two users report in sequence - the latest report overwrites the earlier report
    public  void test_Report_MultipleReports() {

    }

    /* Scenario Test #4:
     * Given: I'm logged in as a user and the map, buttons and the parking structures are all showing
     * When: I clicks settings button
     * Then: the app should start settings activity
     */
    public void test_Settings_OnTap() {
        //- - GIVEN - -
        given_LoginAndPreconditionsSuccessed();
        //- - WHEN - -
        when_clickSetting();
        //- - THEN - -
        then_startSettingActivity();
    }

    //When: I clicks settings button
    public void when_clickSetting() {
        mShowMapActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShowMapActivity.findViewById(R.id.showMap_button_settings).performClick();
            }
        });
    }

    //Then: the app should start settings activity
    public void then_startSettingActivity() {
        Instrumentation.ActivityMonitor settingsActivityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);
        final Settings settingsActivity = (Settings) getInstrumentation().waitForMonitorWithTimeout(settingsActivityMonitor, 3000);
        assertNotNull(settingsActivity);
    }
    /**************************************END SCENARIO TEST #4*********************************************************************************/


    /* Scenario Test #5:
     * Given: I'm on the settings activity after I clicked the settings button
     * When: I change user's preference and views a parking structure
     * Then: the parking sturcture tailored to fit user's preference
     */
    public void test_Settings_ChangeParkingType() {
        //- - GIVEN - -
        given_clickButtonAndonSettingPage();
        //- - WHEN - -
        String parkingtype = when_setUserPreference();
        //- - THEN - -
        then_parkingStructureTailored(parkingtype);
    }

    //Given: I'm on the settings activity after I clicked the settings button
    public void given_clickButtonAndonSettingPage(){
        Instrumentation.ActivityMonitor settingsActivityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);
        mShowMapActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShowMapActivity.findViewById(R.id.showMap_button_settings).performClick();
            }
        });
        final Settings settingsActivity = (Settings) getInstrumentation().waitForMonitorWithTimeout(settingsActivityMonitor, 3000);
        assertNotNull(settingsActivity);
    }

    //When: I change user's preference and views a parking structure
    public String when_setUserPreference () {
        Instrumentation.ActivityMonitor settingsActivityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);
        final Settings settingsActivity = (Settings) getInstrumentation().waitForMonitorWithTimeout(settingsActivityMonitor, 3000);

        ParseUser user = mShowMapActivity.getCurrentUser();
        final String previousParkingType = user.getString("parkingType");
        user.put("parkingType", "S");
        try {
            user.save();
            user = user.fetch();
        } catch (Exception e) {
            Log.d("test_changeParkingType", e.getMessage());
            assertTrue(false);
        }

        final RadioButton selected = (RadioButton) settingsActivity.findViewById(((RadioGroup) settingsActivity.findViewById(R.id.radiogroup)).getCheckedRadioButtonId());
        assertEquals(user.getString("parkingType"), selected.getText().toString());
        settingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RadioGroup) settingsActivity.findViewById(R.id.radiogroup)).check(R.id.radioButton4);
                settingsActivity.findViewById(R.id.settings_button_setParkingType).performClick();
            }
        });
        try {
            synchronized (this) {
                wait(1000);
            }
        } catch (Exception e) {
            Log.d("test_changeParkingType", e.getMessage());
        }
        assertTrue(settingsActivity.isFinishing());
        return previousParkingType;
    }


    //Then: the parking sturcture tailored to fit user's preference
    public void then_parkingStructureTailored (String previousParkingType) {
        ParseUser user = mShowMapActivity.getCurrentUser();
        try {
            user = user.fetch();
            assertEquals("V/VP", user.getString("parkingType"));
            user.put("parkingType", previousParkingType);
            user.save();
            user = user.fetch();
            assertEquals(previousParkingType, user.getString("parkingType"));
        } catch (Exception e) {
            Log.d("test_changeParkingType", e.getMessage());
            assertTrue(false);
        }
    }
    /**************************************END SCENARIO TEST #5*********************************************************************************/


    /* Scenario Test #6:
     * Given: I'm on the settings activity after I clicked the settings button
     * When: I change password with a bad password
     * Then: the settings activity should stay open
     */
    public void test_Settings_ChangePasswordInvalid() {
        //- - GIVEN - -
        given_clickButtonAndonSettingPage();
        //- - WHEN - -
        when_changeBadPassword();
        //- - THEN - -
        then_activityStayOpen();
    }

    //When: I change password with a bad password
    public void when_changeBadPassword() {
        Instrumentation.ActivityMonitor settingsActivityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);
        final Settings settingsActivity = (Settings) getInstrumentation().waitForMonitorWithTimeout(settingsActivityMonitor, 3000);

        settingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!(settingsActivity.findViewById(R.id.etChangePassword)).requestFocus()) {
                    assertTrue(false);
                }
            }
        });
        try {
            synchronized (this) {
                wait(500);
            }
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
        }
        assertTrue(settingsActivity.findViewById(R.id.etChangePassword).isFocused());
        getInstrumentation().sendStringSync("password1");
        assertEquals("password1", ((EditText) settingsActivity.findViewById(R.id.etChangePassword)).getText().toString());
        settingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!(settingsActivity.findViewById(R.id.etReenterPassword)).requestFocus()) {
                    assertTrue(false);
                }
            }
        });
        try {
            synchronized (this) {
                wait(500);
            }
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
        }
        assertTrue(settingsActivity.findViewById(R.id.etReenterPassword).isFocused());
        getInstrumentation().sendStringSync("password2");
        assertEquals("password2", ((EditText) settingsActivity.findViewById(R.id.etReenterPassword)).getText().toString());
        settingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                settingsActivity.findViewById(R.id.settings_button_changePassword).performClick();
            }
        });
        try {
            synchronized (this) {
                wait(1000);
            }
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
        }
        assertFalse(settingsActivity.isFinishing());
        settingsActivity.finish();

    }

    //Then: the settings activity should stay open
    public void then_activityStayOpen() {
        final ParseUser user = mShowMapActivity.getCurrentUser();
        try {
            ParseUser changedPassword = ParseUser.logIn(user.getUsername(), "password2");
            assertTrue(false);
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
        }
    }
    /**************************************END SCENARIO TEST #6*********************************************************************************/


    /* Scenario Test #7:
     * Given: I'm on the settings activity after I clicked the settings button
     * When: I change password with a vaild password
     * Then: the settings activity should close and the user successfully changes password
     */
    public void test_Settings_ChangePasswordValid() {
        //- - GIVEN - -
        given_clickButtonAndonSettingPage();
        //- - WHEN - -
        when_changeValidPassword();
        //- - THEN - -
        then_actvityCloseAndchangePassword();
    }

    //When: I change password with a vaild password
    public void when_changeValidPassword() {
        Instrumentation.ActivityMonitor settingsActivityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);
        final Settings settingsActivity = (Settings) getInstrumentation().waitForMonitorWithTimeout(settingsActivityMonitor, 3000);

        settingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!(settingsActivity.findViewById(R.id.etChangePassword)).requestFocus()) {
                    assertTrue(false);
                }
            }
        });
        try {
            synchronized (this) {
                wait(500);
            }
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
        }
        assertTrue(settingsActivity.findViewById(R.id.etChangePassword).isFocused());
        getInstrumentation().sendStringSync("password2");
        assertEquals("password2", ((EditText) settingsActivity.findViewById(R.id.etChangePassword)).getText().toString());
        settingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!(settingsActivity.findViewById(R.id.etReenterPassword)).requestFocus()) {
                    assertTrue(false);
                }
            }
        });
        try {
            synchronized (this) {
                wait(500);
            }
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
        }
        assertTrue(settingsActivity.findViewById(R.id.etReenterPassword).isFocused());
        getInstrumentation().sendStringSync("password2");
        assertEquals("password2", ((EditText) settingsActivity.findViewById(R.id.etReenterPassword)).getText().toString());
        settingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                settingsActivity.findViewById(R.id.settings_button_changePassword).performClick();
            }
        });
        try {
            synchronized (this) {
                wait(1000);
            }
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
        }
        assertTrue(settingsActivity.isFinishing());
    }

    //Then: the settings activity should close and the user successfully changes password
    public void then_actvityCloseAndchangePassword() {
        final ParseUser user = mShowMapActivity.getCurrentUser();
        try {
            ParseUser changedPassword = ParseUser.logIn(user.getUsername(), "password2");
            assertNotNull(changedPassword);
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
            assertTrue(false);
        }
        user.setPassword("password");
        try {
            user.save();
        } catch (Exception e) {
            Log.d("test_ChangePassword", e.getMessage());
            assertTrue(false);
        }

    }
    /**************************************END SCENARIO TEST #7*********************************************************************************/


    /* Scenario Test #8:
     * Given: I'm on the settings activity after I clicked the settings button
     * When: I click logout button
     * Then: the map activity should close and current user should be null
     */
    public void test_Settings_LogOut() {
        //- - GIVEN - -
        given_LoginAndPreconditionsSuccessed();
        //- - WHEN - -
        when_clickLogout();
        //- - THEN - -
        then_mapClose();
    }

    //When: I click logout button
    public void when_clickLogout() {
        Instrumentation.ActivityMonitor settingsActivityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);
        final Settings settingsActivity = (Settings) getInstrumentation().waitForMonitorWithTimeout(settingsActivityMonitor, 3000);

        settingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                settingsActivity.findViewById(R.id.settings_button_logout).performClick();
                AlertDialog dialog = settingsActivity.getLastDialog();
                assertNotNull(dialog);
                if (dialog.isShowing()) {
                    try {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    } catch (Throwable e) {
                        Log.d("test_Logout", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
        try {
            synchronized (this) {
                wait(1000);
            }
        } catch (Exception e) {
            Log.d("test_Logout", e.getMessage());
        }
        assertTrue(settingsActivity.isFinishing());
    }

    //Then: the map activity should close and current user should be null
    public void then_mapClose() {
        assertTrue(mShowMapActivity.isFinishing());
        final ParseUser user = ParseUser.getCurrentUser();
        assertNull(user);
    }
    /**************************************END SCENARIO TEST #8*********************************************************************************/

}
