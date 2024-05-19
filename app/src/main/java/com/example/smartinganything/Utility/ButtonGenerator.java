package com.example.smartinganything.Utility;

import android.graphics.Color;

import java.util.Random;

public class ButtonGenerator {

    public static int generateRandomColor() {
        Random random = new Random();
        // Generate random RGB values for the color
        int red = random.nextInt(156);
        int green = random.nextInt(156);
        int blue = random.nextInt(156);
        // Create the color by combining the RGB values
        return Color.rgb(red, green, blue);
    }

}
