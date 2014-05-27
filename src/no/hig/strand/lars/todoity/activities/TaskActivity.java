package no.hig.strand.lars.todoity.activities;

import java.io.IOException;
import java.util.List;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.adapters.PlacesAutoCompleteAdapter;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.helpers.TimePickerFragment;
import no.hig.strand.lars.todoity.helpers.TimePickerFragment.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class TaskActivity extends FragmentActivity implements OnTimeSetListener {

	private Task mTask;
	private int mPosition;
	private AutoCompleteTextView mLocationText;
	private ArrayAdapter<String> mAutoCompleteAdapter;
	private Spinner mCategory;
	private EditText mDescription;
	private CheckBox mFixedTime;
	private Button mTimeButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task);
		setupActionBar();
		
		Intent data = getIntent();
		mPosition = data.getIntExtra(Constant.POSITION_EXTRA, -1);
		if (data.hasExtra(Constant.TASK_EXTRA)) {
			mTask = data.getParcelableExtra(Constant.TASK_EXTRA);
		} else {
			mTask = new Task();	
		}
		
		setupUI();
	}

	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_task, menu);
        return true;
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_save:
			saveTask();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	private void setupUI() {
		ScrollView container = (ScrollView) findViewById(R.id.container);
		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLocationText.isFocused()) {
					InputMethodManager imm = (InputMethodManager) 
							getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					mLocationText.clearFocus();
					mLocationText.clearFocus();
				}
			}
		});
		
		// Set behavior of the category spinner.
		mCategory = (Spinner) findViewById(R.id.category_spinner);
		// Read occupation from preferences and set 
		//  predefined categories accordingly.
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String occupationPref = sharedPref.getString(
				SettingsActivity.PREF_OCCUPATION_KEY, "");
		int spinnerArray; 
		if (occupationPref.equals(getString(R.string.pref_undergraduate))) {
			spinnerArray = R.array.undergraduate_tasks_array;
		} else {
			spinnerArray = R.array.postgraduate_tasks_array;
		}
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, spinnerArray,
				android.R.layout.simple_spinner_dropdown_item);
		adapter.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		mCategory.setAdapter(adapter);
		if (! mTask.getCategory().equals("")) {
			mCategory.setSelection(adapter.getPosition(mTask.getCategory()));
		}
		
		mDescription = (EditText) findViewById(R.id.description_edit);
		mDescription.setText(mTask.getDescription());
		
		// Set up the auto complete text view with listeners.
		mLocationText = (AutoCompleteTextView) findViewById(R.id.location_text);
		mLocationText.setText(mTask.getAddress());
		mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this,
				android.R.layout.simple_dropdown_item_1line);
		mLocationText.setAdapter(mAutoCompleteAdapter);
		mLocationText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				String location = (String) adapterView
						.getItemAtPosition(position);
				mLocationText.setText(location);
				mTask.setAddress(location);
				InputMethodManager imm = (InputMethodManager) 
						getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
				mLocationText.clearFocus();
			}
		});
		mLocationText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (! hasFocus) {
					new GetLocationCoordinatesFromName().execute(
							mLocationText.getText().toString());
				}
			}
		});
		
		// Set behavior of the location button.
		Button button = (Button) findViewById(R.id.location_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						TaskActivity.this, MapActivity.class);
				if (mTask.getLatitude() != 0) {
					intent.putExtra(Constant.LOCATION_EXTRA, new LatLng(
							mTask.getLatitude(), mTask.getLongitude()));
				}
				startActivityForResult(intent, Constant.MAP_REQUEST);
			}
		});
		
		// Set behavior of the fixed time check box.
		mFixedTime = (CheckBox) findViewById(R.id.fixed_time_check);
		mFixedTime.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, 
					boolean isChecked) {
				Button fromButton = (Button) findViewById(R.id.from_button);
				Button toButton = (Button) findViewById(R.id.to_button);
				if (isChecked) {
					fromButton.setEnabled(true);
					toButton.setEnabled(true);
				} else {
					fromButton.setEnabled(false);
					toButton.setEnabled(false);
				}
			}
		});
		if (! mTask.getFixedStart().equals("")) {
			mFixedTime.setChecked(true);
		}
		
		// Set behavior of the fixed time buttons
		OnClickListener timeButtonListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTimeButton = (Button) v;
				DialogFragment timePicker = new TimePickerFragment();
				timePicker.show(getSupportFragmentManager(), "timePicker");
			}
		};
		button = (Button) findViewById(R.id.from_button);
		button.setOnClickListener(timeButtonListener);
		if (! mTask.getFixedStart().equals("")) {
			button.setText(mTask.getFixedStart());
		}
		button = (Button) findViewById(R.id.to_button);
		button.setOnClickListener(timeButtonListener);
		if (! mTask.getFixedEnd().equals("")) {
			button.setText(mTask.getFixedEnd());
		}
	}
	
	
	
	private void saveTask() {
		
		// Check if a location is chosen.
		if (mTask.getLatitude() != 0 || ! mTask.getAddress().isEmpty()) {
			
			// Check start time if the fixed time box is ticked.
			if (mFixedTime.isChecked()) {
				Button button = (Button) findViewById(R.id.from_button);
				String fixedStart = button.getText().toString();
				button = (Button) findViewById(R.id.to_button);
				String fixedEnd = button.getText().toString();
				if (! fixedStart.equals(getString(R.string.from))) {
					// Has fixed times and times are properly set.
					mTask.setFixedStart(fixedStart);
					if (! fixedEnd.equals(getString(R.string.to))) {
						mTask.setFixedEnd(fixedEnd);
					}
				} else {
					Toast.makeText(this, getString(R.string.set_time_message), 
							Toast.LENGTH_LONG).show();
					return;
				}
			// If task doesn't have fixed times, reset values in case the task
			//  is being edited.
			} else {
				mTask.setFixedStart("");
				mTask.setFixedEnd("");
			}
			
			mTask.setCategory(mCategory.getSelectedItem().toString());
			mTask.setDescription(mDescription.getText().toString());
			
			Intent data = new Intent();
			data.putExtra(Constant.TASK_EXTRA, mTask);
			if (mPosition > -1) {
				data.putExtra(Constant.POSITION_EXTRA, mPosition);
			}
			setResult(RESULT_OK, data);
			finish();
			
		} else {
			Toast.makeText(TaskActivity.this, getString(
					R.string.set_location_message), Toast.LENGTH_LONG).show();
		}
	}
	
	
	
	@Override
	public void onTimeSet(String time) {
		mTimeButton.setText(time);
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		if (requestCode == Constant.MAP_REQUEST) {
			if (resultCode == RESULT_OK) {
				LatLng location = data.getParcelableExtra(
						Constant.LOCATION_EXTRA);
				mTask.setLatitude(location.latitude);
				mTask.setLongitude(location.longitude);
				new GetLocationCoordinatesFromValue().execute(location);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	
	private class GetLocationCoordinatesFromValue extends 
			AsyncTask<LatLng, Void, String> {

		@Override
		protected String doInBackground(LatLng... params) {
			LatLng location = params[0];
			try {
				List<Address> addresses = new Geocoder(getBaseContext())
					.getFromLocation(location.latitude, location.longitude, 1);
		
				if (addresses.size() > 0) {
					Address address = addresses.get(0);
					String value = "";
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
						value += address.getAddressLine(i);
						if (i < address.getMaxAddressLineIndex() -1) {
							value += ", ";
						}
					}
					return value;
				}
			} catch (IOException e) {
				Log.d("MTP", "Could not get GeoCoder");
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				mTask.setAddress(result);
				mLocationText.setText(result);
				//Button button = (Button) findViewById(R.id.location_button);
				// Just move the focus away from the editable text
				//button.requestFocus();
			}
		}

	}
	
	
	
	private class GetLocationCoordinatesFromName extends 
			AsyncTask<String, Void, LatLng> {

		@Override
		protected LatLng doInBackground(String... params) {
			String value = params[0];
			try {
				List<Address> addresses = new Geocoder(getBaseContext())
						.getFromLocationName(value, 1);
				if (addresses.size() > 0) {
					return new LatLng(addresses.get(0).getLatitude(),
							addresses.get(0).getLongitude());
				}
			} catch (IOException e) {
				Log.d("MTP", "Could not get GeoCoder");
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(LatLng result) {
			if (result != null) {
				mTask.setLatitude(result.latitude);
				mTask.setLongitude(result.longitude);
			}
		}

	}

}
