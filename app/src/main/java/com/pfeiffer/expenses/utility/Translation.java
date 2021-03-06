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
        cal.set(Calendar.HOUR,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        return cal.getTime();
    }

}