package com.pfeiffer.expenses;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


class Translation {
    private static final String databaseDateFormat_ = "yyyy:MM:dd";

    private static final Map<String, String> monthMapper_;

    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("01", "Januar");
        aMap.put("02", "Februar");
        aMap.put("03", "März");
        aMap.put("04", "April");
        aMap.put("05", "Mai");
        aMap.put("06", "Juni");
        aMap.put("07", "Juli");
        aMap.put("08", "August");
        aMap.put("09", "September");
        aMap.put("10", "Oktober");
        aMap.put("11", "November");
        aMap.put("12", "Dezember");

        monthMapper_ = Collections.unmodifiableMap(aMap);
    }

    static public String getMonthString(String monthNumber) {
        return monthMapper_.get(monthNumber);
    }

    static public String getDatbaseDateFormat() {
        return databaseDateFormat_;
    }

    static public String humanReadableDate(String date) {
        return date.substring(8, 10) + "." + date.substring(5, 7) + ".";
    }

    static private String adjustZeros(String arg) {
        int len = arg.length();

        // check if "." appears more than once
        int numberOfDots = len - arg.replace(".", "").length();
        // Log.d( "Translation", "Number of dots: " + String.valueOf(
        // numberOfDots ) );
        if (numberOfDots > 1)
            return null;

        String ret = arg;

        if (numberOfDots == 0)
            return ret + ".00";
        else {
            int dotPos = arg.indexOf(".");
            // Log.d( "Translation", "Dot position: " + String.valueOf( dotPos )
            // + ", length: " + len );
            if (dotPos == 0)
                ret = "0" + ret;
            if (dotPos == len - 1)
                ret += "00";
            else if (dotPos == len - 2)
                ret += "0";
        }
        return ret;
    }

    static public String getValidPrice(String arg) {
        // round long values
        double value = (double) Math.round(Double.parseDouble(arg) * 100) / 100;

        return adjustZeros(String.valueOf(value));
    }
}