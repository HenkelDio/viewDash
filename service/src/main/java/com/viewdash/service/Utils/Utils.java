package com.viewdash.service.Utils;

public class Utils {

    public static String getScore(String answer) {
        int score = Integer.parseInt(answer);
        if (score <= 6) {
            return "DETRACTOR";
        } else if (score <= 8) {
            return "NEUTRAL";
        }

        return "PROMOTER";
    }

}
