package no.hig.strand.lars.todoity.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import no.hig.strand.lars.todoity.data.Constant;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public final class Utilities {

	public Utilities() {}
	
	
	@SuppressLint("SimpleDateFormat")
	public static String getTodayDate() {
		SimpleDateFormat formatter = new SimpleDateFormat(Constant.DATE_FORMAT);
		Calendar c = Calendar.getInstance();
		
		return formatter.format(c.getTime());
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	public static long dateToMillis(String date) {
		long dateInMillis = 0;
		SimpleDateFormat formatter = 
				new SimpleDateFormat(Constant.DATE_FORMAT);
		formatter.setLenient(false);
		try {
			Date d = formatter.parse(date);
			dateInMillis = d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		return dateInMillis;
	}
	
	
	
	public static ArrayList<String> getDates(int days) {
		ArrayList<String> dates = new ArrayList<String>();
		
		Calendar c = Calendar.getInstance();
		String date;
		for (int i = 0; i < days; i++) {
			date = Utilities.millisToDate(c.getTimeInMillis());
			dates.add(date);
			c.add(Calendar.DATE, 1);
		}
		
		return dates;
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	public static String millisToDate(long timeInMillis) {
		SimpleDateFormat formatter = new SimpleDateFormat(Constant.DATE_FORMAT);
		return formatter.format(new Date(timeInMillis));
	}
	
	
	
	public static long getTimeSinceMidnight(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return time - c.getTimeInMillis();
	}
	
	
	
	/**
	 * Converts time of day from String format (HH:mm) to millisecond long.
	 * @param time - Time string to be converted.
	 * @return time in milliseconds as a long value.
	 */
	@SuppressLint("SimpleDateFormat")
	public static long timeToMillis(String time) {
		Calendar c = Calendar.getInstance();
		String times[] = time.split(":");
		c.set(0, 0, 0, Integer.valueOf(times[0]), Integer.valueOf(times[0]), 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}
	
	
	
	public static int getDayOfWeek(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return c.get(Calendar.DAY_OF_WEEK);
	}
	
	
	
	public interface OnConfirmListener {
		public void onConfirm(DialogInterface dialog, int id);
	}
	
	
	
	public static void showConfirmDialog(Context context, String title, 
			String message, String confirmText,
			final OnConfirmListener callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (title != null) {
			builder.setTitle(title);
		}
		builder.setMessage(message);
		builder.setPositiveButton(confirmText, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onConfirm(dialog, which);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, 
				new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.create().show();
	}
	
	
	
	public static boolean isGooglePlayServicesAvailable(
			FragmentActivity context) {
		
		int resultCode = 
				GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		
		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		} else {
			
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode, context, 
					Constant.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(context.getSupportFragmentManager(),
						"Google Play Services");
			}
		}
		return false;
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	public static class DateComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			SimpleDateFormat formatter = 
					new SimpleDateFormat(Constant.DATE_FORMAT);
			formatter.setLenient(false);
			Date date1 = null, date2 = null;
			try {
				date1 = formatter.parse(lhs);
				date2 = formatter.parse(rhs);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date1.compareTo(date2);
		}
	}
	
}
