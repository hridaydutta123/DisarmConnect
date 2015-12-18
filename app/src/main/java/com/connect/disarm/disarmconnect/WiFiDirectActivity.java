package com.connect.disarm.disarmconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.connect.disarm.disarmconnect.Shaker.Callback;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WiFiDirectActivity extends AppCompatActivity implements Callback {

    ListView lv;
    TextView wifitext, hotspottext, wifiname, hotspotname;
    ImageView imgwifi, imghotspot;
    Drawable myDrawable, myDrawable1,scanwifi,hpoff,hpon;
    WifiManager wifi;
    String wifis[]={"None"}, checkWifiState="0x";
    String ssid,ssid1;
    WifiScanReceiver wifiReciever;
    boolean b,c,wifiState;
    Timer myTimer,wifiTimer,hotspotDisplayTimer;
    WifiConfiguration wifiConfig;
    Method getConfigMethod, setConfigMethod;
    SupplicantState subState;
    WifiInfo wifiInfo;
    String networkSSID = "DisarmHotspot";
    List<String> IpAddr;
    BufferedReader br = null;
    FileReader fr = null;
    ArrayAdapter<String> arrayAdapter;
    int count=0,startwififirst = 1;
    private Shaker shaker=null;
    Vibrator v;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Logger.addRecordToLog("Activity Started");

        lv = (ListView) findViewById(R.id.listview);
        wifitext = (TextView) findViewById(R.id.WiFitextView);
        wifiname = (TextView) findViewById(R.id.namewifi);
        hotspotname = (TextView) findViewById(R.id.namehospot);
        hotspottext = (TextView) findViewById(R.id.hotspottextView2);
        imgwifi = (ImageView) findViewById(R.id.imageView);
        imghotspot = (ImageView) findViewById(R.id.imageView2);
        myDrawable = getResources().getDrawable(R.drawable.ic_signal_wifi_4_bar_black_48dp);
        myDrawable1 = getResources().getDrawable(R.drawable.ic_signal_wifi_off_black_48dp);
        scanwifi = getResources().getDrawable(R.drawable.ic_perm_scan_wifi_black_48dp);
        hpoff = getResources().getDrawable(R.drawable.ic_portable_wifi_off_black_48dp);
        hpon = getResources().getDrawable(R.drawable.ic_wifi_tethering_black_48dp);

        //passing Shaking Arguments
        shaker=new Shaker(this, 3.0d, 750, this);





        wifi =(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();

        wifi.startScan();

        this.registerReceiver(this.WifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(WiFiDirectActivity.this,log.class);
                startActivity(i);
            }
        });

      myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 60 * 1000);
        wifiTimer = new Timer();
        wifiTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                WifiTimerMethod();
            }

        }, 0, 10*1000);



        hotspotDisplayTimer = new Timer();
        hotspotDisplayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                wifihotspotDisplayTimerMethod();
            }

        }, 0, 10);


    }



    protected void onPause() {
        unregisterReceiver(wifiReciever);
        unregisterReceiver(WifiStateChangedReceiver);
        Logger.addRecordToLog("Activity onPause called");

        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(WifiStateChangedReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Logger.addRecordToLog("Activity onResume called");

        super.onResume();
    }
    public void onDestroy() {
        Logger.addRecordToLog("Activity onDestroyed called ");

        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_favorite){
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }

        return super.onOptionsItemSelected(item);
    }

    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Toggle);
    }

    private void WifiTimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(WifiConnect);
    }

    private void wifihotspotDisplayTimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.

        this.runOnUiThread(wifihpDisplay);
    }


    private Runnable Timer_Toggle = new Runnable() {

        public void run() {


            wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifi.getConnectionInfo();

            checkWifiState =  wifiInfo.getSSID();
            Toast.makeText(WiFiDirectActivity.this, "Ticking", Toast.LENGTH_SHORT).show();
            Toast.makeText(WiFiDirectActivity.this, checkWifiState, Toast.LENGTH_SHORT).show();
            count++;
            Logger.addRecordToLog("Ticking"+count);
            Logger.addRecordToLog(checkWifiState+"found"+count);




            if (checkWifiState.equals("<unknown ssid>")) {
                Toast.makeText(WiFiDirectActivity.this, "Hotspot Mode Detected", Toast.LENGTH_SHORT).show();
                Logger.addRecordToLog("Hotspot mode detected");

                boolean isReachable = false;
                try {
                    int macCount = 0;
                    fr =  new FileReader("/proc/net/arp");
                    br = new BufferedReader(fr);
                    String line;
                    IpAddr = new ArrayList<String>();
                    c = false;
                    while ((line = br.readLine()) != null)
                    {
                        String[] splitted = line.split(" +");

                        if (splitted != null )
                        {
                            if (splitted[3].matches("..:..:..:..:..:..")) {
                                Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 -t 1 " + splitted[0]);
                                int returnVal = p1.waitFor();
                                isReachable = (returnVal==0);

                            }
                            if(isReachable)
                            {
                                c = true;
                                Toast.makeText(
                                        getApplicationContext(),
                                        "C IS TRUE !!! ", Toast.LENGTH_SHORT).show();
                                Logger.addRecordToLog("c is true");
                            }

                            // Basic sanity check
                            String mac = splitted[3];
                            System.out.println("Mac : Outside If " + mac);
                            Logger.addRecordToLog("Mac : Outside If " + mac);
                            if (mac.matches("..:..:..:..:..:.."))
                            {
                                macCount++;

                                IpAddr.add(splitted[0]);

                                System.out.println("Mac : " + mac + " IP Address : " + splitted[0]);
                                System.out.println("Mac_Count  " + macCount + " MAC_ADDRESS  "+ mac);

                                Logger.addRecordToLog("Mac : " + mac + " IP Address : " + splitted[0]);
                                Logger.addRecordToLog("Mac_Count  " + macCount + " MAC_ADDRESS  "+ mac);
                                Toast.makeText(
                                        getApplicationContext(),
                                        "IP Address  " + splitted[0] + "   MAC_ADDRESS  "
                                                + mac, Toast.LENGTH_SHORT).show();
                                Logger.addRecordToLog("IP Address  " + splitted[0] + "   MAC_ADDRESS  "
                                        + mac);

                            }
                        }
                    }
                    if(c) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Connected!!! ", Toast.LENGTH_SHORT).show();

                        Logger.addRecordToLog("conneted !!");
                    }
                    else
                    {
                        Toast.makeText(
                                getApplicationContext(),
                                "Not Connected!!! ", Toast.LENGTH_SHORT).show();
                        Logger.addRecordToLog("Not Connected !!");
                    }
                } catch(Exception e){
                    Log.e("MYAPP", "exception", e);
                } finally {
                    if (fr != null) {
                        try {
                            fr.close();
                            br.close();
                            IpAddr.clear();
                            /*Toast.makeText(
                                    getApplicationContext(),
                                    "FATAL FINALLY !! ", Toast.LENGTH_SHORT).show();*/
                        } catch (IOException e) {
                            // This is unrecoverable. Just report it and move on
                            e.printStackTrace();
                        }
                    }
                }

                if(!c){
                    toggle();
                }



            } //if Completed check


            else if (checkWifiState.contains("DisarmHotspot")) {
                Toast.makeText(WiFiDirectActivity.this, "DisarmConnected Not Toggling", Toast.LENGTH_SHORT).show();
                Logger.addRecordToLog("Disarm Connected Not Toggling");
            }
            else{
                toggle();
            }

            //wifi.startScan();
                /*try {
                    Thread.sleep(5*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                /*
                List allScanResults = wifi.getScanResults();
                String a = allScanResults.get(0).toString();
                Toast.makeText(WiFiDirectActivity.this, a, Toast.LENGTH_SHORT).show();

                if (Arrays.asList(wifis).contains("DisarmHotspot")) {
                    Toast.makeText(WiFiDirectActivity.this, "Disarm Hotspot Available", Toast.LENGTH_SHORT).show();

                    checkWifiState = wifiInfo.getSSID();

                    if(!checkWifiState.equals("DisarmHotspot")) {
                        WifiConfiguration conf = new WifiConfiguration();
                        conf.SSID = "\"" + networkSSID + "\"";
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        wifi.addNetwork(conf);
                        wifi.disconnect();
                        wifi.enableNetwork(conf.networkId, true);
                        wifi.reconnect();

                    }
                }*/


        }
    };


    private Runnable WifiConnect = new Runnable() {

        public void run() {
            Toast.makeText(getApplicationContext(), "Running Autoconnector", Toast.LENGTH_SHORT).show();
            Logger.addRecordToLog("Running Auto connector");
            wifiInfo = wifi.getConnectionInfo();
            String ssidName = wifiInfo.getSSID();
            Toast.makeText(getApplicationContext(), ssidName, Toast.LENGTH_SHORT).show();
            if(ssidName.contains("DisarmHotspot")) {
                Toast.makeText(getApplicationContext(), "Already Connected", Toast.LENGTH_SHORT).show();
                Logger.addRecordToLog("Already Connected");
            }
            else if(!ssidName.equals("<unknown ssid>")){
                Toast.makeText(getApplicationContext(), "Checking For Disarm Hotspot", Toast.LENGTH_SHORT).show();
                Logger.addRecordToLog("Checking For Disarm Hotspot");
                // Connecting to DisarmHotspot WIfi on Button Click

                List allScanResults = wifi.getScanResults();
                //Toast.makeText(getApplicationContext(), allScanResults.toString(), Toast.LENGTH_SHORT).show();
                if (allScanResults.toString().contains("DisarmHotspot")) {
                    Toast.makeText(getApplicationContext(), "Connecting Disarm", Toast.LENGTH_SHORT).show();
                    Logger.addRecordToLog("Connecting Disarm");
                    String ssid = "DisarmHotspot";
                    WifiConfiguration wc = new WifiConfiguration();
                    wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
                    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int res = wifi.addNetwork(wc);
                    boolean b = wifi.enableNetwork(res, true);
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                    Logger.addRecordToLog("Connected");
                }
                else{
                    Toast.makeText(getApplicationContext(), "Disarm Not Available", Toast.LENGTH_SHORT).show();
                    Logger.addRecordToLog("Disarm Not Available");
                }

            }
        }
    };

    public void toggle(){
        Toast.makeText(WiFiDirectActivity.this, "Toggling randomly!!!", Toast.LENGTH_LONG).show();
        Logger.addRecordToLog("Toggling Randomly !!");
        //This method runs in the same thread as the UI.
        //Do something to the UI thread here
        if (startwififirst == 1){
            wifiState= false;
            startwififirst = 0;
        }else{
            wifiState = (Math.random() < 0.5);
        }


        //wifiState = false;
        // WifiState - 1 (Is Hotspot) || 0 - (CheckHotspot)
        if(wifiState) {
            wifi.setWifiEnabled(false);
            b = ApManager.isApOn(WiFiDirectActivity.this);

            if (!b) {
                ApManager.configApState(WiFiDirectActivity.this);
            }
            Toast.makeText(WiFiDirectActivity.this, "Hotspot Active", Toast.LENGTH_SHORT).show();
            Logger.addRecordToLog("Hotspot Active");
        }
        else {
            b = ApManager.isApOn(WiFiDirectActivity.this);
            if(b)
            {
                ApManager.configApState(WiFiDirectActivity.this);

            }

            wifi.setWifiEnabled(true);
            Toast.makeText(WiFiDirectActivity.this, "Wifi Active", Toast.LENGTH_SHORT).show();
            Logger.addRecordToLog("wifi Active");
            wifi.startScan();


/*
            if (wifi.getScanResults().contains("DisarmHotspot")) {
                // true
                Toast.makeText(WiFiDirectActivity.this, "Found DisarmHotspot!!", Toast.LENGTH_LONG).show();

                // connect to the hotspot
                wifiConfig = new WifiConfiguration();


                if (wifiConfig.SSID != null && wifiConfig.SSID.equals("DisarmHotspot")) {
                    wifi.disconnect();
                    wifi.enableNetwork(wifiConfig.networkId, true);
                    wifi.reconnect();


                    Toast.makeText(WiFiDirectActivity.this, "DisarmHotspot connected !!", Toast.LENGTH_LONG).show();
                }
            }*/
        }

        //expandable layout
        //final String[] array = {"Hello", "World", "Android", "is", "Awesome", "World", "Android", "is", "Awesome", "World", "Android", "is", "Awesome", "World", "Android", "is", "Awesome"};



    }
    private BroadcastReceiver WifiStateChangedReceiver
            = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE ,
                    WifiManager.WIFI_STATE_UNKNOWN);


            switch(extraWifiState){
                case WifiManager.WIFI_STATE_DISABLED:
                    wifiname.setText("WIFI  DISABLED");
                    imgwifi.setImageDrawable(myDrawable1);
                    Logger.addRecordToLog("wifi_state_disabled");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    wifiname.setText("WIFI STATE DISABLING");
                    imgwifi.setImageDrawable(myDrawable1);
                    Logger.addRecordToLog("wifi_state_disabling");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    ssid1 = wifi.getConnectionInfo().getSSID().toString();
                    wifiname.setText(ssid1);
                    imgwifi.setImageDrawable(myDrawable);
                    Logger.addRecordToLog("Connected to : "+ssid1);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    ssid1 = wifi.getConnectionInfo().getSSID().toString();
                    wifiname.setText(ssid1);
                    imgwifi.setImageDrawable(scanwifi);
                    Logger.addRecordToLog("wifi_state_enabling to : "+ssid1);
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    ssid1 = wifi.getConnectionInfo().getSSID().toString();
                    wifiname.setText(ssid1);
                    imgwifi.setImageDrawable(scanwifi);
                    Logger.addRecordToLog("wifi_state_unknown");
                    break;

            }

           /* wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifi.isWifiEnabled()) {
                String ssid = wifi.getConnectionInfo().getSSID().toString();
                if(ssid=="<unknown ssid>"){
                    wifiname.setText("WiFi Enabled");
                    imgwifi.setImageDrawable(scanwifi);
                    Toast.makeText(getApplicationContext(), "Wifi Connected!", Toast.LENGTH_SHORT).show();
                    Log.d("WifiReceiver", "Have Wifi Connection");
                }else{
                wifiname.setText(ssid);

                imgwifi.setImageDrawable(myDrawable);
                }
            } else {
                wifiname.setText("WiFi OFF/Disabled");
                imgwifi.setImageDrawable(myDrawable1);
                Toast.makeText(getApplicationContext(), "Wifi Not Connected!", Toast.LENGTH_SHORT).show();
                Log.d("WifiReceiver", "Don't have Wifi Connection");
            }*/


        }
    };


    private Runnable wifihpDisplay = new Runnable() {

        public void run() {
            /*wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifi.isWifiEnabled()) {
                String ssid = wifi.getConnectionInfo().getSSID().toString();
                if(ssid=="<unknown ssid>"){
                    wifiname.setText("WiFi Enabled");
                    imgwifi.setImageDrawable(scanwifi);

                    Log.d("WifiReceiver", "Have Wifi Connection");
                        }else{
                    wifiname.setText(ssid);

                    imgwifi.setImageDrawable(myDrawable);
                }
            } else {
                wifiname.setText("WiFi OFF/Disabled");
                imgwifi.setImageDrawable(myDrawable1);

                Log.d("WifiReceiver", "Don't have Wifi Connection");
            }*/
            b = ApManager.isApOn(WiFiDirectActivity.this);
            if (!b) {

                    imghotspot.setImageDrawable(hpoff);
                    hotspotname.setText("Hotspot is OFF");


                } else   {
                    imghotspot.setImageDrawable(hpon);
                    hotspotname.setText("Hotspot is ON");

                }


        }
    };

    @Override
    public void shakingStarted() {
        Log.d("Shake", "Shaking started!");
         v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(250);

        b = ApManager.isApOn(WiFiDirectActivity.this);
        if (b) {
            ApManager.configApState(WiFiDirectActivity.this);
            wifi.setWifiEnabled(true);
            Log.d("OnShake", "Changing WiFi ON");


        }
        else if(!b)  {
            v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(250);
            wifi.setWifiEnabled(false);
            ApManager.configApState(WiFiDirectActivity.this);
            Log.d("OnShake", "Changing Hotspot ON");
        }




    }

    @Override
    public boolean shakingStopped() {
        Log.d("Shake", "Shaking stopped!");
        return false;
    }

    /*public void testAp (MenuItem item) {

        b = ApManager.isApOn(WiFiDirectActivity.this); // check Ap state :boolean
        c = ApManager.configApState(WiFiDirectActivity.this); // change Ap state :boolean
        boolean isWifiEnable = wifi.isWifiEnabled();
        if (!isWifiEnable) {
            if (b) {
                Toast.makeText(this, "Hotspot off", Toast.LENGTH_SHORT).show();
                Logger.addRecordToLog("Hotspot Switched Off");
            } else {
                if (c) {
                    Toast.makeText(this, "Hotspot state changed (Switch On)", Toast.LENGTH_SHORT).show();
                    Logger.addRecordToLog("Hotspot Switched On");
                }
            }
        } else {
            Toast.makeText(this, "Disabling Wifi. Press Hotspot Button again !!", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(false);
        }
    }*/


        public class WifiScanReceiver extends BroadcastReceiver {

            public void onReceive(Context c, Intent intent) {
                final List<ScanResult> wifiScanList = wifi.getScanResults();
                wifis = new String[wifiScanList.size()];

                for (int i = 0; i < wifiScanList.size(); i++) {
                    wifis[i] = ((wifiScanList.get(i)).SSID);
                    Logger.addRecordToLog(wifis.toString());

                }


                lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.view_header, R.id.header_text, wifis));

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WiFiDirectActivity.this);

                        // Setting Dialog Title
                        alertDialog.setTitle("Connect");
                        for (int i = 0; i < wifis.length; i++) {

                            // Setting Dialog Message
                            alertDialog
                                    .setMessage("Connect to " + wifis[position]);
                        }

                        // Setting Positive "Yes" Button
                        alertDialog.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // Write your code here to invoke YES event
                                Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_SHORT).show();
                                Logger.addRecordToLog("AlertDialog setpositive connecting");

                                //Connecting to specific network
                                WifiConfiguration conf = new WifiConfiguration();

                                conf.SSID = "\" " + wifis[position] + "\"";
                                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


                                wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                                wifi.addNetwork(conf);

                                List<WifiConfiguration> list = wifi.getConfiguredNetworks();
                                for (WifiConfiguration i : list) {
                                    if (i.SSID != null && i.SSID.equals("\"" + wifis[position] + "\"")) {
                                        wifi.disconnect();
                                        wifi.enableNetwork(i.networkId, true);
                                        wifi.reconnect();
                                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                                        Logger.addRecordToLog("AlertDialod Connecting");

                                        break;
                                    }
                                }

                            }
                        });

                        // Setting Negative "NO" Button
                        alertDialog.setNegativeButton("Disconnect", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event
                                wifi.disconnect();
                                Toast.makeText(getApplicationContext(), "Disconnect", Toast.LENGTH_SHORT).show();
                                Logger.addRecordToLog("AlertDialog Disconnect");
                                dialog.cancel();
                            }

                        });
                        alertDialog.show();
                    }
                });








            }

        }

}