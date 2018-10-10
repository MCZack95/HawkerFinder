package com.example.hawkerfinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity_Maps extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final float BENCHMARK_DISTANCE = 5000;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private LatLng defaultLatLng;
    private Address defaultAddress;
    private ArrayList<Marker> nearbyMarkerLocations = null;
    private ArrayList<Address> nearbyAddressLocations = null;
    private float distanceFromCurrent = 0;
    private LatLng previousLatLng;

    DatabaseHelper myDb;
    TextView latitudeTextView, longitudeTextView, postalCodeTextView;
    EditText setLocationEditText;
    Button backButton, searchNearbyButton, refreshCurrentLocationButton, setLocationButton;
    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        myDb = new DatabaseHelper(this);
        myDialog = new Dialog(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocationPermission();

        latitudeTextView = findViewById(R.id.lat_text_view);
        longitudeTextView = findViewById(R.id.long_text_view);
        postalCodeTextView = findViewById(R.id.postal_code_text_view);
        setLocationEditText = findViewById(R.id.set_location_text);

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchNearbyButton = findViewById(R.id.search_nearby_button);
        searchNearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchNearbyStalls(v);
            }
        });

        refreshCurrentLocationButton = findViewById(R.id.refresh_current_location);
        refreshCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCurrentLocation(v);
            }
        });

        setLocationButton = findViewById(R.id.set_location_button);
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTargetLocation(v);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(nearbyMarkerLocations == null)
                    return false;

                if (nearbyMarkerLocations.size() == 0)
                    return false;

                if (marker.getTitle().matches("Current Location")) {
                    marker.showInfoWindow();
                    return false;
                } else {
                    specialMapReset();

                    // Getting URL to the Google Directions API
                    String url = getUrl(defaultLatLng, marker.getPosition());
                    Log.d("onMapClick", url);

                    // Start downloading json data from Google Directions API
                    FetchUrl fetchUrl = new FetchUrl();
                    fetchUrl.execute(url);

                    mMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle())).showInfoWindow();
                    return false;
                }
            }
        });

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                if (marker.getTitle().matches("Current Location"))
                    return;

                myDialog.setContentView(R.layout.dialog_stall_location_details);
                myDialog.setCanceledOnTouchOutside(true);

                TextView tempView = myDialog.findViewById(R.id.dialog_stall_name);
                String tempString = marker.getTitle();
                tempView.setText(tempString);

                tempView = myDialog.findViewById(R.id.dialog_stall_lat);
                tempString = "Latitude: " + String.valueOf(marker.getPosition().latitude);
                tempView.setText(tempString);

                tempView = myDialog.findViewById(R.id.dialog_stall_long);
                tempString = "Longitude: " + String.valueOf(marker.getPosition().longitude);
                tempView.setText(tempString);

                tempView = myDialog.findViewById(R.id.dialog_stall_postal_code);
                tempString = "Postal Code: " + myDb.getStallPostalCode(marker.getTitle());
                tempView.setText(tempString);

                tempView = myDialog.findViewById(R.id.dialog_stall_distance);
                tempString = "Distance: " + distanceFromCurrent / 1000 + " km";
                tempView.setText(tempString);

                tempView = myDialog.findViewById(R.id.dialog_stall_displacement);
                tempString = "Displacement: " + getDistance(marker.getPosition()) / 1000 + " km";
                tempView.setText(tempString);

                //noinspection ConstantConditions
                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i : grantResults) {
                        if (i != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(Activity_Maps.this);
                }
            }
        }
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(Activity_Maps.this);
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressWarnings("unchecked")
    private void getDeviceLocation() {
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            if(currentLocation != null){
                                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title("Current Location");
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                                String temp = "Latitude: " + String.valueOf(latLng.latitude);
                                latitudeTextView.setText(temp);
                                temp = "Longitude: " + String.valueOf(latLng.longitude);
                                longitudeTextView.setText(temp);

                                defaultLatLng = latLng;

                                setDefaultAddress(defaultLatLng);

                                temp = "Postal Code: " + defaultAddress.getPostalCode();
                                postalCodeTextView.setText(temp);

                                mMap.addMarker(markerOptions);
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                                NearbyStalls nearbyStalls = new NearbyStalls();
                                nearbyStalls.execute(defaultAddress.getPostalCode());

                            }else{
                                getDeviceLocation();
                            }
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(Activity_Maps.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    ////////////////////////////////////////////////// ON-CLICK METHODS //////////////////////////////////////////////////
    public void searchNearbyStalls(View v) {
        if(nearbyMarkerLocations == null){
            Toast.makeText(v.getContext(),"Please wait...\nNearby locations are being loaded.",Toast.LENGTH_LONG).show();
            return;
        }

        mapReset();
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Activity_Maps.this);
        mBuilder.setCancelable(true);

        if (nearbyMarkerLocations.size() == 0) {
            mBuilder.setTitle("No nearby stalls");
            mBuilder.setMessage(" ");
            mBuilder.show();
            return;
        }

        for(Marker marker : nearbyMarkerLocations){
            mMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()));
        }

        mBuilder.setTitle(nearbyMarkerLocations.size() + " Location(s) Found\n(Within 5 KM)");
        mBuilder.setMessage(" ");
        mBuilder.show();
    }

    @SuppressWarnings("unchecked")
    public void refreshCurrentLocation(View v) {
        mMap.clear();
        nearbyMarkerLocations = null;
        nearbyAddressLocations = null;
        getDeviceLocation();
    }

    public void setTargetLocation(View v) {
        nearbyAddressLocations = null;
        nearbyMarkerLocations = null;

        String m_Text = setLocationEditText.getText().toString();

        if(m_Text.isEmpty()) {
            setLocationEditText.setError("Required field is empty!");
            return;
        }

        if (m_Text.length() == 6 && isInteger(m_Text))
            m_Text = "Singapore " + setLocationEditText.getText().toString();

        if(!m_Text.contains("Singapore"))
            m_Text = "Singapore " + m_Text;

        List<Address> addressList = null;

        Geocoder geocoder = new Geocoder(Activity_Maps.this);
        try {
            addressList = geocoder.getFromLocationName(m_Text, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressList != null) {
            if (addressList.get(0).getPostalCode() == null) {
                addressList.get(0).setPostalCode("null");
            }
            try {
                defaultAddress = addressList.get(0);
                defaultLatLng = new LatLng(defaultAddress.getLatitude(), defaultAddress.getLongitude());
                mapReset();

                String temp = "Latitude: " + String.valueOf(defaultLatLng.latitude);
                latitudeTextView.setText(temp);
                temp = "Longitude: " + String.valueOf(defaultLatLng.longitude);
                longitudeTextView.setText(temp);
                temp = "Postal Code: " + defaultAddress.getPostalCode();
                postalCodeTextView.setText(temp);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, DEFAULT_ZOOM));

                NearbyStalls nearbyStalls = new NearbyStalls();

                if(defaultAddress != null) {
                    nearbyStalls.execute(defaultAddress.getPostalCode());
                }
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(Activity_Maps.this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(Activity_Maps.this, "Location not found", Toast.LENGTH_SHORT).show();
        }
    }

    ////////////////////////////////////////////////// CLASS METHODS //////////////////////////////////////////////////
    public void setDefaultAddress(LatLng latLng) {
        List<Address> addressList = null;
        Geocoder geocoder = new Geocoder(getApplicationContext());
        try {
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressList != null) {
            try {
                defaultAddress = addressList.get(0);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(getApplicationContext(), "Location not found!", Toast.LENGTH_LONG).show();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Location not found.", Toast.LENGTH_LONG).show();
        }
    }

    public ArrayList<Address> getLocationAddresses() {
        List<Address> addressList = null;
        ArrayList<Address> addressArrayList = new ArrayList<>();
        ArrayList<String> locations = myDb.getAllStallPostalCode();

        for (String temp : locations) {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(temp, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addressList != null) {
                try {
                    Address address = addressList.get(0);
                    addressArrayList.add(address);
                } catch (IndexOutOfBoundsException e) {
                    Toast.makeText(getApplicationContext(), "Location not found!", Toast.LENGTH_LONG).show();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Location not found.", Toast.LENGTH_LONG).show();
            }
        }
        return addressArrayList;
    }

    public float getDistance(LatLng latLng) {
        Location location1 = new Location("Start");
        Location location2 = new Location("End");

        location1.setLatitude(defaultLatLng.latitude);
        location1.setLongitude(defaultLatLng.longitude);

        location2.setLatitude(latLng.latitude);
        location2.setLongitude(latLng.longitude);

        return location1.distanceTo(location2);
    }

    public float getIntermediateDistance(LatLng latLng){
        Location location1 = new Location("Start");
        Location location2 = new Location("End");

        location1.setLatitude(previousLatLng.latitude);
        location1.setLongitude(previousLatLng.longitude);

        location2.setLatitude(latLng.latitude);
        location2.setLongitude(latLng.longitude);

        return location1.distanceTo(location2);
    }

    public void mapReset() {
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(defaultLatLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(markerOptions);
    }

    public void specialMapReset() {
        mMap.clear();
        previousLatLng = defaultLatLng;
        distanceFromCurrent = 0;
        for (Marker marker : nearbyMarkerLocations) {
            mMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()));
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(defaultLatLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(markerOptions);
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Api Key
        String str_apiKey = "key=AIzaSyCVsOpqZpqeb84lHNQJ4ti9do1AhmI9rcE";

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_apiKey + "&" + str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    //A method to download json data from url
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data);
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }

    ////////////////////////////////////////////////// NESTED CLASSES //////////////////////////////////////////////////
    // Fetches data from url passed
    @SuppressLint("StaticFieldLeak")
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    //A class to parse the Google Places in JSON format
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }

            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                    distanceFromCurrent += getIntermediateDistance(position);
                    previousLatLng = position;
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute LineOptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Toast.makeText(Activity_Maps.this,"Route not found. Try Again.",Toast.LENGTH_LONG).show();
                Log.d("onPostExecute","without Polyline(s) drawn");
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class NearbyStalls extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            nearbyAddressLocations = new ArrayList<>();
            ArrayList<Address> addressArrayList = getLocationAddresses();
            ArrayList<Address> filteredAddressArrayList = new ArrayList<>();

            for (Address address : addressArrayList)
                if (getDistance(new LatLng(address.getLatitude(),address.getLongitude())) <= BENCHMARK_DISTANCE)
                    filteredAddressArrayList.add(address);

            if (filteredAddressArrayList.size() == 0)
                return null;

            if(nearbyAddressLocations != null) {
                nearbyAddressLocations.addAll(filteredAddressArrayList);
                return "Success: Nearby Locations found.";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null){
                Log.d(TAG,"Fail: Nearby Locations not found.");
                return;
            }
            nearbyMarkerLocations = new ArrayList<>();
            if(nearbyMarkerLocations.size() == 0) {
                for (Address address : nearbyAddressLocations)
                    nearbyMarkerLocations.add(mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title(myDb.getStallName(address.getPostalCode()))));
                mapReset();
                Log.d(TAG, s);
            }
        }
    }

}