package com.example.handygit.ui.Map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.handygit.CustomRadioBtn.CustomPresetRadioGroup;
import com.example.handygit.CustomRadioBtn.ForCategories.CustomRadioBtnCategories;
import com.example.handygit.Notification;
import com.example.handygit.R;
import com.example.handygit.Request;
import com.example.handygit.Service.Notify;
import com.example.handygit.Worker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.example.handygit.Global.Username;

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {


    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;
    private double latitude, longitude;
    private int ProximityRadius = 10000;
    private Context mContext;
    private FirebaseFirestore db;
    private List<Worker> workers;
    private Drawable icon_CurrentService;
    private CustomPresetRadioGroup BlockChips;
    private FirebaseAuth mAuth;
    private HashMap<String, String> ServicesIcon;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        BlockChips = root.findViewById(R.id.FlowLayout_category);

        init();

        return root;
    }


    void init() {

        mContext = getContext();

        db = FirebaseFirestore.getInstance();

        /**Connection With FirebaseAuth .*/
        mAuth = FirebaseAuth.getInstance();

        ServicesIcon = new HashMap<>();

        workers = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        FragmentManager fm = getActivity().getSupportFragmentManager();/// getChildFragmentManager();
        SupportMapFragment supportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, supportMapFragment).commit();
        }
        supportMapFragment.getMapAsync(this);

        // GET ALL WORKER FROM Firebase . . .
        getWorkers();

        // GET ALL CATEGORY FROM Firebase . . .
        getService();
    }

    private void DisplayWorkers(String Filter) {


        mMap.clear();

        for (int i = 0; i < workers.size(); i++) {

            if (!Filter.trim().equals("All") && !workers.get(i).getJopTitle().trim().equals(Filter.trim()))
                continue;


            int finalI = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    URL url;
                    Bitmap bmp = null;
                    try {
                        Log.i("zozo",ServicesIcon.get(workers.get(finalI).getJopTitle()));
                        url = new URL(ServicesIcon.get(workers.get(finalI).getJopTitle()));
                        bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Bitmap finalBmp = Bitmap.createScaledBitmap(bmp, 75, 75, false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            MarkerOptions markerOptions = new MarkerOptions();
                            Worker worker = workers.get(finalI);
                            LatLng latLng = new LatLng(worker.getLatLng().getLatitude(), worker.getLatLng().getLongitude());
                            markerOptions.position(latLng);
                            markerOptions.title(worker.getName() + " : " + worker.getJopTitle());
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalBmp));
                            mMap.addMarker(markerOptions).setTag(worker);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                        }
                    });
                }
            });
            thread.start();

        }


    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googleURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location=" + latitude + "," + longitude);
        googleURL.append("&radius=" + ProximityRadius);
        googleURL.append("&type=" + nearbyPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key=" + "Your API key");

        Log.d("GoogleMapsActivity", "url = " + googleURL.toString());

        return googleURL.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this::onMarkerClick);

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public boolean checkUserLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (googleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(mContext, "Permission Denied...", Toast.LENGTH_SHORT).show();
                }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        lastlocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(12));

        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Worker worker = (Worker) marker.getTag();

        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_request_worker, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(dialogView);
        final AlertDialog Dialog = builder.create();
        Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView txtJobTitle = dialogView.findViewById(R.id.txtJobTitle);
        TextView txtWorkerName = dialogView.findViewById(R.id.txtWorkerName);
        TextView txtDesc = dialogView.findViewById(R.id.txtDesc);
        TextView txtPhoneNumber = dialogView.findViewById(R.id.txtPhoneNumber);
        ImageView icon = dialogView.findViewById(R.id.icon);
        icon.setImageDrawable(icon_CurrentService);

        txtJobTitle.setText(worker.getJopTitle());
        txtWorkerName.setText(worker.getName());
        txtDesc.setText(worker.getJobDescription());
        txtPhoneNumber.setText(worker.getPhoneNumber());


        dialogView.findViewById(R.id.btn_SendRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Request request = new Request(mAuth.getCurrentUser().getUid(), worker.getUID(), new Date(), "padding");
                db.collection("Requests")
                        .add(request)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                Toast.makeText(mContext, "The request has been sent successfully", Toast.LENGTH_SHORT).show();
                                Dialog.dismiss();

                                OnSendNotification(worker.getUID());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(mContext, "The request was not sent successfully", Toast.LENGTH_SHORT).show();
                                Dialog.dismiss();
                            }
                        });
            }
        });

        Dialog.show();

        return false;
    }

    private void getWorkers() {

        db.collection("Workers")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot Worker : queryDocumentSnapshots) {
                            Worker worker = Worker.toObject(Worker.class);
                            worker.setUID(Worker.getId());

                            workers.add(worker);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.i("ERRORS", e.getMessage());
                    }
                });
    }

    public void getService() {

        db.collection("Jobs")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {


                        BlockChips.setOnCheckedChangeListener(new CustomPresetRadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(View radioGroup, View radioButton, boolean isChecked, int checkedId) {

                                if (isChecked) {
                                    icon_CurrentService = ((CustomRadioBtnCategories) radioButton).getImage();
                                    DisplayWorkers(((CustomRadioBtnCategories) radioButton).getCategory());
                                }


                            }
                        });

                        final CustomRadioBtnCategories AllChip = new CustomRadioBtnCategories(mContext);
                        AllChip.setImage("https://firebasestorage.googleapis.com/v0/b/handymanfinal.appspot.com/o/image%2Fall.png?alt=media&token=86df266b-6ee1-46f3-9a78-56ff5167f719");
                        AllChip.setCategory(" All ");
                        BlockChips.addView(AllChip);

                        for (QueryDocumentSnapshot category : queryDocumentSnapshots) {

                            final CustomRadioBtnCategories CustomChip = new CustomRadioBtnCategories(mContext);
                            String Url = category.getData().get("Url").toString().trim();
                            CustomChip.setImage(Url);
                            String name = category.getData().get("Name").toString().trim();
                            CustomChip.setCategory(name);
                            ServicesIcon.put(name, Url);

                            BlockChips.addView(CustomChip);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.i("ERRORS", e.getMessage());
                    }
                });
    }


    @Override
    public void onResume() {
        super.onResume();
        if(mMap != null){ //prevent crashing if the map doesn't exist yet (eg. on starting activity)
            mMap.clear();

            // add markers from database to the map
        }
    }

    void OnSendNotification(String WorkerID) {


        db.collection("Token").document(WorkerID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.getResult().exists()) {

                    Notification notification = new Notification(task.getResult().get("Token").toString(), Username + " sent you a maintenance request");
                    notification.execute();

                }
            }
        });

    }
}