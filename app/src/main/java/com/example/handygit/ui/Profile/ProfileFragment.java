package com.example.handygit.ui.Profile;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.handygit.CustomRadioBtn.CustomPresetRadioGroup;
import com.example.handygit.CustomRadioBtn.ForCategories.CustomRadioBtnCategories;
import com.example.handygit.R;
import com.example.handygit.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private FirebaseFirestore db;
    private String FullName;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);


        init(root);

        getUserInfo();

        return root;
    }

    void init(View root){

        root.findViewById(R.id.btnApply).setOnClickListener(this::onClick);
        root.findViewById(R.id.LayoutFullName).setOnClickListener(this::onClick);

        /** Connection With FireStore */
        db = FirebaseFirestore.getInstance();
    }

    void getUserInfo(){

        db.document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if(documentSnapshot.exists()){
                            User user = documentSnapshot.toObject(User.class);
                            ((TextView)getActivity().findViewById(R.id.txtFullName)).setText(user.getFullname());
                            ((TextView)getActivity().findViewById(R.id.txtEmail)).setText(user.getEmail());
                            ((TextView)getActivity().findViewById(R.id.txtPhone)).setText(user.getPhone());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.i("ERRORS",e.getMessage());
                Toast.makeText(getContext(), "User data was not fetched", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        
        switch (v.getId()){
            case R.id.btnApply:
                OnApply();
                break;
            case R.id.LayoutFullName:
                OpenChangeFullNameDialog();
        }
    }
    
    private void OnApply(){

        if(FullName ==null){
            Toast.makeText(getContext(), "I never changed the data", Toast.LENGTH_LONG).show();
            return;
        }
        FullName = ((TextView)getActivity().findViewById(R.id.txtFullName)).getText().toString().trim();

        Map<String,Object> map = new HashMap<>();
        map.put("fullname",FullName);

        db.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                FullName = null;
                Toast.makeText(getContext(), "The data has been successfully updated", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.i("ERRORS",e.getMessage());
                Toast.makeText(getContext(), "The data was not successfully updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void OpenChangeFullNameDialog(){

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_data, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        final AlertDialog Dialog = builder.create();
        Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText editTxtFullName = dialogView.findViewById(R.id.editTxtFullName);

        dialogView.findViewById(R.id.btnApply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FullName = editTxtFullName.getText().toString().trim();

                if (FullName.isEmpty()) {
                    editTxtFullName.setError("Enter the full name");
                    return;
                }

                ((TextView)getActivity().findViewById(R.id.txtFullName)).setText(FullName);

                Dialog.dismiss();
            }
        });
        Dialog.show();
    }

}