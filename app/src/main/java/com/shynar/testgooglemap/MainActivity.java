package com.shynar.testgooglemap;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static final String TAG = "MainActivity";
    GoogleMap mGoogleMap;
    EditText et;
    List<Address> list = null;
    Geocoder gc = null;
    private static int MY_PERMISSIONS_REQUEST = 1;
    LocationManager locationManager;
    Location mCurrentLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private static String[] permissions = {"android.Manifest.permission.ACCESS_FINE_LOCATION",
            "android.Manifest.permission.ACCESS_COARSE_LOCATION"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServicesAvaible()) {
            //Toast.makeText(this,"Perfect!!!", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_main);
            et = (EditText) findViewById(R.id.editText);
            initMap();
        } else {

        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("My location");
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
         Log.d(TAG, "Location update started ..............: ");
     }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (et.getText().toString().equals(""))

        {   String text = et.getText().toString();
            switch (item.getItemId()) {
                case (R.id.shareId):

                   // Toast.makeText(this, "intentWA", Toast.LENGTH_LONG).show();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                    //sendIntent.setPackage("com.whatsapp");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.shareTtl)));
                    //startActivity(sendIntent);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    public boolean googleServicesAvaible(){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        } else if (api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(this, isAvailable,0);
            dialog.show();
        } else {
            Toast.makeText(this,"Cant connect to play services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

     @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        Double x, y;
        float z;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           int per = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            if ( per != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST);
                mGoogleMap.setMyLocationEnabled(true);

            }else
            {
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
     }

public void defineAdreess(Location lc) {
    Geocoder gc = new Geocoder(MainActivity.this);
    try {
        List<android.location.Address> list = gc.getFromLocation(lc.getLatitude(), lc.getLongitude(), 1);
        android.location.Address adr = list.get(0);
        et = (EditText) findViewById(R.id.editText);
        et.setText(adr.getAddressLine(0));
    }catch(Exception e){

    }
}
            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            }

    private void GoToLocationZoom(Double x, Double y, Float z) {
        LatLng ll = new LatLng(x,y);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, z);
        mGoogleMap.moveCamera(update);

    }

    public void geoLocate(View view) {

        et = (EditText) findViewById(R.id.editText);
        String strAddress = et.getText().toString();
        Geocoder gc = new Geocoder(this);

        try {

            list = gc.getFromLocationName(strAddress,1);
            android.location.Address adr = list.get(0);
            String locality = adr.getLocality();
            Toast.makeText(this, locality,Toast.LENGTH_LONG).show();

            double lat = adr.getLatitude();
            double lng = adr.getLongitude();
            GoToLocationZoom(lat, lng, (float) 15);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     @Override
     public void onConnected(@Nullable Bundle bundle) {
         Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
         startLocationUpdates();
     }

     @Override
     public void onConnectionSuspended(int i) {

     }

     @Override
     public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

     }

     @Override
     public void onLocationChanged(Location location) {
         Log.d(TAG, "Firing onLocationChanged..............................................");
         mCurrentLocation = location;
         defineAdreess(mCurrentLocation);

     }
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }
 }
