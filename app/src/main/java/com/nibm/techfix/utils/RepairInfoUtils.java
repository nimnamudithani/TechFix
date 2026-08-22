package com.nibm.techfix.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Small customer-service helpers for estimates, completion time and warranty. */
public final class RepairInfoUtils {
    private RepairInfoUtils() { }

    public static String estimatedTime(String serviceName) {
        String s = serviceName == null ? "" : serviceName.toLowerCase(Locale.ROOT);
        if (s.contains("screen")) return "2–4 hours";
        if (s.contains("battery")) return "1–2 hours";
        if (s.contains("keyboard")) return "2–4 hours";
        if (s.contains("motherboard")) return "1–3 working days";
        if (s.contains("power supply")) return "1 working day";
        return "1–2 working days";
    }

    public static int warrantyDays(String serviceName) {
        String s = serviceName == null ? "" : serviceName.toLowerCase(Locale.ROOT);
        if (s.contains("screen") || s.contains("battery") || s.contains("keyboard")) return 90;
        if (s.contains("motherboard") || s.contains("power supply")) return 60;
        return 30;
    }

    public static String warrantyExpiry(String requestedDate, int days) {
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        try {
            Date d = input.parse(requestedDate);
            Calendar c = Calendar.getInstance();
            c.setTime(d != null ? d : new Date());
            c.add(Calendar.DAY_OF_YEAR, days);
            return output.format(c.getTime());
        } catch (ParseException e) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, days);
            return output.format(c.getTime());
        }
    }
}
