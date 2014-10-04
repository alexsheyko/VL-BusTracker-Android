package com.nyubustracker.models;

import android.content.res.Resources;

import com.nyubustracker.R;
import com.nyubustracker.activities.MainActivity;
import com.nyubustracker.helpers.BusManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;

public class Time {
    /*
     // compare is used to sort the list of times being checked for the "nextBusTime" in MainActivity.
     public static final Comparator<Time> compare = new Comparator<Time>() {
         // Return a negative number if Time1 is before, positive number if time2 is before, and 0 otherwise.
         @Override
         public int compare(Time time1, Time time2) {
             // timeOfWeek is an enum. ordinal() returns the rank of the given TimeOfWeek.
             if (time1.getTimeOfWeek().ordinal() == time2.getTimeOfWeek().ordinal()) {    // Times at the same time in the week.
                 if (time1.isStrictlyBefore(time2)) {     // Checks hour and minute. Returns false if they're equal or time2 is before.
                     return -1;
                 }
                 if (time2.isStrictlyBefore(time1)) {
                     return 1;
                 }
                 // Same exact time (hour, minute, and timeOfWeek). So, check if we're looking at the current time.
                 if (time1.getRoute() == null) {
                     return -1;
                 }
                 if (time2.getRoute() == null) {
                     return 1;
                 }
                 // Times are the same, but we aren't comparing the current time.
                 return 0;
             }
             else if (time1.getTimeOfWeek().ordinal() < time2.getTimeOfWeek().ordinal()) {    // Time1 is an earlier day.
                 return -1;
             }
             else {       // Time2 is an earlier day.
                 return 1;
             }
         }
     };
     */
    //private final TimeOfWeek timeOfWeek;  // Either Weekday, Friday, Weekend.
    private int hour;           // In 24 hour (military) format.
    private int min;
    private int minutes;
    private long lastupdate;
    //private boolean AM;         // Used for parsing the input string ("8:04 PM") => 20:04, AM = true
    private String route;       // What route this time corresponds to.

    public Time(String time, String mRoute) {           // Input a string 120 minutes
        //AM = time.toLowerCase(Locale.ROOT).contains("am");       // Automatically accounts for AM/PM with military time.
        //String amOrPm = AM ? "am" : "pm";
        //hour = Integer.parseInt(time.substring(0, time.indexOf(":")).trim());
        //min = Integer.parseInt(time.substring(time.indexOf(":") + 1, time.toLowerCase().indexOf(amOrPm)).trim());
        Integer sec = Integer.parseInt(time);
        minutes = Math.round(sec / 60);
        lastupdate = System.currentTimeMillis();
        //timeOfWeek = mTimeOfWeek;
        route = mRoute;
    }

    /*
    // Create a new Time given a military hour and minute.
    public Time(int mHour, int mMin) {
        AM = mHour < 12;
        hour = mHour;
        min = mMin;
        timeOfWeek = getCurrentTimeOfWeek();
    }*/

    private TimeOfWeek getCurrentTimeOfWeek() {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String dayOfWeek = rightNow.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        TimeOfWeek timeOfWeek = TimeOfWeek.Weekday;
        if (dayOfWeek.equals("Saturday") || dayOfWeek.equals("Sunday"))
            timeOfWeek = TimeOfWeek.Weekend;
        else if (dayOfWeek.equals("Friday")) timeOfWeek = TimeOfWeek.Friday;
        return timeOfWeek;
    }

    /*
    public static Time getCurrentTime() {
        //Calendar rightNow = Calendar.getInstance();
        //rightNow.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return new Time(rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE));
    }
    */

    /*
    // Returns a String representation of the time of week this Time is in.
    public String getTimeOfWeekAsString() {
        switch (timeOfWeek) {
            case Weekday:
                return "Weekday";
            case Friday:
                return "Friday";
            case Weekend:
                return "Weekend";
        }
        //if (MainActivity.LOCAL_LOGV) Log.e("Time Debugging", "Invalid timeOfWeek");
        return "";      // Should never reach here.
    }
    */

    public Boolean notExpire() {
        if((System.currentTimeMillis()-lastupdate)>1000*60){
            return false;
        }else return true;

    }

    public String getRoute() {
        return route;
    }

