package lk.jiat.xpect.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.activity.Map;
import lk.jiat.xpect.dto.EventDTO;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ProfileFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();
    private int numberOfMyEvents;
    private static final int REQUEST_LOCATION = 10;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private ArrayList<EventDTO> events;
    private RecyclerView.Adapter adapter;
    private String imagePath;
    private ImageView userImageView;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private static final int REQUEST_CODE_MAP = 101;
    private Button btnGoToEditProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userImageView = view.findViewById(R.id.viewUploadedEventImage);
        btnGoToEditProfile = view.findViewById(R.id.btnGoToEditProfile);
        btnGoToEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
               transaction.replace(R.id.container,new EditProfileFragment());
               transaction.addToBackStack(null);
               transaction.commit();
            }
        });
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        Button viewById = view.findViewById(R.id.btnUpdateLocationprofileFragment);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "adadadada");
                Intent intent = new Intent(getActivity(), Map.class);
                startActivityForResult(intent, REQUEST_CODE_MAP);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);
        Call<ArrayList<EventDTO>> eventCall = xpectWebService.getEventByUserId(currentUserId);
        eventCall.enqueue(new Callback<ArrayList<EventDTO>>() {
            @Override
            public void onResponse(Call<ArrayList<EventDTO>> call, Response<ArrayList<EventDTO>> response) {
                if (response.isSuccessful()) {
                    events = response.body();
                    if (events != null) {
                        for (EventDTO eventDTO : events) {
                            //retrive from firebase
                            firestore.collection("eventEntity").whereEqualTo("eventUniqueId",eventDTO.getEventUniqueId())
                                            .get()
                                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                                        if (!queryDocumentSnapshots.isEmpty()){
                                                           DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                                                            String imageUrl = snapshot.getString("imagePath");
                                                            String name = snapshot.getString("name");
                                                            eventDTO.setEventName(name);
                                                            eventDTO.setImageUrl(imageUrl);
                                                            adapter.notifyDataSetChanged();
                                                            Log.i(TAG, imageUrl);
                                                        }
                                                    });


                            //retrive from firebase
                        }

                        adapter = new RecyclerView.Adapter() {
                            @NonNull
                            @Override
                            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                LayoutInflater inflater = LayoutInflater.from(getActivity());
                                View eventView = inflater.inflate(R.layout.my_events_single_view_layout, parent, false);
                                return new MEVH(eventView);
                            }

                            @Override
                            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                                MEVH mevh = (MEVH) holder;
                                mevh.myEventName.setText(events.get(position).getEventName());
                                Picasso.get().load(events.get(position).getImageUrl()).into(mevh.myEventImage);

                                holder.itemView.setOnClickListener(view -> {
                                    EditMyEventFragment editMyEventFragment = new EditMyEventFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("eventUniqueId", events.get(position).getEventUniqueId());
                                    editMyEventFragment.setArguments(bundle);
                                    FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.replace(R.id.container, editMyEventFragment);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                });
                            }

                            @Override
                            public int getItemCount() {
                                numberOfMyEvents = events.size();
                                TextView numberOfMyEventsView = view.findViewById(R.id.numberOfMyEventsView);
                                numberOfMyEventsView.setText(String.valueOf(numberOfMyEvents));
                                return numberOfMyEvents;
                            }
                        };
                        RecyclerView recyclerView = view.findViewById(R.id.myEventLoadRecyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerView.setAdapter(adapter);

                    }
                }
                firestore.collection("userImages").whereEqualTo("userId", currentUserId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                //
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                                imagePath = document.getString("imagePath");

                                if (imagePath != null && !imagePath.isEmpty()) {

                                    Picasso.get().load(imagePath).into(userImageView);
                                } else {
//                                        Log.i(TAG,"image not set");
                                }
                                //
                            }
                        });
                firestore.collection("favoriteEvents").whereEqualTo("userId",currentUserId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                           if (!queryDocumentSnapshots.isEmpty()){
                               int size = queryDocumentSnapshots.size();
                               TextView numberOfFavoriteEventView = view.findViewById(R.id.numberOfInterestedEventView);
                          numberOfFavoriteEventView.setText(String.valueOf(size));
                           }
                        });
            }

            @Override
            public void onFailure(Call<ArrayList<EventDTO>> call, Throwable t) {

            }
        });



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                LatLng selectedLocation = data.getParcelableExtra("selected_location");
                // Retrieve the selected location data from the Map Activity
