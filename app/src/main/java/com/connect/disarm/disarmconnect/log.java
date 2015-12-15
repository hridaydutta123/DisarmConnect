package com.connect.disarm.disarmconnect;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Sanna on 11-12-2015.
 */
public class log extends WiFiDirectActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Find the directory for the SD Card using the API
        //*Don't* hardcode "/sdcard"
        File sdcard = Environment.getExternalStorageDirectory();

        //Get the text file
        File file = new File(sdcard,"/sdcard/Files/disarmconnect/disarmconnect_Log.txt");

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        //Find the view by its id
        ScrollView scrollView=(ScrollView)findViewById(R.id.sv);
        scrollView.fullScroll(View.FOCUS_DOWN);


        TextView tv = (TextView)findViewById(R.id.textView);
        tv.setBackgroundColor(android.R.color.black);
        tv.setTextColor(Color.GREEN);


        //Set the text
        tv.setText(text.toString());


    }
}
