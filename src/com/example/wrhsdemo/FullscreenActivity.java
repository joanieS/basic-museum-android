package com.example.wrhsdemo;

import java.io.IOException;
import java.util.List;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;
import com.example.wrhsdemo.util.SystemUiHider;

import android.bluetooth.BluetoothAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	
///////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
	
	
	  private BeaconManager beaconManager;
	  private Beacon bacon;
	  private Beacon checker;
	  private EditText minorEditView;
	  public String key = "Aura";
	  public int mode = MODE_PRIVATE;
	  private int minmin;
	  private MediaPlayer mp;
	  private String packs = "";
	  private SurfaceView surfaceView;
	  private ImageView imageView;
	  private static final String TAG = FullscreenActivity.class.getSimpleName();
	  
	  
	  //put in the notify_activity into EXTRAS_TARGET_ACTIVITY
	  public static final String EXTRAS_TARGET_ACTIVITY = "extrasTargetActivity";
	  public static final String EXTRAS_BEACON = "extrasBeacon";

	  private static final int REQUEST_ENABLE_BT = 1234;
	  private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		
///////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
		final SharedPreferences settings = getSharedPreferences("WRHS", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();
		if(!settings.contains(Integer.toString(65535))){
			editor.putString(Integer.toString(65535), "wedellwilliams");
			editor.putString(Integer.toString(39720), "http://lufthouse.wordpress.com/2014/06/03/road-trip-1926-jordan-house-car/");
			editor.putString(Integer.toString(62689), "oldsmobile");
			editor.putString(Integer.toString(18722), "http://lufthouse.wordpress.com/2014/06/03/wrhs-did-you-like-what-you-saw/");
			editor.commit();
		}
		mp= MediaPlayer.create(getApplicationContext(),R.raw.silence);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		imageView = (ImageView) findViewById(R.id.imageView1);
		
		beaconManager = new BeaconManager(this);
	    beaconManager.setForegroundScanPeriod(5000, 1000);
	    
	    beaconManager.setRangingListener(new BeaconManager.RangingListener() {
	      @Override
	      public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
	        // Note that results are not delivered on UI thread.
	        runOnUiThread(new Runnable() {
	        	
	          @Override
	          public void run() {
	        	 
	            // Note that beacons reported here are already sorted by estimated
	            // distance between device and beacon.
	        	  
	        	// checks that the beacons array has any nearby beacons within it
	        	if(beacons.size() > 0){
	        		bacon = beacons.get(0);
	        		
	        	// if there are beacons nearby, then a toast with the minor number is shown 
	            if(bacon != null){
		  	    	Toast.makeText(FullscreenActivity.this, Integer.toString(bacon.getMinor()), Toast.LENGTH_SHORT).show();
		  	    }
	            
	            
	            //If switching to a new beacon, it starts to play the song unique to it
	            // as defined by the SharedPreferences
	            if((checker == null || checker.getMinor() != bacon.getMinor()) && bacon != null  && settings.contains(Integer.toString(bacon.getMinor()))){
	            	checker = bacon;
	            	surfaceView.setVisibility(View.GONE);
	            	mp.reset();
	            	String UriString = settings.getString(Integer.toString(bacon.getMinor()), "");
	            	Context ct = FullscreenActivity.this.getApplicationContext();
    				Resources res = ct.getResources();
    				int ResID = res.getIdentifier(UriString, "raw", "com.example.wrhsdemo");
	            	if(UriString != null && 39720!=bacon.getMinor() && 18722!=bacon.getMinor()){	            		
	            		
	            		//Music playing if it is Lufthouse native
	            		
						try {
							mp = MediaPlayer.create(ct,ResID);
							if(65535 == bacon.getMinor()){
								SurfaceHolder surfaceHolder = surfaceView.getHolder();
								surfaceView.setVisibility(View.VISIBLE);
								surfaceView.bringToFront();
								mp.setDisplay(surfaceHolder);
							}
							mp.start();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						}
	            	}
	            	else if(UriString != null && (39720==bacon.getMinor() || 18722==bacon.getMinor())){
	            		//make a webactivity class
	            		Intent myIntent = new Intent(getApplicationContext(), WebActivity.class);
						myIntent.putExtra("web",UriString);
						startActivity(myIntent);
	            		
	            	}
	            	}	            
	        	}
	}});}});}
	          
	private String getRealPathFromURI(Uri contentUri) {
		  String[] proj = { MediaStore.Images.Media.DATA };
		  
		  CursorLoader cursorLoader = new CursorLoader(
		            this, 
		            contentUri, proj, null, null, null);        
		  Cursor cursor = cursorLoader.loadInBackground();
		  
		  int column_index = 
		    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		  cursor.moveToFirst();
		  return cursor.getString(column_index); 
		 }
	  @Override
	  protected void onDestroy() {
	    beaconManager.disconnect();
	    mp.release();
	    super.onDestroy();
	  }
	  
	  @Override
	  protected void onStart() {
	    super.onStart();

	    // Check if device supports Bluetooth Low Energy.
	    if (!beaconManager.hasBluetooth()) {
	      Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
	      return;
	    }

	    // If Bluetooth is not enabled, let user enable it.
	    if (!beaconManager.isBluetoothEnabled()) {
	      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	    } else {
	      connectToService();
	    }
	  }
	  
	  @Override
	  protected void onStop() {
	    try {
	      beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
	    } catch (RemoteException e) {
	      Log.d(TAG, "Error while stopping ranging", e);
	    }

	    super.onStop();
	  }
	  
	  private void connectToService() {
		  
		    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
		      @Override
		      public void onServiceReady() {
		        try {
		          beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
		          
		          
		        } catch (RemoteException e) {
		          Toast.makeText(FullscreenActivity.this, "Cannot start ranging, something terrible happened",
		              Toast.LENGTH_LONG).show();
		          Log.e(TAG, "Cannot start ranging", e);
		        }
		      }
		    });
		  }
	  
}
	  
	  