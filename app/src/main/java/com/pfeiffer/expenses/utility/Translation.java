package com.pfeiffer.expenses.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Translation {
    static String[] months_ = {"Januar", "Februar",
            "März", "April", "May", "Juni", "Juli",
            "August", "September", "Oktober", "November",
            "Dezember"};

    static public String getMonthString(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return months_[cal.get(Calendar.MONTH)];

    }

    static public String shortDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.");
        return dateFormat.format(date);
    }

    static public boolean sameMonthAndYear(Date a, Date b){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        return dateFormat.format(a).equals(dateFormat.format(b));
    }

    static public String yearAndMonth(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        return dateFormat.format(date);
    }

    static public Date getFirstDateOfCurrentMonth() {
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
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

    static public String getValidPrice(double arg) {
        return getValidPrice(String.valueOf(arg));
    }

    static public String getValidPrice(String arg) {
        // round long values
        double value = (double) Math.round(Double.parseDouble(arg) * 100) / 100;

        return adjustZeros(String.valueOf(value));
    }
//
//    static public String getPercentage(double arg){
//
//    }

}