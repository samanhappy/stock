package com.samanhappy.stock.util;

import java.util.Calendar;

public class TimeUtil
{
    public static long getAddTs(long ts, int field, int amount)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts);
        cal.add(field, amount);
        return cal.getTimeInMillis();
    }
}
