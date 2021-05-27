package com.example.handygit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class SplashScreen extends AppCompatActivity {


    /**
     * FireStore
     */
    private FirebaseFirestore DB;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        init();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                CheckRegistration();

            }
        }, 2000); // 2000

    }

    void init() {


        DB = FirebaseFirestore.getInstance();


        mAuth = FirebaseAuth.getInstance();

    }

    private void CheckRegistration() {

        if (mAuth.getCurrentUser() != null) {

            DB.document("Users/" + mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot Doc) {

                            if (Doc.exists()) {

                                SplashScreen.this.startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                SplashScreen.this.finish();

                            }
                            else
                                GoToLoginActivity();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {

                    GoToLoginActivity();
                }
            });
        } else {

            GoToLoginActivity();

        }
    }

    void GoToLoginActivity(){
        SplashScreen.this.startActivity(new Intent(SplashScreen.this, LoginActivity.class));
        SplashScreen.this.finish();
    }

}