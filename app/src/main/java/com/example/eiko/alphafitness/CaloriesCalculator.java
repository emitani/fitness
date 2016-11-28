package com.example.eiko.alphafitness;

/**
 * Helper class to calculate burned calories.
 * Created by eiko on 11/2/2016.
 */
public class CaloriesCalculator {


    private double weight;

    public CaloriesCalculator(double weight) {
        this.weight = weight;
    }

    float getCaloriesForSteps(float steps)
    {
        int baseCalories = getBaseCalories();
        return (steps / 1000) * baseCalories;
    }

    /**
     * get "base calories" or calories burned per 1000 steps based on a weight group.
     * @return
     */
    private int getBaseCalories()
    {
        int baseCalories = 0;

        if(weight <= 100) {
            baseCalories = 28;
        }
        else if (weight <= 120) {
            baseCalories = 33;
        }
        else if (weight <= 140) {
            baseCalories = 38;
        }
        else if(weight <= 160) {
            baseCalories = 44;
        }
        else if (weight <= 180) {
            baseCalories = 49;
        }
        else if (weight <= 200) {
            baseCalories = 55;
        }
        else if (weight < 220) {
            baseCalories = 60;
        }
        else if (weight < 250) {
            baseCalories = 69;
        }
        else if (weight <= 275) {
            baseCalories = 75;
        }
        else {
            baseCalories = 82;
        }
        return baseCalories;
    }
}
