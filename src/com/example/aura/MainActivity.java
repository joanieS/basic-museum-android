package com.example.aura;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;

import java.util.List;

public class MainActivity extends Activity {
	
	  private static final String TAG = MainActivity.class.getSimpleName();
	  
	  
	  //put in the notify_activity into EXTRAS_TARGET_ACTIVITY
	  public static final String EXTRAS_TARGET_ACTIVITY = "extrasTargetActivity";
	  public static final String EXTRAS_BEACON = "extrasBeacon";

	  private static final int REQUEST_ENABLE_BT = 1234;
	  private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

	  private BeaconManager beaconManager;
	  private Beacon bacon;
	  private Beacon checker;
	  public String key = "Aura";
	  public int mode = MODE_PRIVATE;
	  private SharedPreferences settings;
	  private WebView webView;
	  private String packs;
	  private ImageView imageView;
	  
	  
	  public class JavaScriptInterface {
		    Context mContext;

		    /** Instantiate the interface and set the context */
		    JavaScriptInterface(Context c) {
		        mContext = c;
		    }
		    
		    @JavascriptInterface
		    public void clearSettings(){
		    	SharedPreferences.Editor editor = settings.edit();
				editor.clear();
				editor.apply();
		    }

		    @JavascriptInterface
		    public void loadSettings(String key, String URL) {
		    	SharedPreferences.Editor editor = settings.edit();
		    	editor.putString(key, URL);
		    	editor.apply();
		    }
		}

	  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    setContentView(R.layout.activity_main);
	    
	    WebView webView = (WebView)findViewById(R.id.webView);
	    webView.getSettings().setJavaScriptEnabled(true);
	    webView.addJavascriptInterface(new JavaScriptInterface(this), "WE6lvm0pXS");
	    imageView = (ImageView) findViewById(R.id.imageView1);
	    imageView.setBackgroundColor(Color.rgb(255, 255, 255));
	    
	    settings = getSharedPreferences(key,mode);

	    // Configure verbose debug logging.
	    L.enableDebugLogging(true);

	    // Configure BeaconManager.
	    beaconManager = new BeaconManager(this);
	    beaconManager.setForegroundScanPeriod(5000, 5000);
	    
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
		  	    	Toast.makeText(MainActivity.this, Integer.toString(bacon.getMinor()), Toast.LENGTH_SHORT).show();
		  	    }
	            
	            //If switching to a new beacon, it starts to play the song unique to it
	            // as defined by the SharedPreferences
	            if((checker == null || checker.getMinor() != bacon.getMinor()) && bacon != null  && settings.contains(Integer.toString(bacon.getMinor()))){
	            	checker = bacon;
	            	String UriString = settings.getString(Integer.toString(bacon.getMinor()), "");
	            	if(UriString != null){	            		
	            		
	            			try {
	            				
	            				//With shared UID, you can access other app raws. DLC is accessed through Context.
								Intent myIntent = new Intent(getApplicationContext(), WebActivity.class);
								myIntent.putExtra("web", settings.getString(UriString+"w", ""));
								startActivity(myIntent);
								//Need to find a way to destroy webactivity if already running
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								showToast("IllegArg");
								e.printStackTrace();
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								showToast("secure");
								e.printStackTrace();
							} catch (IllegalStateException e) {
								showToast("Illegstate");
								e.printStackTrace();
							}
	            		}
	            	}
	            		
	            		            
	        	}
	            if (getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY) != null) {
	                try {
	                  Class<?> clazz = Class.forName(getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY));
	                  Intent intent = new Intent(MainActivity.this, clazz);
	                  intent.putExtra(EXTRAS_BEACON, bacon);
	                  startActivity(intent);
	                } catch (ClassNotFoundException e) {
	                  Log.e(TAG, "Finding class by name failed", e);
	                }
	              }
	          }
	        });
	      }
	    });
	    
	  }
	
	//Sets SharedPreferences
	 public void setPreferences(String keyLime, int pieALaMode){
		 key = keyLime;
		mode = pieALaMode;
	 }
	  
	  //This clears all SharedPreferences for the setting in use.
	  public void clearSettings(View view){
		 settings = getSharedPreferences(key,mode);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();
		showToast("Settings cleared");
	  }
	  

	  @Override
	  protected void onDestroy() {
	    beaconManager.disconnect();
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
	  
	  
	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_ENABLE_BT) {
	      if (resultCode == Activity.RESULT_OK) {
	        connectToService();
	      } else {
	        Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
	        getActionBar().setSubtitle("Bluetooth not enabled");
	      }
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	  }

	  private void connectToService() {
		  
		    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
		      @Override
		      public void onServiceReady() {
		        try {
		          beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
		          
		          
		        } catch (RemoteException e) {
		          Toast.makeText(MainActivity.this, "Cannot start ranging, something terrible happened",
		              Toast.LENGTH_LONG).show();
		          Log.e(TAG, "Cannot start ranging", e);
		        }
		      }
		    });
		  }
	  
	  private void showToast(String text) {
		    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
		  }

	
}