//                double latitude = data.getDoubleExtra(selectedLocation.latitude, 0.0);
//                double longitude = data.getDoubleExtra("longitude", 0.0);
                latitude = selectedLocation.latitude;
                longitude = selectedLocation.longitude;
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);

                        // Get the city name or place name from the address
                        String cityName = address.getLocality();
                        String placeName = address.getFeatureName();

                        firestore.collection("UserCurrentLocation")
                                .whereEqualTo("userId", currentUserId)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        boolean found = false;
                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                            documentSnapshot.getReference().update("latitude", latitude);
                                            documentSnapshot.getReference().update("longitude", longitude);
                                            found = true;
                                        }
                                        if (!found){
                                            HashMap<String, Object> userLocation = new HashMap<>();
                                            userLocation.put("userId", currentUserId);
                                            userLocation.put("latitude", latitude);
                                            userLocation.put("longitude", longitude);
                                            firestore.collection("UserCurrentLocation")
                                                    .add(userLocation)
                                                    .addOnSuccessListener(documentReference -> {
                                                        Log.i(TAG, "Doucement added");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Error adding document", e);
                                                    });
                                        }

                                    } else {
                                        Log.e(TAG, "Error getting docuemtns");
                                    }
                                });
                        Toast.makeText(requireContext(), "Place: " + placeName + ", City: " + cityName, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//    private void CheckLocationPermission() {
//        Log.i(TAG,"call check permission");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Log.i(TAG,"call check permission1");
//            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
//        }
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Log.i(TAG,"call check permission2");
//            OnGPS();
//        } else {
//            Log.i(TAG,"call check permission3");
//            getCurrentLocation();
//        }
//    }

//    private void OnGPS() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setMessage("Enable GPS").setCancelable(false)
//                .setPositiveButton(
//                        "Yes", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                            }
//                        }
//                ).setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//        final AlertDialog dialog = builder.create();
//        dialog.show();
//    }

//    private void getCurrentLocation() {
//        Log.i(TAG,"call check permission4");
//        if (ActivityCompat.checkSelfPermission(
//
//                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG,"call check permission5");
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//        } else {
//            Log.i(TAG,"call check permission6");
//            Location GpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (GpsLocation != null) {
//                double latitude = GpsLocation.getLatitude();
//                double longitude = GpsLocation.getLongitude();
//                Log.i(TAG,"call check permission7");
//                userLatitude = String.valueOf(latitude);
//                userLongitude = String.valueOf(longitude);
//
//                String location = userLatitude + userLongitude;
//                Log.i(TAG, location);
//
//
//                HashMap<String, Object> userLocation = new HashMap<>();
//                userLocation.put("userId", currentUserId);
//                userLocation.put("latitude", userLatitude);
//                userLocation.put("longitude", userLongitude);
//
//                Log.i(TAG,"call check permission8");
//                firestore.collection("userLocations")
//                        .add(userLocation)
//                        .addOnSuccessListener(documentReference -> {
//                            Log.d(TAG, "Event location added with ID: " + documentReference.getId());
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e(TAG, "Error adding location", e);
//                        });
//            }else {
//                Log.i(TAG,"yoo yoo");
//            }
//        }
//    }

    class MEVH extends RecyclerView.ViewHolder {
        TextView myEventName;
        ImageView myEventImage;

        public MEVH(@NonNull View itemView) {
            super(itemView);
            myEventName = itemView.findViewById(R.id.MyEventName);
            myEventImage = itemView.findViewById(R.id.myEventImage);
        }
    }
}