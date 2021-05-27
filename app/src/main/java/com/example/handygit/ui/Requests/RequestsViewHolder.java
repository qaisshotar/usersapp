package com.example.handygit.ui.Requests;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.handygit.Format;
import com.example.handygit.R;
import com.example.handygit.Request;
import com.example.handygit.Worker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsViewHolder extends RecyclerView.ViewHolder{

    private TextView txtWorkerName,txtStatus,txtDate;
    private CircleImageView icon;
    private Context mContext;
    private FirebaseFirestore db;
    private ImageButton btnPhone;
    private Worker worker;
    public RequestsViewHolder(@NonNull View itemView) {
        super(itemView);

        txtWorkerName =  itemView.findViewById(R.id.txtWorkerName);
        txtStatus =  itemView.findViewById(R.id.txtStatus);
        txtDate =  itemView.findViewById(R.id.txtDate);
        icon =  itemView.findViewById(R.id.icon);
        btnPhone = itemView.findViewById(R.id.btnPhone);
    }

    public void onBind(final Context context, FirebaseFirestore db, Request request) {

        this.mContext=context;
        this.db=db;
        db.document("Workers/"+request.getWID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot Worker) {

                        if(Worker.exists()){

                            worker = Worker.toObject(Worker.class);

                            txtWorkerName.setText(worker.getName().trim());

                            SetImage(worker.getJopTitle());
                        }
                    }
                });

        txtDate.setText(Format.DateFormat(request.getRequestdate().getTime()));

        txtStatus.setText(request.getStatus().trim());

        switch (request.getStatus()){
            case "padding":
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.orange));
                break;
            case "Accepted":
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
                break;
            case "Rejected":
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
        }

        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (worker !=null)
                onClickCall();
            }
        });
    }

    private void SetImage(String JopTitle) {

        db.document("Jobs/"+JopTitle)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot Job) {

                        if(Job.exists()){
                            // Loading Image
                            Glide.with(mContext)
                                    .load(Job.getData().get("Url").toString())
                                    .fitCenter()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(icon);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.i("ERRORS",e.getMessage());
            }
        });



    }

    void onClickCall() {

        if (worker.getPhoneNumber() != null && worker.getPhoneNumber().length() != 0) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Service");
            alertDialog.setMessage("Do you want call phone number ?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mContext.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", worker.getPhoneNumber(), null)));
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show();
        } else
            Toast.makeText(mContext, "He does not have the phone number.", Toast.LENGTH_SHORT).show();
    }
}
