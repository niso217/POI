package com.benezra.nir.poi.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by nir on 08/10/2017.
 */

public class DateUtil {

    public static String FormatDate(int year, int month, int day) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        Date dateRepresentation = cal.getTime();

        return CalendartoDate(dateRepresentation);
    }

    public static String CalendartoDate(Date date) {
        String format = "EEE, MMM d, yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        String formattedNow = simpleDateFormat.format(date);
        return formattedNow;
    }

    public static String CalendartoTime(Date date) {
        String format = "hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        String formattedNow = simpleDateFormat.format(date);
        return formattedNow;
    }
}
