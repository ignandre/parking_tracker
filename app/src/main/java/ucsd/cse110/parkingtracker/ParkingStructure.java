package ucsd.cse110.parkingtracker;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Data structure that holds information about a parking structure.
 */
public class ParkingStructure implements Serializable {

    /**
     * Object ID from Parse
     */
    private String mObjectID;

    // Name of the parking structure
    private String mName;

    // Array of integers storing the availability of V, A, B, S parking spots
    private List<Integer> mAvailability;

    // Last updated time
    private Date mUpdatedTime;

    // Location of parking structure on google maps
    private LatLngSerializable mLocation;
    private List<LatLngSerializable> mBoundaries;

    /**
     * Constructor for Parking Structures using a ParseObject.
     * Parking Structures can only be added manually using the Parse website interface.
     * <br>
     * Parse database access currently limited to the developer team.
     * @param parseObject    ParseObject containing a ParkingStructure
     */
    public ParkingStructure(ParseObject parseObject)
    {
        mObjectID = parseObject.getObjectId();
        mName = parseObject.getString("Name");
        mAvailability = parseObject.getList("AvailabilityArray");
        mUpdatedTime = parseObject.getUpdatedAt();
        double latitude = parseObject.getDouble("Latitude");
        double longitude = parseObject.getDouble("Longitude");
        mLocation = new LatLngSerializable(latitude, longitude);
        List<Double> vertices = parseObject.getList("Boundaries");
        mBoundaries = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i += 2) {
            mBoundaries.add(new LatLngSerializable(vertices.get(i), vertices.get(i+1)));
        }
    }

    public String getObjectID() { return mObjectID; }
    public String getName()
    {
        return mName;
    }
    public List<Integer> getAvailability()
    {
        return mAvailability;
    }
    public Date getUpdatedTime() { return mUpdatedTime; }
    public double getLatitude() { return mLocation.latitude; }
    public double getLongitude() { return mLocation.longitude; }
    public LatLng getLocation() {
        return mLocation.getLatLng();
    }
    public List<LatLngSerializable> getBoundaries() {
        return mBoundaries;
    }

    public void setName(String name) {
        mName = name;
    }
    public void setAvailability(List<Integer> availability) {
        mAvailability = availability;
    }
    public void setLatitude(double latitude) {
        mLocation.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        mLocation.longitude = longitude;
    }
    public void setLocation(LatLng location) {
        mLocation = new LatLngSerializable(location.latitude, location.longitude);
    }
    public void setLocation(LatLngSerializable location) {
        mLocation = location;
    }

}