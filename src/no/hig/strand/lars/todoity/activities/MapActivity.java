package no.hig.strand.lars.todoity.activities;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.data.Constant;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity for displaying and choosing/editing a location
 *  for a task on a map.
 * @author LarsErik
 *
 */
public class MapActivity extends FragmentActivity {

	private GoogleMap mMap;
	private LatLng mLocation;
	private MarkerOptions mMarkerOptions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent data = getIntent();
		
		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		mLocation = null;
		mMarkerOptions = new MarkerOptions();
		// Display the current location of the task (if any).
		if (data.hasExtra(Constant.LOCATION_EXTRA)) {
			mLocation = data.getParcelableExtra(Constant.LOCATION_EXTRA);
			mMarkerOptions.position(mLocation);
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
		getMenuInflater().inflate(R.menu.activity_map, menu);
        return true;
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		// The Done button is pressed.
		case R.id.action_done:
			// If location is selected, send it back to parent activity. 
			if (mLocation != null) {
				Intent data = new Intent();
				data.putExtra(Constant.LOCATION_EXTRA, mLocation);
				setResult(RESULT_OK, data);
			}
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	private void setupUI() {
		// Enable 'To my location' button.
		mMap.setMyLocationEnabled(true);
		
		if (mLocation != null) {
			mMap.addMarker(mMarkerOptions);
		}
		
		mMap.setOnMapClickListener(new OnMapClickListener() {
			// Set location and place a marker on map click.
			@Override
			public void onMapClick(LatLng latLng) {
				mMap.clear();
				mLocation = latLng;
				mMarkerOptions.position(mLocation);
				mMap.addMarker(mMarkerOptions);
			}
		});
	}

}
