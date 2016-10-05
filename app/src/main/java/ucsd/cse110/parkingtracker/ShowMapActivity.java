package ucsd.cse110.parkingtracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity which displays a map generated using the Google Maps API.
 */
public class ShowMapActivity extends FragmentActivity implements OnMapReadyCallback, Serializable,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = ShowMapActivity.class.getSimpleName();
    private LocationRequest mLocationRequest;
    protected Location mLocation;

    private ParseUser currentUser;

    final private LatLng mSouthwest = new LatLng(32.86938702, -117.24506378);
    final private LatLng mNortheast = new LatLng(32.89212851, -117.21279144);
    private LatLngBounds mBoundsUCSD;
    private float zoomMin;

    private ArrayList<ParkingStructure> mParkingStructures;
    private ArrayList<Pair<ParkingStructure, Polygon>> mPolygons;
    private ArrayList<Marker> mMarkers;

    private static final int REQUEST_FINE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        try {
            Parse.initialize(this,
                    getString(R.string.parse_applicationID),
                    getString(R.string.parse_clientKey));
        } catch (Exception e) {
            Log.d("showMapOnCreate", e.getMessage());
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        // Create client to interpret GPS information
        buildGoogleApiClient();
        createLocationRequest();

        mapFragment.getMapAsync(this);
        currentUser = ParseUser.getCurrentUser();
        loadStructures();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Disable center map on location button
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMarkers = new ArrayList<>();
        mPolygons = new ArrayList<>();
        loadStructures();

        // On map loaded, move the camera to within UCSD boundaries and get furthest zoom level.
        // Also populate map with polygons for each parking structure.
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mBoundsUCSD = new LatLngBounds(mSouthwest, mNortheast);
                // Add a marker to the center of UCSD and move the camera
                //mMap.addMarker(new MarkerOptions().position(mLocUCSD).title("UCSD"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mBoundsUCSD, 0));
                zoomMin = mMap.getCameraPosition().zoom;

                for (ParkingStructure p : mParkingStructures) {
                    PolygonOptions polygonOptions = new PolygonOptions();
                    for (LatLngSerializable pos : p.getBoundaries()) {
                        polygonOptions.add(new LatLng(pos.latitude, pos.longitude));
                    }
                    Polygon poly = mMap.addPolygon(polygonOptions);
                    poly.setStrokeWidth(4);
                    addStructureName(p);
                    mPolygons.add(new Pair<>(p, poly));
                }
            }
        });

        loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION);

        // Check GPS permissions and enable Google Maps location services
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            //Toast.makeText(this, "GPS Permission was Granted", Toast.LENGTH_SHORT).show();
        } else {
            mMap.setMyLocationEnabled(false);
            //Toast.makeText(this, "GPS Permission was NOT Granted", Toast.LENGTH_SHORT).show();
        }

        // Listeners for handling polygon clicks.
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for (Pair<ParkingStructure, Polygon> curr : mPolygons) {
                    if (PolyUtil.containsLocation(latLng, curr.second.getPoints(), true)) {
                        showParking(curr.first);
                    }
                }
            }
        });

        // Listeners for handling marker clicks.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (ParkingStructure curr : mParkingStructures) {
                    if (marker.getTitle().equals(curr.getName())) {
                        showParking(curr);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Add a marker corresponding to a structure on the map.
     * @param parkingStructure    Valid parking structure with a name and location
     */
    public void addStructureName(ParkingStructure parkingStructure) {
        String name = parkingStructure.getName();
        LatLng pos = new LatLng(parkingStructure.getLatitude(), parkingStructure.getLongitude());

        // Create a TextView with the structure name
        final TextView textView = new TextView(this);
        textView.setText(name);
        textView.setTextSize(14);

        // Get the 2d rendering info about the TextView
        final Paint paintText = textView.getPaint();

        // Create a rectangle to encompass the text
        final Rect boundsText = new Rect();
        paintText.getTextBounds(name, 0, textView.length(), boundsText);
        paintText.setTextAlign(Paint.Align.CENTER);

        // Create a bitmap configuration with 32bit colors
        final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        // Create a blank bitmap with dimensions of the rectangle plus padding
        final Bitmap bmpText = Bitmap.createBitmap(boundsText.width() + 6, boundsText.height() + 6, conf);

        // Create an image with the size of the bitmap
        final Canvas canvasText = new Canvas(bmpText);

        // Draw the background WHITE
        // Draw the text BLACK
        canvasText.drawColor(Color.WHITE);
        paintText.setColor(Color.BLACK);
        canvasText.drawText(name, canvasText.getWidth() / 2,
                canvasText.getHeight() - 3, paintText);

        // Create a marker with the custom icon
        final MarkerOptions markerOptions = new MarkerOptions()
                .position(pos)
                .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                .anchor(0.5f, 1)
                .title(name);

        // Add marker to map
        mMarkers.add(mMap.addMarker(markerOptions));
    }

    /**
     * Display ParkingInfo activity to show information about a parking structure.
     * Fetches the latest information about a parking structure before passing it to the
     *      new activity in the intent.
     * @param parkingStructure    A valid parking structure.
     */
    public void showParking(ParkingStructure parkingStructure) {
        try {
            Intent intent = new Intent(this, ParkingInfo.class);
            ParseQuery<ParseObject> query = new ParseQuery<>("ParkingStructure");
            ParkingStructure p = new ParkingStructure(query.get(parkingStructure.getObjectID()));
            intent.putExtra("Parking_Structure_Object", p);
            startActivity(intent);
        } catch (Exception e) {
            Log.d("ShowMap.reportParking", e.getMessage());
        }
    }

    /**
     * onClick handler for the report button.
     * Checks if the GPS function is on and if GPS permissions are allowed. If both, then
     *      attempts to generates a list of structures the user is located in with reportParkingWithLocation.
     *      If there are no structures, then the user is notified. Otherwise, reportParkingWithLocation
     *      will handle the new activity.
     * @param view    The Report button
     */
    public void reportParking(View view) {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                if (!reportParkingWithLocation(pos)) {
                    Toast.makeText(this, "No nearby parking structures", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Location permissions not enabled.", Toast.LENGTH_LONG).show();
                Log.d("reportParking", e.toString());
            }
        } else {
            Toast.makeText(this, "Please enable GPS tracking.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks if there are any parking structures nearby the given location.
     * If there is at least one structure nearby, the list of structures will be passed to
     *      a new instance of the Report activity.
     * @param pos    A position on Earth.
     * @return  True if at least one parking structure is found, false otherwise.
     */
    private boolean reportParkingWithLocation(LatLng pos) {
        ArrayList<ParkingStructure> nearby = getParkingStructuresAt(pos);
        if (!nearby.isEmpty()) {
            Intent intent = new Intent(this, Report.class);
            intent.putExtra("Parking_Structures", nearby);
            startActivityForResult(intent, getResources().getInteger(R.integer.REQUEST_SHOWMAP_REPORT));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get all parking structures which contain a position latLng.
     * @param latLng    A position on Earth
     * @return  An ArrayList of ParkingStructure objects which contain the position latLng
     */
    public ArrayList<ParkingStructure> getParkingStructuresAt(LatLng latLng) {
        ArrayList<ParkingStructure> result = new ArrayList<>();

        for (Pair<ParkingStructure, Polygon> curr : mPolygons) {
            if (PolyUtil.containsLocation(latLng, curr.second.getPoints(), true)) {
                try {
                    ParseQuery<ParseObject> query = new ParseQuery<>("ParkingStructure");
                    ParkingStructure p = new ParkingStructure(query.get(curr.first.getObjectID()));
                    result.add(p);
                } catch (Exception e) {
                    Log.d("getStructures", e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * onClick for the Settings button.
     * @param view    The Settings button
     */
    public void openSettings(View view) {
        Intent intent = new Intent(this, Settings.class);
        startActivityForResult(intent, getResources().getInteger(R.integer.REQUEST_SETTINGS));
    }

    /**
     * Activity result handler for settings and report.
     * @param requestCode    Request code passed to startActivityForResult
     * @param resultCode     Result code from activity set with setResult()
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Resources res = getResources();
        Log.d("Result", Integer.toString(requestCode) + " " + Integer.toString(resultCode));
        if (requestCode == res.getInteger(R.integer.REQUEST_SHOWMAP_REPORT)) {
            if (resultCode == res.getInteger(R.integer.RESULT_SHOWMAP_REPORT_SUCCESS)) {
                Toast.makeText(getApplicationContext(), "Thank you for reporting!", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == res.getInteger(R.integer.REQUEST_SETTINGS)) {
            if (resultCode == res.getInteger(R.integer.RESULT_SETTINGS_LOGOUT)) {
                Log.d("LogoutResult", "Logout?");
                logout();
            }
        }
        loadStructures();
    }

    //get all query
    private Boolean loadStructures() {
        if (mParkingStructures == null) {
            mParkingStructures = new ArrayList<>();
        } else {
            mParkingStructures.clear();
        }

        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingStructure");
            List<ParseObject> structures = query.find();

            for (ParseObject structure : structures) {
                mParkingStructures.add(new ParkingStructure(structure));
            }
            Log.d("LoadStructs", mParkingStructures.toString());
        } catch (Exception e) {
            Log.d("ShowMap.loadStructures", e.getMessage());
            return false;
        }
        return true;
    }

    public void logout() {
        ParseUser.logOut();
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d("Back Pressed", "Pressed button");
        new AlertDialog.Builder(this)
                .setMessage("Logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ShowMapActivity.this.logout();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean b = super.dispatchTouchEvent(event);
        Log.d("Touch event", "Processing");
        Log.d("Touch event", event.toString());
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d("Touch event", "Process up action");
            if (mMap != null) {
                CameraPosition camera = mMap.getCameraPosition();

                if (camera.zoom < zoomMin) { // camera zoomed too far out
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBoundsUCSD, 0));
                } else {
                    // get current map boundaries
                    LatLngBounds mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    double dLeft = 0.0, dRight = 0.0, dUp = 0.0, dDown = 0.0;

                    // check if mapBounds southwest corner is more west of UCSD southwest corner
                    if (mapBounds.southwest.latitude < mSouthwest.latitude) {
                        //move camera by difference to the right
                        dRight = mSouthwest.latitude - mapBounds.southwest.latitude;
                    }
                    if (mapBounds.southwest.longitude < mSouthwest.longitude) {
                        //move camera by difference up
                        dUp = mSouthwest.longitude - mapBounds.southwest.longitude;
                    }
                    if (mapBounds.northeast.latitude > mNortheast.latitude) {
                        //move camera by difference to the left
                        dLeft = mapBounds.northeast.latitude - mNortheast.latitude;
                    }
                    if (mapBounds.northeast.longitude > mNortheast.longitude) {
                        //move camera by difference down
                        dDown = mapBounds.northeast.longitude - mNortheast.longitude;
                    }

                    LatLng newPos = new LatLng(camera.target.latitude - dLeft + dRight,
                            camera.target.longitude + dUp - dDown);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(newPos));
                }
            }
        }
        return b;
    }


    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Location services resumed.");
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "Location services paused.");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10 * 1000);    // 10 seconds, in milliseconds
        mLocationRequest.setFastestInterval(1000); // 1 second, in milliseconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    private void loadPermissions(String perm,int requestCode) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, new String[]{perm}, requestCode);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (permissions.length == 1
                        && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    //Toast.makeText(this, "GPS Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    mMap.setMyLocationEnabled(false);
                    //Toast.makeText(this, "GPS Permission NOT Granted", Toast.LENGTH_SHORT).show();
                }
            }

        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "Location services connected.");
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        } else {
            //Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                //connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                Log.i(TAG, "Connection Failed.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    public GoogleMap getmMap() {
        return mMap;
    }
    public float getZoomMin() {
        return zoomMin;
    }
    public ParseUser getCurrentUser() {
        return currentUser;
    }
    public ArrayList<ParkingStructure> getParkingStructures() {
        return mParkingStructures;
    }
    public ArrayList<Pair<ParkingStructure, Polygon>> getPolygons() {
        return mPolygons;
    }
    public LatLngBounds getBoundsUCSD() {
        return mBoundsUCSD;
    }
    public ArrayList<Marker> getMarkers() {
        return mMarkers;
    }


    public void setCurrentUser(ParseUser currentUser) {
        this.currentUser = currentUser;
    }

}
