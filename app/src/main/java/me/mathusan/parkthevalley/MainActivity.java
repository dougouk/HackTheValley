package me.mathusan.parkthevalley;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener {


    enum STATE {
        ADDING,
        SEARCHING,
        MODIFYING,
        NORMAL // nothing
    }

    private String TAG = MainActivity.class.getName();
    /**
     * Class members
     */

    private UiSettings mUiSettings;
    private static String CLASS_NAME = "MAIN ACTIVITY";
    private STATE state;

    private String name, email;
    private HashMap<User, String> userListHashMap;

    /**
     * Dialogue stuff
     */
    private View addParkingDialogueView;
    private ImageView parkingPreview;
    private Spot spotToAdd;

    /**
     * Map Members
     */

    private GoogleMap mMap;
    private Marker selectedMarker;
    private Circle selectedCircle;
    private Location lastKnownLocation;

    /**
     * Connection members
     */
    private GoogleApiClient mGoogleAPIClient;
    private LocationRequest locationRequest;

    private DatabaseReference databaseReference;
    final public static String FIREBASE_URL = "https://fir-parkthevalley.firebaseio.com/";

    TextView nameH=null, emailH=null;

    /**
     * User member
     */

    private User user;

//    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        state = STATE.NORMAL;
        userListHashMap = new HashMap<>();
//        button = (Button) findViewById(R.id.refreshMapButton);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                writeNewPost(null);
//            }
//        });

        mGoogleAPIClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        createLocationRequest();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            name = b.getString("name");
            email = b.getString("email");

        }

        user = new User();
        user.setName(name);
        user.setEmail(email);
        askForNumber();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View nav_view = getLayoutInflater().inflate(R.layout.nav_header_main, null);
        nameH = (TextView) nav_view.findViewById(R.id.navheader_name);
        emailH = (TextView) nav_view.findViewById(R.id.navheader_email);

        nameH.setText(user.getName() == null ? "Please sign in" : user.getName());
        emailH.setText(user.getEmail());
        
        mapFragment.getMapAsync(this);

        // Write a message to the databaseReference
        databaseReference = FirebaseDatabase.getInstance().getReference();

    }

    public Activity getInstance() {return this;}

    public HashMap<User, String> getUserListHashMap () {return userListHashMap;}

    private void askForNumber() {
        createPhoneNumberDialogue();
    }


    private void createPhoneNumberDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.dialogue_phone_number, null);
                // Add action buttons
                builder.setView(view).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText number = (EditText) view.findViewById(R.id.phoneNumberPicker);

                        if(number != null && number.getText() != null){
                            user.setPhone(number.getText().toString());
                            Toast.makeText(getApplicationContext(), "Phone number saved", Toast.LENGTH_SHORT).show();
                        }


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        user.setPhone("--- --- ----");
                    }
                });

        builder.show();
    }

    private void createAddParkingDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        addParkingDialogueView = inflater.inflate(R.layout.add_parking_dialogue, null);
        Button button = (Button) addParkingDialogueView.findViewById(R.id.dialgouePlacePicker);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlacePicker();
            }
        });

        // Add action buttons
        builder.setView(addParkingDialogueView).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                EditText number = (EditText) addParkingDialogueView.findViewById(R.id.price);

                if(number != null && number.getText() != null){
                    spotToAdd.setPrice(Float.parseFloat(number.getText().toString()));
                }


                changeCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(spotToAdd.getLat(), spotToAdd.getLng()), 14));

//                selectedMarker = mMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(spotToAdd.getLat(), spotToAdd.getLng()))
////                        .title((String) place.getAddress())
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_xs)));

