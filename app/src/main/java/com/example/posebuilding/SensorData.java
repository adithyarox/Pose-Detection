package com.example.posebuilding;



import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



import com.example.posebuilding.ml.PoseDetection1;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensorData extends AppCompatActivity implements SensorEventListener {

    final float alpha = (float) 0.8;
    private static final int N_SAMPLES = 1;
    protected static List<Float> ax;
    protected static List<Float> ay;
    protected static List<Float> az;
    protected static List<Float> gx;
    protected static List<Float> gy;
    protected static List<Float> gz;
    float[] probabilities = new float[3];


    String name,wt,ht;
    String selectedOption;
    EditText result;
    Button buttonStart;
    Button buttonStop;
    boolean isRunning;
    final String TAG = "SensorLog";
    FileWriter awriter,gwriter;
    Spinner option;
    String[] activity={"Standing","Sitting","Sleeping"};
    //    float [] data;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                name = "";
                wt = "";
                ht = "";
            }else {
                name = extras.getString("Name");
                wt = extras.getString("Weight");
                ht = extras.getString("Height");
            }
        }else {
            name = (String) savedInstanceState.getSerializable("Name");
            wt = (String) savedInstanceState.getSerializable("Weight");
            ht = (String) savedInstanceState.getSerializable("Height");
        }

        super.onCreate(savedInstanceState);
        ax = new ArrayList<>();
        ay = new ArrayList<>();
        az = new ArrayList<>();
        gx = new ArrayList<>();
        gy = new ArrayList<>();
        gz = new ArrayList<>();
//        float[] probabilities = new float[3];
        setContentView(R.layout.sensordata);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        buttonStart = (Button)findViewById(R.id.start);
        buttonStop = (Button)findViewById(R.id.stop);
        isRunning = false;
        option = findViewById(R.id.options);
        ArrayAdapter<String> a=new ArrayAdapter<String>(SensorData.this, android.R.layout.simple_spinner_item,activity);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        option.setAdapter(a);

        option.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOption = parent.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(SensorData.this, "Nothing selected", Toast.LENGTH_SHORT).show();

            }
        });




        buttonStart.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                Log.d(TAG,"Writing to " + getStorageDir());
                try{
                    awriter = new FileWriter(new File(getStorageDir(),  selectedOption+"_" + System.currentTimeMillis() +".csv" ));
                    awriter.write(String.format("%s; %s; %s\n",name,wt,ht));
                    gwriter = new FileWriter(new File(getStorageDir(),  selectedOption+"_" + System.currentTimeMillis() +".csv" ));
                    gwriter.write(String.format("%s; %s; %s\n",name,wt,ht));
                }
                catch (IOException e){
                    e.printStackTrace();
                }

                sensorManager.registerListener(SensorData.this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),10000);
                sensorManager.registerListener(SensorData.this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),10000);
