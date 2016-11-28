package com.example.eiko.alphafitness;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordWorkoutLandscapeFragment extends Fragment {


    private LineChart chart;
    private TextView aveSpeedTV;
    private TextView minSpeedTV;
    private TextView maxSpeedTV;

    private String aveSpeed = "0:00";
    private String minSpeed = "0:00";
    private String maxSpeed = "0:00";

    ArrayList<Entry> stepCounts;
    ArrayList<Entry> calories;

    public RecordWorkoutLandscapeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("stepCountData", stepCounts);
        outState.putParcelableArrayList("caloriesData", calories);

      //  System.out.println("saving min:" + minSpeed);
        outState.putString("minSpeed", minSpeed);
        outState.putString("aveSpeed", aveSpeed);
        outState.putString("maxSpeed", maxSpeed);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        calories = new ArrayList<>();
        stepCounts = new ArrayList<>();

        IntentFilter stepDataIntentFilter = new IntentFilter(Constants.GRAPH_DATASET_CHANGED_BROADCAST_INTENT_LISTENER);
        getActivity().getApplicationContext().registerReceiver(stepsChangeReceiver, stepDataIntentFilter);

        //request service to send a graph data.
        requestGraphData();
    }

    /**
     * Send a b/c request to receive the latest graph data.
     */
    private void requestGraphData()
    {
        Intent intent = new Intent();
        intent.setAction(Constants.REQUEST_STATISTICS);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_record_workout_landscape, container, false);
        chart = (LineChart) view.findViewById(R.id.chart);

        aveSpeedTV = (TextView) view.findViewById(R.id.aveSpeed);
        minSpeedTV = (TextView) view.findViewById(R.id.minSpeed);
        maxSpeedTV = (TextView) view.findViewById(R.id.maxSpeed);


        if(savedInstanceState != null)
        {
            stepCounts = savedInstanceState.getParcelableArrayList("stepCountData");
            calories = savedInstanceState.getParcelableArrayList("caloriesData");
            System.out.println("ave:" + savedInstanceState.getString("aveSpeed"));
            aveSpeedTV.setText(savedInstanceState.getString("aveSpeed"));
            minSpeedTV.setText(savedInstanceState.getString("minSpeed"));
            maxSpeedTV.setText(savedInstanceState.getString("maxSpeed"));
        }

        LineDataSet dataSet = new LineDataSet(stepCounts, "Step Count");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircles(true);
        dataSet.setDrawValues(true);
        dataSet.setColor(Color.rgb(Integer.parseInt("32", 16),Integer.parseInt("8c", 16) ,Integer.parseInt("c1", 16)));


        LineDataSet dataSet2 = new LineDataSet(calories, "Calories Burned");
        dataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2.setDrawCircles(true);
        dataSet2.setDrawValues(true);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        dataSets.add(dataSet2);

        LineData data = new LineData(dataSets);

        chart.setData(data);
        chart.setDescription("Workout Session");
        chart.invalidate();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
      //  System.out.println("onResume:aveSpeed=" + aveSpeed );
     //   minSpeedTV.setText(minSpeed);
      //  maxSpeedTV.setText(maxSpeed);
       // aveSpeedTV.setText(aveSpeed);

        //request service to send a graph data.
        requestGraphData();
    }

    /**
     * Broadcast receiver for step count changes.
     */
    private BroadcastReceiver stepsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            System.out.println("minSpeed=" + (intent.getStringExtra(Constants.MIN_SPEED)));
            System.out.println("maxSpeed=" + (intent.getStringExtra(Constants.MAX_SPEED)));
            System.out.println("aveSpeed=" + (intent.getStringExtra(Constants.AVE_SPEED)));

            minSpeed = intent.getStringExtra(Constants.MIN_SPEED);
            maxSpeed = intent.getStringExtra(Constants.MAX_SPEED);
            aveSpeed = intent.getStringExtra(Constants.AVE_SPEED);

            minSpeedTV.setText(minSpeed);
            maxSpeedTV.setText(maxSpeed);
            aveSpeedTV.setText(aveSpeed);

            ArrayList<Entry> stepData = intent.getParcelableArrayListExtra(Constants.STEP_GRAPH_DATA);
            ArrayList<Entry> caloriesData = intent.getParcelableArrayListExtra(Constants.CALORIES_GRAPH_DATA);
            if(stepData != null && !stepData.isEmpty()) {
                stepCounts.clear();
                stepCounts.addAll(stepData);

                LineDataSet dataSet = new LineDataSet(stepCounts, "Step Count");
                dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setDrawCircles(true);
                dataSet.setDrawValues(true);
                dataSet.setColor(Color.rgb(Integer.parseInt("32", 16),Integer.parseInt("8c", 16) ,Integer.parseInt("c1", 16)));

                calories.clear();
                calories.addAll(caloriesData);

                LineDataSet dataSet2 = new LineDataSet(calories, "Calories Burned");
                dataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);
                dataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet2.setDrawCircles(true);
                dataSet2.setDrawValues(true);

                List<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(dataSet);
                dataSets.add(dataSet2);

                LineData data = new LineData(dataSets);

                chart.setData(data);
                chart.setDescription("Workout Session");
                chart.invalidate();

            }

            chart.invalidate();
        }
    };

}

