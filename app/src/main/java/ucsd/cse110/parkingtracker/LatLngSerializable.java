package ucsd.cse110.parkingtracker;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Holds a LatLng by latitude and longitude values and can create a LatLng.
 */
public class LatLngSerializable implements Serializable {
    public double latitude, longitude;

    public LatLngSerializable(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
