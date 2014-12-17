package com.rathipriya.findhomeshelter;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;


public class MainActivity extends ActionBarActivity {
 //ImageView next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       //next=(ImageView)findViewById(R.id.imageButton1);  
    }//oncreate end

    public void launchMap(View view) {
    	mapView();
 
	}
 public void mapView(){
	 Intent intent=new Intent(this,GoogleMapActivity.class);
	 startActivity(intent);
 }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

   
}
