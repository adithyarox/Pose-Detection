package com.example.posebuilding;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText name,weight,height;
    Button letsgo;
    Spinner s;
    RadioGroup r;
    String[] age={"15-21","22-35","36-50","50+"};
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name = (EditText) findViewById(R.id.edtFullName);
        weight = (EditText) findViewById(R.id.edtweight);
        height = (EditText) findViewById(R.id.edtheight);
        s=(Spinner) findViewById(R.id.agegroup);
        r=(RadioGroup)findViewById(R.id.gender);
        ArrayAdapter<String> a=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item,age);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
        letsgo =  (Button)findViewById(R.id.btnSignUp);

        letsgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = name.getText().toString();
                String userweight = weight.getText().toString();
                String userheight = height.getText().toString();
                String age = s.getSelectedItem().toString();
                Log.d("Age",age);
                int sel = r.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) findViewById(sel);
                String gender = rb.getText().toString();
                Log.d("age", age);
                Log.d("gender", gender);

                Intent i = new Intent(MainActivity.this, SensorData.class);
                i.putExtra("Name", username);
                i.putExtra("Weight", userweight);
                i.putExtra("Height", userheight);
                i.putExtra("Age Group", "age");
                i.putExtra("Gender", gender);
                startActivity(i);
                Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                finish();

            }
        });

    }
}