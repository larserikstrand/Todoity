package no.hig.strand.lars.todoity.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;


public class Utilities {

	
	@SuppressLint("SimpleDateFormat")
	public static String getTodayDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy");
		Calendar c = Calendar.getInstance();
		
		return formatter.format(c.getTime());
	}
	
}
