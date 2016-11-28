package com.example.eiko.alphafitness;

/**
 * Created by eiko on 10/22/2016.
 */
public class Constants {

    /**
     * Constants for Profile settings.
     */
    protected final static String PROFILE_PREFERENCE_NAME = "com.example.eiko.PROFILE_SETTINGS";
    protected final static String PROFILE_NAME = "PROFILE_NAME";
    protected final static String PROFILE_GENDER = "PROFILE_GENDER";
    protected final static String PROFILE_WEIGHT = "PROFILE_WEIGHT";


    /**
     * Constants for Content Provider
     */
    protected final static String CONTENT_PROVIDER_URI_AUTHORITIES = "com.example.eiko.alphafitness.workoutprovider";
    protected final static String CONTENT_PROVIDER_WORKOUT_URI = "content://" + CONTENT_PROVIDER_URI_AUTHORITIES + "/workouts";

    /**
     * for location change broadcast messages
     */
    protected final static String LOCATION_CHANGED_BROADCAST_INTENT_FILTER = "com.example.eiko.alphafitness.LOCATION_CHANGED";
    protected final static String LOCATION_CHANGED_LATITUDE = "com.example.eiko.alphafitness.LOC_CHANGED_LATITUDE";
    protected final static String LOCATION_CHANGED_LONGDITUDE = "com.example.eiko.alphafitness.LOC_CHANGED_LONGDITUDE";

    /**
     * for step count change broadcast messages.
     */

    protected final static String STEPCOUNT_CHANGED_BROADCAST_INTENT_FILTER = "com.example.eiko.alphafitness.STEPCOUNT_CHANGED";
    protected final static String STEPCOUNT = "com.example.eiko.alphafitness.STEPCOUNT";
    protected final static String TRAVEL_DISTANCE = "com.example.eiko.alphafitness.TRAVEL_DISTANCE";

    /**
     * broadcast messages for updating graph dataset.
     */
    protected final static String GRAPH_DATASET_CHANGED_BROADCAST_INTENT_LISTENER = "com.example.eiko.alphafitness.GRAPH_CHANGED";
    protected final static String STEP_GRAPH_DATA = "com.example.eiko.alphafitness.STEP_GRAPH_DATA";
    protected final static String CALORIES_GRAPH_DATA = "com.example.eiko.alphafitness.CALORIES_GRAPH_DATA";
    protected final static String MIN_SPEED = "com.example.eiko.alphafitness.MIN_SPEED";
    protected final static String MAX_SPEED = "com.example.eiko.alphafitness.MAX_SPEED";
    protected final static String AVE_SPEED = "com.example.eiko.alphafitness.AVE_SPEED";

    protected final static String REQUEST_STATISTICS = "com.example.eiko.alphafitness.REQUEST_STATISTICS";

}
