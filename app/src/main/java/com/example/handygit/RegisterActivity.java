package com.example.handygit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handygit.Utilities.Location;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public final static String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private EditText editTxtFullName, editTxtEmail, editTxtPassword, editTxtPhone;
    private String FullName, Email, Password, Phone;
    private int LAUNCH_Map_ACTIVITY = 1;
    private GeoPoint latLng;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
    }

    void init() {

        mContext = this;
        editTxtFullName = findViewById(R.id.editTxtFullName);
        editTxtEmail = findViewById(R.id.editTxtEmail);
        editTxtPassword = findViewById(R.id.editTxtPassword);
        editTxtPhone = findViewById(R.id.editTxtPhone);


        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
    }

    public void OnRegistration(View view) {

        if (!InputIsValid()) return;

        mAuth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            String UserID = task.getResult().getUser().getUid();
                            Register(UserID);
                        } else
                            Toast.makeText(getApplicationContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void Register(String UID) {

        db.collection("Users") // add user info to firebase
                .document(UID).set(new User(FullName, Email, Password, Phone,"User",latLng)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getBaseContext(), "successfully registered", Toast.LENGTH_LONG).show();

                    finish();

                } else
                    Toast.makeText(getBaseContext(), "Erorr : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    boolean InputIsValid() {

        FullName = editTxtFullName.getText().toString().trim();
        Email = editTxtEmail.getText().toString().trim();
        Password = editTxtPassword.getText().toString().trim();
        Phone = editTxtPhone.getText().toString().trim();

        boolean isValid = true;
        if (FullName.isEmpty()) {
            editTxtFullName.setError("Enter the full name");
            isValid = false;
        }

        if (!(Email.matches(emailPattern) && Email.length() > 0)) {
            isValid = false;
            editTxtEmail.setError("invalid email");
        }


        if (Password.isEmpty()) {
            isValid = false;
            editTxtPassword.setError("Enter your password");
        } else if (Password.length() < 4) {
            isValid = false;
            editTxtPassword.setError("at least 6 character");
        }

        if (Phone.isEmpty()) {
            isValid = false;
            editTxtPhone.setError("Enter your phone number");
        } else if (Phone.length() < 10) {
            isValid = false;
            editTxtPhone.setError("The phone number is too short");
        } else if (Phone.length() > 10) {
            isValid = false;
            editTxtPhone.setError("The phone number is too long");
        }

         if(latLng == null){
            isValid = false;
            Toast.makeText(mContext, "Select your location", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    public void OnSelectLocation(View view){

        Intent i = new Intent(RegisterActivity.this, Location.class);
        startActivityForResult(i, LAUNCH_Map_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_Map_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {

                double lat = data.getDoubleExtra("lat",0);
                double lng = data.getDoubleExtra("lng",0);
                latLng = new GeoPoint(lat,lng);

                ((TextView)findViewById(R.id.txtLatLng))
                        .setText("lat "+String.format(Locale.ENGLISH,"%.7f",lat)
                                +", lng "+String.format(Locale.ENGLISH,"%.7f",lng));
            }
        }
    }
}