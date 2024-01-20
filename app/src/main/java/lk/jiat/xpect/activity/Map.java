package lk.jiat.xpect.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lk.jiat.xpect.R;

public class Map extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private GoogleMap map;

    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
//        LatLng latLng = new LatLng(6.253487, 80.048154);
//        map.addMarker(new MarkerOptions().position(latLng).title("My location"));
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

        if (checkPermission()) {
            map.setMyLocationEnabled(true);
            getLastLocation();
        } else {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );

        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Toast.makeText(Map.this, "Select location"+latLng.latitude+","+latLng.longitude, Toast.LENGTH_SHORT).show();

                // Clear previous markers
                map.clear();

                // Add a marker to the selected location
                map.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));

                // Move the camera to the selected location
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                Geocoder geocoder = new Geocoder(Map.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            latLng.latitude, latLng.longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        // Get the location name (if available)
                        String locationName = address.getLocality();
                        if (locationName == null || locationName.isEmpty()) {
                            locationName = address.getSubLocality();
                        }

                        // Show location details in a Toast
                        String displayText = "Selected location: " + locationName;
                        Toast.makeText(Map.this, displayText, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("selected_location", latLng);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

    }

    private boolean checkPermission() {
        boolean permission = false;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            permission = true;
        }
        return permission;
    }

    private void getLastLocation() {
        if (checkPermission()){
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                        if (location != null){
                            currentLocation = location;
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                            map.addMarker(new MarkerOptions().position(latLng).title("My Location"));
//                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,50));

                            Geocoder  geocoder = new Geocoder(Map.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(
                                        latLng.latitude, latLng.longitude, 1);

                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);

                                    // Get the location name (if available)
                                    String locationName = address.getLocality();
                                    if (locationName == null || locationName.isEmpty()) {
                                        locationName = address.getSubLocality();
                                    }

                                    // Show location details in a Toast
                                    String displayText = "Current location: " + locationName;
                                    Toast.makeText(Map.this, displayText, Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // Add marker and move camera (same as before)
                            map.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Snackbar.make(findViewById(R.id.mapContainer), "Location permission denied", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }
}