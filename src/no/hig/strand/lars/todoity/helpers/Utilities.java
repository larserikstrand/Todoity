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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;


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
	
	
	
	public static ArrayList<String> getDatesForWeek() {
		ArrayList<String> dates = new ArrayList<String>();
		
		Calendar c = Calendar.getInstance();
		String date;
		for (int i = 0; i < Constant.TASKS_TO_DISPLAY_IN_WEEK; i++) {
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
	
	
	
	public interface OnConfirmListener {
		public void onConfirm(DialogInterface dialog, int id);
	}
	
	
	
	public static void showConfirmDialog(Context context, String title, 
			String message, String confirmText,
			final OnConfirmListener callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
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
