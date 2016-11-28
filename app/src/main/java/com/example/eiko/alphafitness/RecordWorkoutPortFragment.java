package com.example.eiko.alphafitness;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


/**
 * A simple {@link Fragment} subclass.
 *
 *
 **/
public class RecordWorkoutPortFragment extends Fragment implements OnMapReadyCallback
{

    RemoteServiceInterface remoteService;
    private Button actionBtn;
    private Chronometer chronometer;

    private String startActionStr;
    private String stopActionStr;

    private boolean ticking; //if the action has started.

    private TextView distanceTV;
    private MapView mapView;
    private GoogleMap googleMap;

    /**
     * Color of your workout route on map.
     */
    private int lineColor = Color.rgb(Integer.parseInt("FF", 16),
            Integer.parseInt("40", 16),
            Integer.parseInt("81", 16));



    /**
     * current latitude and altitude.
     */
    private double currentLat, currentLng;

    /**
     * PolygonOptions for drawing the route.
     */
    private PolylineOptions lineOption;

    /**
     * Current polyline on the map.
     */
    private Polyline currentPolyline;

    public RecordWorkoutPortFragment() {
        // Required empty public constructor
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putLong("baseTime", chronometer.getBase());
        outState.putBoolean("ticking", ticking);
        outState.putString("distance", distanceTV.getText().toString());
        outState.putString("currentTime", chronometer.getText().toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        IntentFilter locationIntentFilger = new IntentFilter(Constants.LOCATION_CHANGED_BROADCAST_INTENT_FILTER);
        getActivity().getApplicationContext().registerReceiver(locationChangeReceiver, locationIntentFilger);

        IntentFilter stepsIntentFilter = new IntentFilter(Constants.STEPCOUNT_CHANGED_BROADCAST_INTENT_FILTER);
        getActivity().getApplicationContext().registerReceiver(stepsChangeReceiver, stepsIntentFilter);

        Intent intent = new Intent(getActivity(), RemoteService.class);
        intent.setAction(RemoteServiceInterface.class.getName());
        getActivity().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }



    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unregisterReceiver(locationChangeReceiver);
        getActivity().getApplicationContext().unregisterReceiver(stepsChangeReceiver);
        getActivity().getApplicationContext().unbindService(mConnection);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_workout_port, container, false);

        ImageView profileImage = (ImageView) view.findViewById(R.id.profileImage);
        profileImage.setOnClickListener(profileClickListner);



        startActionStr = getResources().getString(R.string.alphaStartWorkout);
        stopActionStr = getResources().getString(R.string.alphaStopWorkout);

        chronometer = (Chronometer) view.findViewById(R.id.chronometer);

        distanceTV = (TextView) view.findViewById(R.id.distanceTV);
        actionBtn = (Button) view.findViewById(R.id.actionBtn);
        actionBtn.setOnClickListener(actionButtonListener);
        actionBtn.setText(startActionStr);


        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        if(savedInstanceState != null) {
            ticking = savedInstanceState.getBoolean("ticking");
            distanceTV.setText(savedInstanceState.getString("distance"));

            if(ticking) {
                chronometer.setBase(savedInstanceState.getLong("baseTime"));
                chronometer.start();
                actionBtn.setText(stopActionStr);
            } else {


                chronometer.stop();
            }

        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();

    }

    private View.OnClickListener actionButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!ticking) {
                try {
                    remoteService.startWorkout();

                    resetMap(); //clear map from existing session.
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    actionBtn.setText(stopActionStr);
                    lineOption = new PolylineOptions();

                    ticking = true;
                } catch(RemoteException e) {
                    toast("Failed to connect to recording service.");
                }
            } else {
                try {
                    remoteService.stopWorkout();
                    chronometer.stop();
                    actionBtn.setText(startActionStr);
                    ticking = false;
                } catch(RemoteException e) {
                    toast("Failed to connect to recording service");
                }
            }
        }
    };


    /**
     * OnClick listner for a profile picture.
     * It starts a ProfileActivity.
     */
    private View.OnClickListener profileClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.setMyLocationEnabled(true);

        // Permission check.
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String locationProvider = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(locationProvider);


        if(location == null) {
            Toast.makeText(getActivity(), "Unable to obtain location information.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentLat = location.getLatitude();
        currentLng = location.getLongitude();

        LatLng here = new LatLng(currentLat, currentLng);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(here));

        googleMap.setMaxZoomPreference(22f);
        googleMap.setMinZoomPreference(15f);

    }

    /**
     * Reset map.
     */
    private void resetMap()
    {
        googleMap.clear();
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLat, currentLng)));
    }

    /**
     * Update the location on the map and add new polyline.
     * @param location new location.
     */
    private void updateLocationOnMap(LatLng location)
    {
        if(googleMap == null) return;

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        if(ticking && lineOption != null) {
            lineOption.add(new LatLng(currentLat, currentLng));
            currentPolyline = googleMap.addPolyline(lineOption.color(lineColor));
        }
    }


    /**
     * BroadCast receiver for receiving location changes for map tracking.
     */
    private BroadcastReceiver locationChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double newLat = intent.getDoubleExtra(Constants.LOCATION_CHANGED_LATITUDE, currentLat);
            double newLng = intent.getDoubleExtra(Constants.LOCATION_CHANGED_LONGDITUDE, currentLng);
            if(newLat != currentLat || newLng != currentLng) {
                currentLat = newLat;
                currentLng = newLng;
                updateLocationOnMap(new LatLng(currentLat, currentLng));
            }
        }
    };

    /**
     * Broadcast receiver for receiving step counts.
     * @param message
     */
    private BroadcastReceiver stepsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double distance = intent.getDoubleExtra(Constants.TRAVEL_DISTANCE, 0);
            distanceTV.setText(String.format("%,.2f", distance));
        }
    };

    private void toast(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Inner class for accessing remote service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            remoteService = RemoteServiceInterface.Stub.asInterface(iBinder);
           // toast("Connected to a recording service");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            remoteService = null;
            //toast("Disconnected to a recording service.");
        }
    };

}
