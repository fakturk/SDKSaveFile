package com.example.fakturk.sdksavefile;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    File sd;
    Calendar c ;
    String path ;
    String mDesttxtFilename ;
    File myFile ;
    BufferedOutputStream bos;
    Button buttonStart;
    Button buttonPlus;
    Button buttonMinus;
    TextView degreeTV;
    TextView countDownTV;
    EditText editTextDegree;
    boolean startLogging=false;
    boolean timeStarted=false;
    long startTime=0;
    int repeatNumber=1;
    int degree=0;
    float[] acc;
    long time;
    String accMesaage;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        startService(new Intent(this, SensorService.class));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonMinus = (Button) findViewById(R.id.buttonMinus);
        buttonPlus = (Button) findViewById(R.id.buttonPlus);
        degreeTV = (TextView) findViewById(R.id.degreeTV);
        countDownTV = (TextView) findViewById(R.id.countDownTV);
        editTextDegree = (EditText) findViewById(R.id.editTextDegree);

        final SharedPreferences settings = getSharedPreferences("netlab.fakturk.degree", 0);
//        int defaultValue = getResources().getInteger(R.string.degree_default);
        degree = settings.getInt("degree",0);
        editTextDegree.setText(String.valueOf(degree));

        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        sd = Environment.getExternalStorageDirectory();
//         c = Calendar.getInstance();
//         path = sd + "/" + degree+ "_Degree_" +c.getTime() + ".txt";
//         mDesttxtFilename = path;
//         myFile = new File(mDesttxtFilename);
//
//        FileOutputStream fOut = null;
        verifyStoragePermissions(this);
//
//        try
//        {
//            myFile.createNewFile();
//            fOut = new FileOutputStream(myFile);
//            bos = new BufferedOutputStream(fOut);
//
//        } catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }

//        sensorValue();

        time=0;

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                acc = (intent.getFloatArrayExtra("ACC_DATA"));
                if (acc!=null)
                {
//                    accMesaage = intent.getLongExtra("TIME",0)+" "+ acc[0]+" "+acc[1]+" "+acc[2]+"";
//                    Log.d("accSDK  ", "    "+accMesaage);
                    time = intent.getLongExtra("TIME",0);
//                    System.out.println(acc[0]+" "+acc[1]+" "+acc[2]);
//                    float t = (time-startTime)/1000000000.0f;
//        System.out.println(time+" "+startTime+" "+t);
                    writeData(time,acc[0],acc[1],acc[2]);


                }

            }
        }, new IntentFilter(SensorService.ACTION_SENSOR_BROADCAST));

                editTextDegree.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                degree = Integer.parseInt(editTextDegree.getText().toString());
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("degree", degree);
//                c = Calendar.getInstance();
//                path = sd + "/" + degree+ "_Degree_" +c.getTime() + ".txt";
                editor.commit();
            }
        });
        buttonPlus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int d = Integer.parseInt( editTextDegree.getText().toString());
                d += 10;
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("degree", d);
//                c = Calendar.getInstance();
//                path = sd + "/" + d+ "_Degree_" +c.getTime() + ".txt";
                editor.commit();
                editTextDegree.setText(String.valueOf(d));
            }
        });

        buttonMinus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int d = Integer.parseInt( editTextDegree.getText().toString());
                d -= 10;
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("degree", d);
//                path = sd + "/" + d+ "_Degree_" +c.getTime() + ".txt";
                editor.commit();
                editTextDegree.setText(String.valueOf(d));
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (buttonStart.getText().equals("Start"))
                {
                    buttonStart.setText("Stop");
                    new CountDownTimer(5000,1000)
                    {
                        @Override
                        public void onTick(long millisUntilFinished)
                        {
                            countDownTV.setText("Seconds Remaining: " + millisUntilFinished / 1000);
                        }

                        @Override
                        public void onFinish()
                        {
                            countDownTV.setText("Processing");
                            setPath(settings, repeatNumber);
                            startLogging=true;
                            timeStarted=false;
                            new CountDownTimer(1000,1000)
                            {
                                @Override
                                public void onTick(long millisUntilFinished)
                                {
                                    countDownTV.setText("Process "+repeatNumber+" Remaining: " + millisUntilFinished / 1000);
                                }

                                @Override
                                public void onFinish()
                                {

                                    try
                                    {
                                        bos.close();

                                    } catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }





                                    if(repeatNumber<100)
                                    {
                                        repeatNumber++;
                                        timeStarted=false;
                                        setPath(settings, repeatNumber);
                                        start();
                                    }
                                    else
                                    {

                                        startLogging=false;
                                        countDownTV.setText("Process Finished");
                                        buttonStart.setText("Start");
                                        repeatNumber=1;
                                    }


                                }


                            }.start();

                        }


                    }.start();



                }
                else
                {
                    buttonStart.setText("Start");
                    countDownTV.setText("Process Stopped");
                    startLogging=false;

                    try
                    {
                        bos.close();

                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void setPath(SharedPreferences settings, int repeatNumber)
    {
        c = Calendar.getInstance();
        degree = settings.getInt("degree",0);
        System.out.println(degree);
        path = sd + "/" + degree+ "_Degree_" +repeatNumber+"_Repeat_"+c.getTime() + ".txt";
        System.out.println(path);
        mDesttxtFilename = path;
        myFile = new File(mDesttxtFilename);
        FileOutputStream fOut = null;
        try
        {
            myFile.createNewFile();
            fOut = new FileOutputStream(myFile);
            bos = new BufferedOutputStream(fOut);

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void writeData(long time, float x, float y, float z)
    {
        float t = (time-startTime)/1000000000.0f;
//        System.out.println(time+" "+startTime+" "+t);

        String acc =  String.format("%.02f", t)+ " " + x + " " + y + " " + z+"\n";

//        System.out.println(acc);
        if (startLogging)
        {
            if (timeStarted==false)
            {
//                System.out.println("timeStarted");
                timeStarted=true;
                startTime=time;
                t = (time-startTime)/1000000000.0f;
                acc =  String.format("%.02f", t)+ " " + x + " " + y + " " + z+"\n";

            }
            try
            {
                bos.write(acc.getBytes());

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }



    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
//
//    public native void sensorValue();
//
//    public native void startSensorPrint();
//
//    public native void stopSensorPrint();



    @Override
    protected void onDestroy()
    {

        super.onDestroy();
        stopService(new Intent(this,SensorService.class));
    }
    @Override
    protected void onPause() {

        super.onPause();
        stopService(new Intent(this,SensorService.class));


    }

    @Override
    protected void onResume() {

        super.onResume();
        startService(new Intent(this, SensorService.class));
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}