//                    addMarkerToUser();

                List<Spot> tempCollection = user.getSpots() == null ? new ArrayList<Spot>() : user.getSpots();
                tempCollection.add(spotToAdd);
                user.setSpots(tempCollection);

                writeNewPost(user);

            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        user.setPhone("--- --- ----");
                    }
                });
        parkingPreview = (ImageView) addParkingDialogueView.findViewById(R.id.spotImage);
        builder.show();
    }

    private void writeNewPost(@Nullable final User user) {

        Log.d(CLASS_NAME, "WritingPost...");


        // remove existing databasereference
        {
            if(userListHashMap.containsKey(user)){
                databaseReference.child("availableSpots")
                .child(user.getName())
                .child("spots")
                .setValue(user.getSpots());

//                databaseReference.getRef().setValue(user);
//                userListHashMap.remove(user);
//
                Log.d(CLASS_NAME, "userListHashMap contains user already");
            }
             {
                // Generate a new push ID for the new post

                databaseReference.child("availableSpots")
                        .child(user.getName())
                        .setValue(user);
                userListHashMap.put(user, databaseReference.getKey());

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d(TAG, dataSnapshot.toString());

                        for (DataSnapshot list : dataSnapshot.getChildren()) {
                            Log.d(TAG, list.toString());
                            Map<String, Object> userMap = (Map<String, Object>) list.getValue();
                            Set<String> keySet = userMap.keySet();

                            Log.d(TAG, userMap.toString());
                            for (String key : keySet) {
                                Map<String, Object> childUserMap = (Map<String, Object>) userMap.get(key);
                                Set<String> childKeySet = childUserMap.keySet();

                                String phone = (String) childUserMap.get("phone");
                                String email = (String) childUserMap.get("email");
                                ArrayList<Map<String, Object>> spots = (ArrayList<Map<String, Object>>) childUserMap.get("spots");


                                mMap.clear();
                                for (Map<String, Object> spotMap : spots) {
                                    Spot s = new Spot();
//                                    Map<String, Object> latSet = (Map<String, Object> ) obj.get(0);
//                                    Map<String, Object> lngSet = (Map<String, Object> ) spots.get(1);
//                                    Map<String, Object> openSet = (Map<String, Object> ) spots.get(2);
//                                    Map<String, Object> priceSet = (Map<String, Object> ) spots.get(2);
//                                    Map<String, Object> timeSet = (Map<String, Object> ) spots.get(2);

                                    s.setLat((double) spotMap.get("lat"));
                                    s.setLng((double) spotMap.get("lng"));
                                    s.setOpen((boolean) spotMap.get("open"));

                                    // Firebase sometimes returns long for price. Not sure why
                                    if(spotMap.get("price").getClass().equals(Long.class)){
                                        s.setPrice(Double.parseDouble(spotMap.get("price")+""));
                                    }
                                    else{
                                        s.setPrice((double)
                                                spotMap.get("price"));
                                    }

                                    s.setTime((long) spotMap.get("time"));

                                    // add marker ONLY if available
                                    if(s.getOpen()){
                                        selectedMarker = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(s.getLat(), s.getLng()))
                                                .title("Price: " + String.valueOf(s.getPrice()))
    //                                            .title("Available: " + s.getOpen())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_xs)));
                                    }

                                    saveUserList();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(CLASS_NAME, "Failed to read value");
                    }
                });
            }
        }
    }

    private void saveUserList(){
        try{
            FileOutputStream fileOutputStream = openFileOutput("userList", MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            Log.d(CLASS_NAME, "size of object " + objectOutputStream.toString());
            objectOutputStream.writeObject(userListHashMap);
            objectOutputStream.flush();
            objectOutputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_maps) {

        } else if (id == R.id.nav_addlisting) {
            state = STATE.ADDING;
            createAddParkingDialogue();

        } else if (id == R.id.nav_searchspots) {
            state = STATE.SEARCHING;
            startPlacePicker();

        } else if (id == R.id.nav_signout) {
            state = STATE.NORMAL;

        }
          else if (id == R.id.nav_profile) {
            state = STATE.NORMAL;
            Intent i = new Intent(this, MyListings.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    int PLACE_PICKER_REQUEST = 1;

    private void startPlacePicker() {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(data, this);

                // Measure screen dimensions
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                final int height = displaymetrics.heightPixels;
                final int width = displaymetrics.widthPixels;


                if (state == STATE.ADDING) {

                    final String httpRequest = MapHelper.getStaticMapURL(place.getLatLng(), width, height);
                    Log.d(TAG, httpRequest);
                    //Load image onto dialogue
                    parkingPreview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            Picasso.with(getApplicationContext())
                                    .load(httpRequest)
                                    .into(parkingPreview);
                        }
                    });

                    spotToAdd = new Spot();
                    spotToAdd.setLat(place.getLatLng().latitude);
                    spotToAdd.setLng(place.getLatLng().longitude);
                    spotToAdd.setOpen(true);
                }
            }
        }
    }

//    private void addMarkerToUser() {
//        List<Marker> list = user.getMarkers() == null ? new ArrayList<Marker>() : user.getMarkers();
//        list.add(selectedMarker);
//        user.setMarkers(list);
//    }

    private void changeCamera(CameraUpdate cameraUpdate) {
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(80000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(CLASS_NAME, "onMapReady");
        mMap = googleMap;
        if (mMap != null) {
            Log.d(CLASS_NAME, "map is not null");
        }
        askForPermission();

        mUiSettings = mMap.getUiSettings();

        mUiSettings.setZoomControlsEnabled(true);


        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e(CLASS_NAME, e.getMessage());
        }

        Log.d(CLASS_NAME, "lastKnownLocation is null " + (lastKnownLocation == null));

        if(lastKnownLocation != null){
            changeCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 14));
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(CLASS_NAME, "onConnected");
        startLocationUpdates();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            askForPermission();
            return;
        }
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleAPIClient);
    }

    private void startLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, locationRequest, this);
        }catch(SecurityException e){
            Log.e(CLASS_NAME, e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleAPIClient, this);
        Log.d(CLASS_NAME, "stopLocationUpdates");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(CLASS_NAME, "onConnectionSuspended");
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(CLASS_NAME, "location Changed " + location.toString());
    }


    private void askForPermission(){
        // if (ContextCompat.checkSelfPermission(this, Manifest.permission.))
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)){

            //user has previously seen permission dialogue
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
