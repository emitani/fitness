package com.example.eiko.alphafitness;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.zip.Inflater;

public class ProfileActivity extends Activity implements View.OnFocusChangeListener {

    /**
     * For proile settings.
     */
    private TextView nameTV, genderTV, weightTV;

    /**
     * Weekly Average
     */
    private TextView wTimeTV, wDistTV, wCountTV, wCaloriesTV;
    private String strWeekDist, strWeekTime, strWeekCount, strWeekCal;

    /**
     * All time average
     */
    private TextView allTimeTV, allDistTV, allCountTV, allCaloriesTV;
    private String strAllDist, strAllTime, strAllCount, strAllCal;



    /**
     * onCreate method.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameTV = (TextView) findViewById(R.id.nameTV);
        nameTV.setText(readConfig(Constants.PROFILE_NAME, "anonymous"));

        genderTV = (TextView) findViewById(R.id.genderTV);
        genderTV.setText(readConfig(Constants.PROFILE_GENDER, "----"));

        weightTV = (TextView) findViewById(R.id.weightTV);
        weightTV.setText(readConfig(Constants.PROFILE_WEIGHT, "--- lbs"));

        nameTV.setOnClickListener(nameClickListener);
        genderTV.setOnClickListener(genderClickedListner);
        weightTV.setOnClickListener(weightClickedListener);

        // weekly statistics
        wTimeTV = (TextView) findViewById(R.id.weekTime);
        wDistTV = (TextView) findViewById(R.id.weekDistance);
        wCountTV = (TextView) findViewById(R.id.weekCounts);
        wCaloriesTV = (TextView) findViewById(R.id.weekCalories);

        // all-time statistics
        allCaloriesTV = (TextView) findViewById(R.id.allCalories);
        allCountTV = (TextView) findViewById(R.id.allWorkout);
        allDistTV = (TextView) findViewById(R.id.allDistance);
        allTimeTV = (TextView) findViewById(R.id.allTime);

        new Thread(statisticsReadThread).start();
    }

    /**
     * Handler to update the statistics info,
     *  which is retrieved in the background thread.
     */
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            wCaloriesTV.setText(strWeekCal);
            wCountTV.setText(strWeekCount);
            wDistTV.setText(strWeekDist);
            wTimeTV.setText(strWeekTime);

