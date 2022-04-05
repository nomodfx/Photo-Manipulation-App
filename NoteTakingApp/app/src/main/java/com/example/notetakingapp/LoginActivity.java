package com.example.notetakingapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    private Button Confirm;
    private EditText userName, password;
    String usernameStr, passStr;
    String prevStarted = "yes";

    @Override
    protected void onResume() {
        super.onResume();
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        //checks to see if the user has been to login page previously
        if (!sharedPref.getBoolean(prevStarted, false)) {
            //if user has not been to login page set previously started to true once they open login
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(prevStarted, Boolean.TRUE);
            editor.apply();
        } /*else {
            //If previously been on login page move to main page function call
            moveToMain();
        }*/
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        Confirm = findViewById(R.id.Confirm);
        //ViewAll = findViewById(R.id.ViewAll);
        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);

        sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE);



        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    usernameStr = userName.getText().toString();
                    passStr = password.getText().toString();
                    //checks if user entered a username and password greater than 4 characters
                    if (usernameStr.length() > 4 && passStr.length() > 4) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        //edits username and password once user clicks confirm
                        editor.putString("Username", usernameStr);
                        editor.putString("Password", passStr);
                        editor.apply();
                        Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_LONG).show();
                        //Moves to main activity after user is confirmed
                        Intent intent = new Intent(LoginActivity.this, FilterActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Please enter a Username and/or a Password greater than 4 characters.", Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /*public void moveToMain(){
        //Switches to main activity page once called in onResume method
        Intent intent = new Intent(LoginActivity.this,FilterActivity.class);
        startActivity(intent);

    }*/
}