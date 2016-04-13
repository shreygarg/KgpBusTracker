package com.example.shreygarg.bustracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Getting Location Updates.
 *
 * Demonstrates how to use the Fused Location Provider API to get updates about a device's
 * location. The Fused Location Provider is part of the Google Play services location APIs.
 *
 * For a simpler example that shows the use of Google Play services to fetch the last known location
 * of a device, see
 * https://github.com/googlesamples/android-play-location/tree/master/BasicLocation.
 *
 * This sample uses Google Play services, but it does not require authentication. For a sample that
 * uses Google Play services for authentication, see
 * https://github.com/googlesamples/android-google-accounts/tree/master/QuickStart.
 */
public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "location-updates";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;


    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected GoogleApiClient mGoogleApiClient;

    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView errcheck;
    public LatLng[] hall = new LatLng[10];
    protected LatLng rk = new LatLng(22.321095, 87.306992);
    protected LatLng ms = new LatLng(22.321084, 87.304735);
    protected LatLng llr = new LatLng(22.321243, 87.303113);
    protected LatLng lbs = new LatLng(22.320735, 87.300466);
    protected LatLng pan = new LatLng(22.318937, 87.300572);
    protected LatLng azad = new LatLng(22.318331, 87.299332);
    protected LatLng mt = new LatLng(22.317080, 87.306551);
    protected LatLng gate = new LatLng(22.317393, 87.309784);
    protected LatLng vshila = new LatLng(22.317601, 87.311426);
    protected LatLng nalanda = new LatLng(22.315614, 87.315983);
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected ArrayList<Double> distancetohall = new ArrayList<Double>();
    //protected String mLastUpdateTimeLabel;

    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;
    private int results;
    private GoogleMap mymap;
    public ArrayList<Marker> mMarker = new ArrayList<Marker>();
    Firebase myFirebaseRef;
    public int noofbuses;
    public HashMap<String, String> hm;//= new HashMap();
    private boolean isinit;
    public LatLng mygpos;
//    public int flag = 0;
    public LatLng bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getphoneperm();
        hall[0] = rk;
        hall[1] = ms;
        hall[2] = llr;
        hall[3] = lbs;
        hall[4] = pan;
        hall[5] = azad;
        hall[6] = mt;
        hall[7] = gate;
        hall[8] = vshila;
        hall[9] = nalanda;
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        errcheck = (TextView) findViewById(R.id.err);
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        isinit = true;
        hm = new HashMap<String, String>();
        hm.put("Shrey", "865072026684632");
        hm.put("Nitesh", "357215069705690");
        noofbuses = hm.size();
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://burning-inferno-1809.firebaseio.com/");
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getphoneperm() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        results);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return;
        }
    }

    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }


    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        results);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }


    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    public double dist;

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }
    private void gettime(LatLng bus,int minindex)
    {
        String url = getDirectionsUrl(bus, hall[minindex]);

        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private void updateUI() {

        mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel,
                mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel,
                mCurrentLocation.getLongitude()));

        final LatLng mypos = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        distancetohall.removeAll(distancetohall);
//        flag=1;
        for (int i = 0; i < 10; i++) {
            distancetohall.add(CalculationByDistance(mypos, hall[i]));
        }
//        errcheck.setText(Double.toString(Collections.min(distancetohall)));
//        errcheck.setText(Integer.toString(distancetohall.size()));
//        final int minindex=0;
//        for()

