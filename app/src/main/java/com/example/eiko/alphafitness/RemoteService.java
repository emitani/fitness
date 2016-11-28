package com.example.eiko.alphafitness;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RemoteService extends Service {

    protected static final double DISTANCE_PER_STEP_MALE = 0.762; // in meters
    protected static final double DISTANCE_PER_STEP_FEMALE = 0.67; // in meter

    protected static final long POST_DELAY_MILLIS = 1000 * 60 * 5; // 5 minutes.
 // protected static final long POST_DELAY_MILLIS = 1000 * 60 * 1; // 1 minutes for testing.

    protected static final int DISTANCE_IN_KILOMETER = 1000;

    private SensorManager sensorManager;
    private Sensor stepCounter;
    private LocationManager locationManager;

    /**
     * Timer for computing statistics and updating graph every 5 minutes.
     */
    private Timer timer;

    private double speedSum;
    private double aveSpeed;
    private double minSpeed;
    private double maxSpeed;
    private int checkpoints;

    private long startTime;

    private boolean isStarted;

    private String gender;
    private double weight;

    private CaloriesCalculator caloriesCalculator;
    private double distancePerStep;

    /**
     * variables for values 5 minutes ago.
     */
    private double distancePrev = 0;
    private float stepsPrev = 0;
    private long postTimePrev;

    private ArrayList<Entry> stepsPer5mins;
    private ArrayList<Entry> caloriesPer5mins;


    /**
     * current step counts for this workout session.
     */
    private float stepCount;

    /**
     * Initial step count.
     */
    private float stepCountInitial = -1;

    private final RemoteServiceInterface.Stub mBinder = new RemoteServiceInterface.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void startWorkout() throws RemoteException {
            PackageManager packageManager = getPackageManager();
            if(packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
                stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                sensorManager.registerListener(stepSentorListener, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            }


            // initializing variables and schedule a timer task.
            stepCountInitial = -1;
            stepCount = 0;
            isStarted = true;
            postTimePrev = SystemClock.elapsedRealtime();
            startTime = SystemClock.elapsedRealtime();

            aveSpeed = 0;
            minSpeed = 0;
            maxSpeed = 0;
            checkpoints = 0;

            stepsPer5mins = new ArrayList<>();
            caloriesPer5mins = new ArrayList<>();
            timer = new Timer();
            timer.scheduleAtFixedRate(new DataCollectionTask(), 0, POST_DELAY_MILLIS);

            isStarted = true;
        }

        @Override
        public void stopWorkout() throws RemoteException {
            sensorManager.unregisterListener(stepSentorListener, stepCounter);
            if(timer != null) {
                timer.cancel();
                timer = null;
            }

            saveSessionData();
            isStarted = false;
        }
    };


    public RemoteService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String locationProvier = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(locationProvier, 1000, 2, locationListener);

        SharedPreferences preferences = getSharedPreferences(Constants.PROFILE_PREFERENCE_NAME, MODE_PRIVATE);
        gender = preferences.getString(Constants.PROFILE_GENDER, "Male");
        if(gender.equalsIgnoreCase("male")) {
            distancePerStep = DISTANCE_PER_STEP_MALE;
        } else {
            distancePerStep = DISTANCE_PER_STEP_FEMALE;
        }

        String w = preferences.getString(Constants.PROFILE_WEIGHT, "160 lbs");
        weight = Integer.parseInt(w.substring(0, w.length() - " lbs".length()));
        caloriesCalculator = new CaloriesCalculator(weight);

        IntentFilter stepsIntentFilter = new IntentFilter(Constants.REQUEST_STATISTICS);
        registerReceiver(receiver, stepsIntentFilter);

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    /**
     * Update min/max/ave speed statistics.
     *
     * @param newDataPoint
     */
    private void updateSpeedStatistics(double newDataPoint)
    {
        if(newDataPoint == 0) return;

        speedSum += newDataPoint;
        aveSpeed = speedSum / ++checkpoints;

        if(minSpeed == 0) {
            minSpeed = newDataPoint;
        } else {
            minSpeed = (minSpeed > newDataPoint) ? newDataPoint : minSpeed;
        }

        if(maxSpeed == 0) {
            maxSpeed = newDataPoint;
        } else {
            maxSpeed = (maxSpeed < newDataPoint) ? newDataPoint : maxSpeed;
        }

    }


    /**
     * Computer min per Km based on speed (km/h)
     * @param speed
     * @return
     */
    private String minPerKm(double speed)
    {
       if(speed == 0) return "00:00";
        double secPerkm = 1 / speed  * 60 * 60;  //seconds per km
        int hr =  (int)(1 / speed);
        int min = (int) (secPerkm / 60);
        int sec = (int) secPerkm % 60;

        String minPerKm = (hr > 0 ? hr + ":" : "") + String.format("%02d", min % 60) + ":" + String.format("%02d", sec);
        return minPerKm;
    }


    /**
     * Compute the distance traveledbased on step counts.
     * @param steps number of steps taken.
     * @param unit whether the distance should be obtained in m or in km.
     * @return estimated travelled distance in specified unit for the steps.
     */
    private double computeDistanceFromSteps(float steps, int unit)
    {
        return steps * distancePerStep / unit;
    }

    /**
     * Sensor listener for step counter.
     */
    private SensorEventListener stepSentorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float steps = sensorEvent.values[0];
            if(stepCountInitial < 0) {
                stepCountInitial = steps;
            }

            stepCount = steps - stepCountInitial;
            //System.out.println("stepCount=" + stepCount);
            Intent intent = new Intent();
            intent.setAction(Constants.STEPCOUNT_CHANGED_BROADCAST_INTENT_FILTER);
            intent.putExtra(Constants.STEPCOUNT, stepCount);
            intent.putExtra(Constants.TRAVEL_DISTANCE, computeDistanceFromSteps(stepCount, DISTANCE_IN_KILOMETER));
            sendBroadcast(intent);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //do nothing.
        }
    };


    /**
     * Location listener  - used to update map location and draw a route.
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Intent intent = new Intent();
            intent.setAction(Constants.LOCATION_CHANGED_BROADCAST_INTENT_FILTER);
            intent.putExtra(Constants.LOCATION_CHANGED_LATITUDE, location.getLatitude());
            intent.putExtra(Constants.LOCATION_CHANGED_LONGDITUDE, location.getLongitude());
            sendBroadcast(intent);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            //do nothing.
        }

        @Override
        public void onProviderEnabled(String s) {
            //do nothing.
        }

        @Override
        public void onProviderDisabled(String s) {
            //do nothing.
        }
    };


    /**
     * Save session info to database
     */
    private void saveSessionData()
    {
        float steps = stepCount;
        double distanceKM = computeDistanceFromSteps(steps, DISTANCE_IN_KILOMETER);
        long durationInSeconds = (SystemClock.elapsedRealtime() - startTime) / 1000;
        double calories = caloriesCalculator.getCaloriesForSteps(steps);
        //System.out.println("duration=" + durationInSeconds);
        //System.out.println("steps=" + steps);
        //System.out.println("distanceKM=" + distanceKM);
        //System.out.println("calories=" + calories);

        long timestamp = System.currentTimeMillis() / 1000;
        //System.out.println("timestamp=" + timestamp);
        ContentValues values = new ContentValues();
        values.put(WorkoutContentProvider.DURATION, durationInSeconds);
        values.put(WorkoutContentProvider.STEPS, steps);
        values.put(WorkoutContentProvider.DISTANCE, distanceKM);
        values.put(WorkoutContentProvider.CALORIES, calories);
        values.put(WorkoutContentProvider.START_TIME, timestamp);
        getContentResolver().insert(WorkoutContentProvider.URI, values);

    }

    /**
     * Background thread that does computation for the graph every 5 minutes.
     */

    private class DataCollectionTask extends TimerTask
    {
        @Override
        public void run() {
  //          System.out.println("Posting:" + new Date());

            long now = SystemClock.elapsedRealtime();
            long thisPeriod = now - postTimePrev;
            long elapsedTime = now - startTime;
            long elapsedTimeSeconds = elapsedTime / 1000;
            postTimePrev = now;

            float stepsThisPeriod = stepCount - stepsPrev;

            stepsPrev += stepsThisPeriod;



            if(elapsedTimeSeconds > 0) {
                double distanceThisPeriod = computeDistanceFromSteps(stepsThisPeriod, DISTANCE_IN_KILOMETER);
                long thisPeroidInSeconds = thisPeriod /1000;

                double speed = distanceThisPeriod / thisPeroidInSeconds * (60 * 60); //speed in km/h
                updateSpeedStatistics(speed);


                stepsPer5mins.add(new Entry(elapsedTimeSeconds/60, stepsThisPeriod));
                caloriesPer5mins.add(new Entry(elapsedTimeSeconds/60, caloriesCalculator.getCaloriesForSteps(stepsThisPeriod)));

                broadcastStatistics();

            }
        }

    };

    /**
     * Broadcast current statistics data.
     */
    private void broadcastStatistics()
    {
        Intent intent = new Intent();
        intent.setAction(Constants.GRAPH_DATASET_CHANGED_BROADCAST_INTENT_LISTENER);
        intent.putParcelableArrayListExtra(Constants.STEP_GRAPH_DATA, stepsPer5mins);
        intent.putParcelableArrayListExtra(Constants.CALORIES_GRAPH_DATA, caloriesPer5mins);

        intent.putExtra(Constants.AVE_SPEED, minPerKm(aveSpeed));
        intent.putExtra(Constants.MIN_SPEED, minPerKm(minSpeed));
        intent.putExtra(Constants.MAX_SPEED, minPerKm(maxSpeed));

        sendBroadcast(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    /**
     * Requested to broadcast the current statistic data.
     */
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastStatistics();
        }
    };
}