//                S
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        sensorManager.unregisterListener(listener);
//                    }
//                }, 3000);


                isRunning = true;
                return true;
            }
        });

        buttonStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                isRunning = false;
                sensorManager.flush(SensorData.this);
                sensorManager.unregisterListener(SensorData.this);
                try {
                    awriter.close();
                    gwriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

    }

    private void activityPrediction(SensorEvent sensorevent) {
//        List<Float> data = new ArrayList<>();
        float[] data = new float[7];
        int i=0,j=0;
        if (ax.size() == N_SAMPLES && ay.size() == N_SAMPLES && az.size() == N_SAMPLES &&gx.size() == N_SAMPLES && gy.size() == N_SAMPLES && gz.size() == N_SAMPLES ) {
            Log.d("Adi","HI ");
            for(i=0;i<7;i++){
                data[i++] = (float)sensorevent.timestamp;
                data[i++] = ax.get(j);
                data[i++] = ay.get(j);
                data[i++] = az.get(j);
                data[i++] = gx.get(j);
                data[i++] = gy.get(j);
                data[i++] = gz.get(j);
                j++;
            }

            Toast.makeText(this, String.format("%f",ax.get(0))+ String.format("%f",ax.get(1))+ String.format("%f",ax.get(0)), Toast.LENGTH_SHORT).show();

            try {
                Log.d("Anirudh","One");
                PoseDetection1 model = PoseDetection1.newInstance(getApplicationContext());
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 7, 1}, DataType.FLOAT32);
                Log.d("Ani","HELLO");
                inputFeature0.loadArray(data);
//
                Log.d("Hemanth","Two");
//
                Log.d("Gagandeep","Three");
                PoseDetection1.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                probabilities = outputFeature0.getFloatArray();
                int predictedClassIndex = 0;
                float highestProbability = probabilities[0];
                for(int l=0;l<probabilities.length;l++){
                    Log.d("Probab","Probabilities: "+ probabilities[l]);
                }
                Log.d("Hello","Hi");
                for (int k = 1; k < probabilities.length; k++) {
                    if (probabilities[k] > highestProbability) {
                        predictedClassIndex = k;
                        highestProbability = probabilities[k];
                    }

                }
                Log.d("Index","Class" + predictedClassIndex);

                // Now you can output the predicted class based on the index.
                String predictedClass;
                switch (predictedClassIndex) {
                    case 0:
                        predictedClass = "Sleeping";
                        break;
                    case 1:
                        predictedClass = "Sitting";
                        break;
                    case 2:
                        predictedClass = "Standing";
                        break;
                    default:
                        predictedClass = "Unknown"; // Handle the case when none of the classes have a high probability.
                }
                Log.d("Output", "Predicted activity: " + predictedClass);
                ((TextView)findViewById(R.id.Result)).setText(predictedClass);
                Toast.makeText(getApplicationContext(),predictedClass,Toast.LENGTH_SHORT).show();
                model.close();
            }

            catch(IOException e){
                // TODO Handle the exception
                e.printStackTrace();
            }
        }
        if (ax.size() == N_SAMPLES && ay.size() == N_SAMPLES && az.size() == N_SAMPLES &&gx.size() == N_SAMPLES && gy.size() == N_SAMPLES && gz.size() == N_SAMPLES ) {
            ax.clear();
            ay.clear();
            az.clear();
            gx.clear();
            gy.clear();
            gz.clear();
//            data.clear();
        }
    }

    private float[] toFloatArray(List<Float> data){
        int i = 0;
        float[] array = new float[data.size()];
        for (Float f: data){
            array[i++] = (f !=null ? f: Float.NaN);
        }
        return array;
    }




    private String getStorageDir() {
        return this.getExternalFilesDir(null).getAbsolutePath();

    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(isRunning) {
            try {
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        if(ax.size()<=N_SAMPLES && ay.size()<=N_SAMPLES && az.size()<=N_SAMPLES){
                            ax.add(sensorEvent.values[0]);
                            ay.add(sensorEvent.values[1]);
                            az.add(sensorEvent.values[2]);
                        }
                        ((TextView)findViewById(R.id.accelerovalues)).setText("X : " + sensorEvent.values[0] + ", Y : " + sensorEvent.values[1] + ", Z : " + sensorEvent.values[2]);
                        awriter.write(String.format("%d; %s; ACC; %f; %f; %f\n", sensorEvent.timestamp, selectedOption, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], 0.f, 0.f, 0.f));

                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        if(gx.size()<=N_SAMPLES && gy.size()<=N_SAMPLES && gz.size()<=N_SAMPLES) {
                            gx.add(sensorEvent.values[0]);
                            gy.add(sensorEvent.values[1]);
                            gz.add(sensorEvent.values[2]);
                        }
                        ((TextView)findViewById(R.id.gyrovalues)).setText("X : " + sensorEvent.values[0] + ", Y : " + sensorEvent.values[1] + ", Z : " + sensorEvent.values[2]);
                        gwriter.write(String.format("%d; %s; GYRO; %f; %f; %f\n", sensorEvent.timestamp,selectedOption, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], 0.f, 0.f, 0.f));
                        break;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            activityPrediction(sensorEvent);
        }
        //    activityPrediction(sensorEvent);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
    }
}