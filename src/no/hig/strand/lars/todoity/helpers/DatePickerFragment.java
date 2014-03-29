package no.hig.strand.lars.todoity.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import no.hig.strand.lars.todoity.data.Constant;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

@SuppressLint("SimpleDateFormat")
public class DatePickerFragment extends DialogFragment implements 
		OnDateSetListener {
	
	private OnDateSetListener mCallback;
	
	public interface OnDateSetListener {
		public void onDateSet(String date);
	}
	
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mCallback = (OnDateSetListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + 
					" must implement OnDateSetListener");
		}
	}



	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		DatePickerDialog dialog = new DatePickerDialog(getActivity(), 
				this, year, month, day);
		dialog.getDatePicker().setMinDate(c.getTimeInMillis());
		
		return dialog;
	}
	
	
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		SimpleDateFormat formatter = 
				new SimpleDateFormat(Constant.DATE_FORMAT);
		Calendar c = new GregorianCalendar(year, monthOfYear, dayOfMonth);
		
		String date = formatter.format(c.getTime());
			
		mCallback.onDateSet(date);
	}

}
