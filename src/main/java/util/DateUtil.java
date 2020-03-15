package util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static long nanosToMicros(long nanoSecs){
		return nanoSecs / 1000L;
	}

	public static long nanosToMilis(long nanoSecs) {
		return nanoSecs / 1000000L;
	}

	public static long nanosToSecs(long nanoSecs) {
		return nanoSecs / 1000000000L;
	}

	public static long milisToSecs(long miliSecs) {
        return miliSecs / 1000L;
	}

	/** From an interval in nanoSecs, returns a formatted time representation easier to understand */
	public static String getTime(long nanoSecs) {
		float value = nanoSecs;
    	if(value < 1000){
    		return value+ " nanosecs";
    	}

    	float newValue = value / 1000F; //microsecs
		if(newValue < 1000){
			return newValue+ " microsecs";
		}

		value = newValue;
		newValue = value / 1000F; //milisecs
		if(newValue < 1000){
			return newValue+ " milisecs";
		}

		value = newValue;
		newValue = value / 1000F; //secs
		if(newValue < 60){
			return newValue+ " secs";
		}

		value = newValue;
		newValue = value / 60F; //mins
		if(newValue < 60){
			return newValue+ " mins";
		}

		value = newValue;
		newValue = value / 60F;
		return newValue+ " hours";
	}

	public static String formatDate() {
		return formatDate(new Date());
	}

	public static String formatDate(Date date) {
		return getFormatDate().format(date);
	}

	public static String formatDateTimeFull() {
        return getFormatDateTimeFull().format(new Date());
	}

	public static String formatDateTime() {
		return formatDateTime(new Date());
	}

	public static String formatDateTime(long date) {
		return formatDateTime(new Date(date));
	}

	public static String formatDateTime(Date date) {
		return getFormatDateTime().format(date);
	}

	public static String format(String pattern) {
		return new SimpleDateFormat(pattern).format(new Date());
	}

	public static String format(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	public static DateFormat getFormatDate() {
		return new SimpleDateFormat("yyyy/MM/dd");
	}

	public static DateFormat getFormatDateTime() {
		return new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
	}

	public static DateFormat getFormatDateTimeFull() {
	    return new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss.SSS");
	}

	public static void sleepUpToOneSecond() {
		try {
            Thread.sleep((long)(Math.random()*1000));
        } catch (InterruptedException e) { e.printStackTrace(); }
	}
}