            allCaloriesTV.setText(strAllCal);
            allCountTV.setText(strAllCount);
            allDistTV.setText(strAllDist);
            allTimeTV.setText(strAllTime);
        }
    };

    Runnable statisticsReadThread = new Runnable()
    {
        @Override
        public void run() {
            long now = System.currentTimeMillis() / 1000;
            long aWeek =  60 * 60 * 24 * 7; // in seconds
            double distance;
            long time;
            int count;
            double calories;
            Cursor c;

            //weekly statistics
            c = getContentResolver().query(WorkoutContentProvider.URI,
                    new String[] {
                            "SUM(" + WorkoutContentProvider.DURATION + ") AS total_duration",
                            "SUM(" + WorkoutContentProvider.DISTANCE + ") AS total_distance",
                            "SUM(" + WorkoutContentProvider.CALORIES + ") AS total_calories",
                            "COUNT(*) AS total_count"
                        },
                    WorkoutContentProvider.START_TIME + " > ?",
                    new String[] { String.valueOf(now - aWeek) },
                    null);

         //   System.out.println("*****now=" + now + ", aweek=" + aWeek);
         //   System.out.println("condition=" + (aWeek - now));
            c.moveToFirst();
            time = c.getLong(0);
            distance = c.getDouble(1);
            calories = c.getDouble(2);
            count = c.getInt(3);


            double aveDistance = (count == 0 ? 0 : distance / count);
            double aveCalories = (count == 0 ? 0 : calories / count);
            long aveTime = (count == 0 ? 0 : time / count);

            strWeekDist = String.format("%,.2f", aveDistance) + " km";
            strWeekCal = String.format("%,.2f",aveCalories) + " cal";
            strWeekCount = count + (count == 1 ? " time" : " times");
            strWeekTime = formatTime(aveTime);

            c.close();

            // all time statistics
            c = getContentResolver().query(WorkoutContentProvider.URI,
                    new String[] {
                            "SUM(" + WorkoutContentProvider.DURATION + ") AS total_duration",
                            "SUM(" + WorkoutContentProvider.DISTANCE + ") AS total_distance",
                            "SUM(" + WorkoutContentProvider.CALORIES + ") AS total_calories",
                            "COUNT(*) AS total_count"
                    },
                    null,
                    null,
                    null);

            c.moveToFirst();
            time = c.getLong(0);
            distance = c.getDouble(1);
            calories = c.getDouble(2);
            count = c.getInt(3);

            c.close();
            double aveAllDist = (count == 0 ? 0 : distance / count);
            double aveAllCal = (count == 0 ? 0 : calories / count);
            long aveAllTime = (count == 0 ? 0 : time /count);

            strAllDist = String.format("%,.2f", aveAllDist) + " km";
            strAllCal = String.format("%,.2f", aveAllCal) + " cal";
            strAllCount = count + (count == 1 ? " time" : " times");
            strAllTime = formatTime(aveAllTime);

            handler.sendEmptyMessage(0);
        }
    };

    private String formatTime(long seconds)
    {
        int hour = (int) (seconds/ 60 / 60);
        int minute = (int) (seconds / 60 % 60);
        int sec = (int) seconds % 60;

        String formatted = hour + " hr " + minute + " min " + sec + " sec";
        return formatted;
    }


    /**
     * Save Profile configuration to shared preferences.
     * @param key
     * @param value
     */
    private void saveConfig(String key, String value)
    {
        SharedPreferences preferences = getSharedPreferences(Constants.PROFILE_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }



    /**
     * Read value from Profile configuration in Shared preference.
     * @param key key of the configuration.
     * @param defValue default value, in case the value does not exist.
     * @return value for the key, or default value if not present.
     */
    private String readConfig(String key, String defValue)
    {
        SharedPreferences preferences = getSharedPreferences(Constants.PROFILE_PREFERENCE_NAME, MODE_PRIVATE);
        return preferences.getString(key, defValue);
    }

    /**
     * OnClickListener invoked when name field is clicked.
     */
    private View.OnClickListener nameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.name_popup, null);
            Button btn = (Button) v.findViewById(R.id.button);
            final EditText et = (EditText) v.findViewById(R.id.editText);

            final PopupWindow popupWindow = new PopupWindow(v, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = et.getText().toString();
                    if(name.trim().length() > 0) {
                        saveConfig(Constants.PROFILE_NAME, name);
                        nameTV.setText(name);
                    }
                    popupWindow.dismiss();
                }
            });

            popupWindow.setElevation(5.0f);
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        }
    };


    /**
     * OnClick Listener invoked when gender field is clicked.
     */
    private View.OnClickListener genderClickedListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.gender_popup, null);
            Button btn = (Button) v.findViewById(R.id.button);
            final Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
            final PopupWindow popupWindow = new PopupWindow(v, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String gender = spinner.getSelectedItem().toString();
                    saveConfig(Constants.PROFILE_GENDER, gender);
                    genderTV.setText(gender);
                    popupWindow.dismiss();
                }
            });

            popupWindow.setElevation(5.0f);
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        }
    };

    /**
     * OnClickListener invoked when weight field is clicked
     */
    private View.OnClickListener weightClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.weight_popup, null);
            Button btn = (Button) v.findViewById(R.id.button);
            final EditText et = (EditText) v.findViewById(R.id.editText);

            final PopupWindow popupWindow = new PopupWindow(v, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String weight = et.getText().toString();
                    if(weight.trim().length() > 0) {
                        saveConfig(Constants.PROFILE_WEIGHT, weight + " lbs");
                        weightTV.setText(weight + " lbs");
                    }
                    popupWindow.dismiss();
                }
            });

            popupWindow.setElevation(5.0f);
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        }
    };

    @Override
    public void onFocusChange(View view, boolean b) {
       // System.out.println("Focus changed! view=" + view + ", b=" + b);
    }
}