//        flag=0;

        // Start downloading json data from Google Directions API
        final int minindex = distancetohall.indexOf(Collections.min(distancetohall));
        mygpos = mypos;
        if (isinit) {
            mMarker.add(mymap.addMarker(new MarkerOptions().position(mypos).title("Click Start to start")));
            mMarker.add(mymap.addMarker(new MarkerOptions().position(mypos).title("Click Start to start")));
            mMarker.add(mymap.addMarker(new MarkerOptions().position(mypos).title("Me").snippet("I am here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));

            mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(mypos, 16));
            isinit = false;
        }
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String unqid = mngr.getDeviceId();
        boolean isbus = false;
        for (Map.Entry m : hm.entrySet()) {
            if (m.getValue().toString().contains(unqid) || unqid.contains(m.getValue().toString())) {
                isbus = true;
                break;
            }
        }
        if (isbus) {
            myFirebaseRef.child("message").child(unqid).setValue(mypos, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        System.out.println("Data could not be saved. " + firebaseError.getMessage());
                    } else {
                        System.out.println("Data saved successfully.");
                    }
                }
            });
        }
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                errcheck.setText("");
                System.out.println("There are " + snapshot.getChildrenCount() + " blog posts");
                DataSnapshot postSnapshot = snapshot.child("message");
                int i = 0;
                String name = "test";
                double dist=100000;
                for (DataSnapshot finalSnapshot : postSnapshot.getChildren()) {
                    String imei = finalSnapshot.getKey().toString();
                    for (Map.Entry m : hm.entrySet()) {
//                        errcheck.append(imei+"\n"+m.getValue().toString()+"\n");
                        if (m.getValue().toString().contains(imei) || imei.contains(m.getValue().toString())) {
                            name = m.getKey().toString();
//                           errcheck.append(name+"\n");
                        }
                    }
                    Double currlat = Double.parseDouble(finalSnapshot.child("latitude").getValue().toString());
                    Double currlong = Double.parseDouble(finalSnapshot.child("longitude").getValue().toString());
                    LatLng tmp = new LatLng(currlat, currlong);
                    double temp=CalculationByDistance(mypos,tmp);
                    if(dist>temp)
                    {
                     dist=temp;
                        bus=tmp;
                    }
//                    flag=2;



//                    float[] results = new float[1];
//                    Location.distanceBetween(mypos.latitude, mypos.longitude, ms.latitude, ms.longitude, results);
//                    if (dist < results[0])
//                        dist = results[0];

                    //writecodeforbusdirection


//                    mymap.clear();
//                    errcheck.append(Integer.toString(i)+"\n");
                    if (mMarker.get(i) != null) {
                        mMarker.get(i).remove();
                        mMarker.set(i, mymap.addMarker(new MarkerOptions().position(tmp).title(name).snippet(imei)));
                    } else {
                        mMarker.set(i, mymap.addMarker(new MarkerOptions().position(tmp).title(name).snippet(imei)));
//                        mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(tmp, 16));
                    }
                    i++;
                }

                gettime(bus,minindex);

//                errcheck.setText(Double.toString(dist));

                if (mMarker.get(i) != null) {
                    mMarker.get(i).remove();
                    mMarker.set(i, mymap.addMarker(new MarkerOptions().position(mygpos).title("Me").snippet("I am here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));

                } else {
                    mMarker.set(i, mymap.addMarker(new MarkerOptions().position(mygpos).title("Me").snippet("I am here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        errcheck.setText("lol");
        updateUI();
//        Toast.makeText(this, "s"),
//                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mymap = googleMap;
        mymap.addMarker(new MarkerOptions()
                        .position(hall[0])
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop))
                        .title("RK Hall")
        );
        mymap.addMarker(new MarkerOptions()
                .position(hall[1])
                .title("MS Hall")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
        mymap.addMarker(new MarkerOptions()
                .position(hall[2])
                .title("LLR Hall")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
        mymap.addMarker(new MarkerOptions()
                .position(hall[3])
                .title("LBS Hall")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
        mymap.addMarker(new MarkerOptions()
                .position(hall[4])
                .title("PAN Loop")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
        mymap.addMarker(new MarkerOptions()
                .position(hall[5])
                .title("Azad Hall")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
        mymap.addMarker(new MarkerOptions()
                .position(hall[6])
                .title("MT Hall")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
        mymap.addMarker(new MarkerOptions()
                .position(hall[7])
                .title("Gate No.5")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
        mymap.addMarker(new MarkerOptions()
                .position(hall[8])
                .title("Vikramshila")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
        mymap.addMarker(new MarkerOptions()
                .position(hall[9])
                .title("Nalanda")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));

    }

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

            br.close();

        } catch (Exception e) {
            Log.d("Exception  ", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";


            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }


            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);

            }

            errcheck.setText("Distance:" + distance + ", Duration:" + duration);

            // Drawing polyline in the Google Map for the i-th route
            mymap.addPolyline(lineOptions);
        }
    }
}