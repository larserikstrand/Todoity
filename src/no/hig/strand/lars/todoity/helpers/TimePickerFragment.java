package no.hig.strand.lars.todoity.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import no.hig.strand.lars.todoity.data.Constant;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

@SuppressLint("SimpleDateFormat")
public class TimePickerFragment extends DialogFragment implements 
		OnTimeSetListener {
	
	private OnTimeSetListener mCallback;
	
	public interface OnTimeSetListener {
		public void onTimeSet(String time);
	}
	
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mCallback = (OnTimeSetListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + 
					" must implement OnTimeSetListener");
		}
	}
	
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		
		return new TimePickerDialog(getActivity(), this, 
				hour, minute, true);
	}
	
	
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		SimpleDateFormat formatter = new SimpleDateFormat(Constant.TIME_FORMAT);
		Calendar c = Calendar.getInstance();
		c.set(0, 0, 0, hourOfDay, minute);
		
		mCallback.onTimeSet(formatter.format(c.getTime()));
	}
}
