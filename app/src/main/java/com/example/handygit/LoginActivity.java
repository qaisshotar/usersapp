package com.example.handygit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import static com.example.handygit.RegisterActivity.emailPattern;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTxtEmail, editTxtPassword;
    private String Email, Password;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
    }

    private void init(){

        editTxtEmail = findViewById(R.id.editTxtEmail);
        editTxtPassword = findViewById(R.id.editTxtPassword);


        db = FirebaseFirestore.getInstance();


        mAuth = FirebaseAuth.getInstance();
    }

    public void OnLogin(View view){

        if (!InputIsValid()) return;

        mAuth.signInWithEmailAndPassword(Email,Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            db.document("Users/" + mAuth.getCurrentUser().getUid())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot Doc) {

                                            if (Doc.exists()) {

                                                User user = Doc.toObject(User.class);

                                                if (user.getType().equals("User")) {
                                                    Global.UserID = Doc.getId();
                                                    Global.Username = user.getFullname();
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    finish();
                                                }else
                                                    Toast.makeText(getApplicationContext(), "You do not have permission", Toast.LENGTH_LONG).show();
                                            }else
                                                Toast.makeText(getApplicationContext(), "You do not have permission", Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {

                                    Log.i("ERRORS",e.getMessage());
                                }
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), "Make sure the email or password is correct", Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.i("ERRORS",e.getMessage());
                    }
                });
    }

    public void OnRegisterNow(View view){

        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }

    boolean InputIsValid() {

        Email = editTxtEmail.getText().toString().trim();
        Password = editTxtPassword.getText().toString();

        boolean isValid = true;

        if (!(Email.matches(emailPattern) && Email.length() > 0) ) {
            isValid = false;
            editTxtEmail.setError("invalid email");
        }

        if (Password.isEmpty()) {
            isValid = false;
            editTxtPassword.setError("Enter your password");
        }

        return isValid;
    }

}