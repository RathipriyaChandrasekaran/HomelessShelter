package com.rathipriya.findhomeshelter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.maps.CameraUpdateFactory;

import android.support.v7.app.ActionBarActivity;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;

public class GoogleMapActivity extends ActionBarActivity {
	// latitude and longitude
	private Map<Marker, Class> allMarkersMap = new HashMap<Marker, Class>();
    LocationManager locMan;
	private Marker marker;
	private Marker[] placeMarkers;
	private final int MAX_PLACES = 20;
	private MarkerOptions[] places;
	boolean missingValue=false;
	String place_id;
	
	//private int userIcon,  shopIcon, otherIcon;

	private GoogleMap googleMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_map);
		
		 if (googleMap == null) {
	            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
	                    R.id.map)).getMap();	 
	            
	        }
		 if(googleMap!= null){
	            //ok - proceed
	        	googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	        }
	        placeMarkers = new Marker[MAX_PLACES];
	        updatePlaces();
	        googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				
				@Override
				public void onInfoWindowClick(Marker arg0) {
					// TODO Auto-generated method stub
					
				}
			});
		
		   
		 googleMap.setMyLocationEnabled(true);    
	}//oncreate end
	/**
     * function to load map. If map is not created it will create it for you
     * */
 
    
    private void updatePlaces(){
    	//update location
    	locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	Location lastLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	double lat = lastLoc.getLatitude();
    	double lng = lastLoc.getLongitude();
    	LatLng latlng = new LatLng(lat, lng);
    	
    	
    if(marker!=null) marker.remove();
     
    	marker = googleMap.addMarker(new MarkerOptions()
        .position(latlng)
        .title("Your Location")
       
        .snippet("Your current location"));
    	marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
    	
    	marker.showInfoWindow();
    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latlng), 3000, null);
 
    String latVal=String.valueOf(lat);
	String lngVal=String.valueOf(lng);
	String placesSearchStr="";
    try{
		placesSearchStr = "https://maps.googleapis.com/maps/api/place/textsearch/json?location="
	        +URLEncoder.encode(latVal, "UTF-8")
	        +","
	        +URLEncoder.encode(lngVal, "UTF-8")
	        +"&radius="
	        +URLEncoder.encode("1000", "UTF-8")
	        +"&sensor="
	        +URLEncoder.encode("true", "UTF-8")
	        +"&query="
	        +URLEncoder.encode("homeless"+"shelters", "UTF-8")
	        +"&key="
	        +URLEncoder.encode("AIzaSyDLb2RC2IcztoYKHYb35hAyWelC6EiPBI4", "UTF-8");
		
	}catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
	new GetPlaces().execute(placesSearchStr);
	
	}


    //locMan.requestLocationUpdates(LocationManager,NETORK_PROVIDER, 2000, 10, locationListener);
  

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.google_map, menu);
		return true;
	}

	
	
	  private class GetPlaces extends AsyncTask<String, Void, String> {
	    	//fetch and parse place data
	    	@Override
	    	protected String doInBackground(String... placesURL) {
	    	    //fetch places 
	    		StringBuilder placesBuilder = new StringBuilder();
	    		//process search parameter string(s)
	    		
	    		for (String placeSearchURL : placesURL) {
	    		//execute search
	    			HttpClient placesClient = new DefaultHttpClient();
	    			try {
	    			    //try to fetch the data
	    				System.out.println("search query="+placeSearchURL);
	    				HttpGet placesGet = new HttpGet(placeSearchURL);
	    				HttpResponse placesResponse = placesClient.execute(placesGet);
	    				StatusLine placeSearchStatus = placesResponse.getStatusLine();
	    				if (placeSearchStatus.getStatusCode() == 200) {
	    					//we have an OK response
	    					HttpEntity placesEntity = placesResponse.getEntity();
	    					InputStream placesContent = placesEntity.getContent();

	    					InputStreamReader placesInput = new InputStreamReader(placesContent);
	    					BufferedReader placesReader = new BufferedReader(placesInput);
	    					String lineIn;
	    					while ((lineIn = placesReader.readLine()) != null) {
	    					    placesBuilder.append(lineIn);
	    					}
	    					}
	    				
	    				
	    			}
	    			catch(Exception e){
	    			    e.printStackTrace();
	    			}

	    		}
	    		return placesBuilder.toString();
	    	}
	    	
	    	protected void onPostExecute(String result) {
	    		System.out.println("result string="+result);
	    	    //parse place data returned from Google Places
	    		if(placeMarkers!=null){
	    		    for(int pm=0; pm<placeMarkers.length; pm++){
	    		        if(placeMarkers[pm]!=null)
	    		            placeMarkers[pm].remove();
	    		    }
	    		}
	    		try {
	    		    //parse JSON
	    			JSONObject resultObject = new JSONObject(result);
	    			JSONArray placesArray = resultObject.getJSONArray("results");
	    			places = new MarkerOptions[placesArray.length()];
	    			//loop through places
	    			for (int p=0; p<placesArray.length(); p++) {
	    			    //parse each place
	    				LatLng placeLL=null;
	    				String placeName="";
	    				String vicinity="";
	    				//int currIcon = otherIcon;
	    				try{
	    				    //attempt to retrieve place data values
	    					missingValue=false;
	    					JSONObject placeObject = placesArray.getJSONObject(p);
	    					JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
	    					placeLL = new LatLng(
	    						    Double.valueOf(loc.getString("lat")),
	    						    Double.valueOf(loc.getString("lng")));
	    					JSONArray types = placeObject.getJSONArray("types");//For different types of search
	    					for(int t=0; t<types.length(); t++){
	    					    //what type is it
	    						String thisType=types.get(t).toString();
	    						System.out.println(thisType);
	    						if(thisType.contains("store")){
	    						    //currIcon = shopIcon;
	    							//marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

	    						    break;
	    						}
	    					}
	    					vicinity = placeObject.getString("formatted_address");
	    					placeName = placeObject.getString("name");
	    				}	catch(JSONException jse){
	    				    missingValue=true;
	     				   
	    				    jse.printStackTrace();
	    				}
	    				if(missingValue)    
	    					 places[p]=null;
	    				 else
	    					    places[p]=new MarkerOptions()
	    					    .position(placeLL)
	    					    .title(placeName)
	    					    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
	    					    .snippet(vicinity);
	    			}
	    		}catch (Exception e) {
	    		    e.printStackTrace();
	    		}
	    		if(places!=null && placeMarkers!=null){
	    		    for(int p=0; p<places.length && p<placeMarkers.length; p++){
	    		        //will be null if a value was missing
	    		        if(places[p]!=null)
	    		            placeMarkers[p]=googleMap.addMarker(places[p]);
	    		        
	    		    }
	    		}
	    				
	    	}			
}//GetPlaces end
	  private class GetPlaceDetails extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... placeDet) {
				// TODO Auto-generated method stub
				
				
				 //fetch places 
	    		StringBuilder placesBuilder = new StringBuilder();
	    		//process search parameter string(s)
	    		
	    		for (String placeSearchURL : placeDet) {
	    		//execute search
	    			HttpClient placesClient = new DefaultHttpClient();
	    			try {
	    			    //try to fetch the data
	    				//System.out.println("Detail query="+placeSearchURL);
	    				HttpGet placesGet = new HttpGet(placeSearchURL);
	    				HttpResponse placesResponse = placesClient.execute(placesGet);
	    				StatusLine placeSearchStatus = placesResponse.getStatusLine();
	    				if (placeSearchStatus.getStatusCode() == 200) {
	    					//we have an OK response
	    					HttpEntity placesEntity = placesResponse.getEntity();
	    					InputStream placesContent = placesEntity.getContent();

	    					InputStreamReader placesInput = new InputStreamReader(placesContent);
	    					BufferedReader placesReader = new BufferedReader(placesInput);
	    					String lineIn;
	    					while ((lineIn = placesReader.readLine()) != null) {
	    					    placesBuilder.append(lineIn);
	    					}
	    					}
	    				
	    				
	    			}
	    			catch(Exception e){
	    			    e.printStackTrace();
	    			}

	    		}
	    		return placesBuilder.toString();
			}
		
	    
	    }
	  
	 
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			 Log.v("MyMapActivity", "location changed");
			   updatePlaces();
		}

		
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Log.v("MyMapActivity", "provider disabled");
		}

	
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			Log.v("MyMapActivity", "provider enabled");
		}

		
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			 Log.v("MyMapActivity", "status changed");
		}
		@Override
	    protected void onResume() {
	        super.onResume();
	       
	        if(googleMap!=null){
		        locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 50, new LocationListener() {
					
					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderEnabled(String provider) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onLocationChanged(Location location) {
						// TODO Auto-generated method stub
						
					}
				});
		    }
	    }
	    
	    @Override
		protected void onPause() {
		    super.onPause();
		    if(googleMap!=null){
		        locMan.removeUpdates(new LocationListener() {
					
					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderEnabled(String provider) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onLocationChanged(Location location) {
						// TODO Auto-generated method stub
						
					}
				});
		    }
		}
		
		public void onInfoWindowClick(Marker marker) {
			// TODO Auto-generated method stub
			Toast.makeText(getBaseContext(), 
				       "Info Window clicked@" + marker.getPosition(), 
				       Toast.LENGTH_SHORT).show();
			marker.hideInfoWindow();
	        double dlat =marker.getPosition().latitude;
	        double dlon =marker.getPosition().longitude;
	        String slat = String.valueOf(dlat);
	        String slon = String.valueOf(dlon);
	       // Intent intent = new Intent(this, WebViewDetails.class);
	        
	        Bundle b = new Bundle();
	        b.putDouble("dlat", dlat);
	        b.putDouble("dlon", dlon);
	        //intent.putExtras(b);
	        //startActivity(intent);
		}
		
	  
}