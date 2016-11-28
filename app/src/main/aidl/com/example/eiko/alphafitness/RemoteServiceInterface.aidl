// RemoteServiceInterface.aidl
package com.example.eiko.alphafitness;

// Declare any non-default types here with import statements

interface RemoteServiceInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void startWorkout();

    void stopWorkout();

    /**
    * get step count statistics data for graph.
    */
  //  ArrayList<Parcelable> getStepData ();

}