    public String getTimeAsString(){
        return Integer.toString(minutes);
    }
    /*
    // Return a nice string saying the difference between this time and the argument.
    public String getTimeAsStringUntil(Time t, Resources resources) {
        Time difference = this.getTimeAsTimeUntil(t);
        //if (MainActivity.LOCAL_LOGV) Log.v("Time Debugging", "this: " + this.toString() + " | that: " + t.toString());
        //if (MainActivity.LOCAL_LOGV) Log.v("Time Debugging", "Difference: " + difference.hour + ":" + difference.min);
        if (difference != null) {
            /*
            if (this.getTimeOfWeek() != t.getTimeOfWeek()) {
                BusManager.getBusManager().setIsNotDuringSafeRide(false);
                return resources.getString(R.string.offline);
            }

            if (difference.hour >= 3) {
                BusManager.getBusManager().setIsNotDuringSafeRide(false);
                return resources.getString(R.string.offline);
            }
            if (difference.hour == 0 && difference.min == 0) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return resources.getString(R.string.less_one_minute);
            }
            if (difference.hour == 0 && difference.min == 1) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return resources.getString(R.string.one_minute);
            }
            if (difference.hour == 0 && difference.min > 1) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return difference.min + resources.getString(R.string.minutes);
            }
            if (difference.hour > 1 && difference.min == 0) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return difference.hour + resources.getString(R.string.hours);
            }
            if (difference.hour == 1 && difference.min == 0) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return resources.getString(R.string.hour);
            }
            if (difference.hour > 1 && difference.min == 1) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return difference.hour + resources.getString(R.string.hours_and) + difference.min + resources.getString(R.string.one_minute);
            }
            if (difference.hour > 1 && difference.min > 1) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return difference.hour + resources.getString(R.string.hours_and) + difference.min + resources.getString(R.string.minutes);
            }
            if (difference.hour == 1 && difference.min == 1) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return resources.getString(R.string.hour_and_one_min);
            }
            if (difference.hour == 1 && difference.min > 1) {
                BusManager.getBusManager().setIsNotDuringSafeRide(true);
                return resources.getString(R.string.hour_and) + difference.min + resources.getString(R.string.minutes);
            }
        }
        return "";
    }
*/

    /*
    // Return a Time object who represents the difference in time between the two Times.
    public Time getTimeAsTimeUntil(Time t) {
        if (this.isBefore(t)) {
            //if (MainActivity.LOCAL_LOGV) Log.v("Time Debugging", this + " is strictly before " + t);
            int hourDifference = t.hour - this.hour;
            int minDifference = t.min - this.min;
            if (minDifference < 0) {
                hourDifference--;
                minDifference += 60;
            }
            return new Time(hourDifference, minDifference);
        } else {
            return new Time(99, 99);    // This time is 'infinitely' far away.
        }
    }
    */

    /*
    public TimeOfWeek getTimeOfWeek() {
        return timeOfWeek;
    }
    */

    public boolean isBefore(Time t) {
        return (this.hour < t.hour) || (this.hour == t.hour && this.min <= t.min);
    }

    public boolean equals(Object t) {
        if (t instanceof Time) {
            Time time = (Time) t;
            return (time.hour == this.hour && time.min == this.min);
        }
        return false;
    }

    public String toString() {
        return getHour() + ":" + getMinInNormalTime() + " ";
    }

    /*
    // Return this Time in 12-hour format.
    private int getHourInNormalTime() {
        if (hour == 0 && AM) return 12;
        if (hour > 0 && AM) return hour;
        if (hour > 12 && !AM) return hour - 12;
        if (hour <= 12 && !AM) return hour;
        return hour;
    }
*/
    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return min;
    }

    /*
    public boolean isAM() {
        return AM;
    }
    */

    // Ensure the minute string is 2 digits long.
    private String getMinInNormalTime() {
        if (min < 10) return "0" + min;
        else return Integer.toString(min);
    }
/*
    private String getAMorPM() {
        return AM ? "AM" : "PM";
    }
    */

    // isStrictlyBefore(t) returns false if the times are equal or this is after t.
    private boolean isStrictlyBefore(Time t) {
        //if (MainActivity.LOCAL_LOGV) Log.v("Time Debugging", this.toString() + " is strictly before " + t.toString() + ": " + ((this.hour < t.hour) || (this.hour == t.hour && this.min < t.min)));
        return (this.hour < t.hour) || (this.hour == t.hour && this.min < t.min);
    }

    public enum TimeOfWeek {Weekday, Friday, Weekend}


    public static void parseJSONA(JSONArray times, String id, Integer typeId) throws JSONException {
        BusManager sharedManager = BusManager.getBusManager();
        if (typeId==1) { //stop
            Stop stop = sharedManager.getStopByID(id);
            if (stop != null) stop.setLastUpdateTime(System.currentTimeMillis());

            if ((stop != null) && (times != null)) {
                stop.cleanTime();   //clean

                for (int i = 0; i < times.length(); i++) {
                    // Возвращается массив массивов вида [id_route, udid, interval]
                    JSONArray t = times.getJSONArray(i);
                    if (t.length() == 3) {
                        String id_route = t.getString(0);
                        String busid = t.getString(1);
                        String sec = t.getString(2);

                        stop.addTime(new Time(sec, id_route));

                    }
                }

            }
        }else{
            if (typeId==2){ //bus
                Bus bus = sharedManager.getBus(id);
                if (bus != null) bus.setLastUpdateTime(System.currentTimeMillis());
                if ((bus != null) && (times != null)) {
                    bus.cleanTime();   //clean

                    for (int i = 0; i < times.length(); i++) {
                        //[[5584,5],[5579,21],[5571,79],[10179,88],[6758,108]
                        JSONArray t = times.getJSONArray(i);
                        if (t.length() == 2) {
                            String id_stop = t.getString(0); //set to id_route
                            String sec = t.getString(1);

                            bus.addTime(new Time(sec, id_stop));
                        }
                    }

                }


            }
        }//if typeid=1

    }
}