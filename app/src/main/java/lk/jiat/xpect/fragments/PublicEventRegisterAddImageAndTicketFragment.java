package lk.jiat.xpect.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.activity.Map;
import lk.jiat.xpect.entity.TicketType;


public class PublicEventRegisterAddImageAndTicketFragment extends Fragment {
    private String cityName;
    private String placeName;
    private double latitude;
    private double longitude;
    private MaterialCardView selectImage;
    private ImageView eventImageView;
    private Uri imageUri;
    private Bitmap bitmap;
    private static final int REQUEST_CODE_MAP = 101;
    //
    public static final String TAG = MainActivity.class.getName();
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference mStorage;
    private String photoUrl;
    private TicketTypeAdapter adapter;
    private String eventUniqueId;
    private String eventName;
    private List<TicketType> ticketTypesList = new ArrayList<>();
    //

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            eventUniqueId = bundle.getString("uniqueId");
            eventName = bundle.getString("eventName");
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_public_event_register_add_image_and_ticket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
// get firebase instance
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorage = storage.getReference();
// get firebase instance
//select image code
        selectImage = view.findViewById(R.id.selectEventImage);
        eventImageView = view.findViewById(R.id.viewUploadedEventImage);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Key pressed");
//  method to check permission
                checkStoragePermission();
//  method to check permission
            }
        });
//select image code
//////////////////////////////////
//Open dialog to Add ticket types
        view.findViewById(R.id.btnAddTicketTypeToViewDialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTicketTypeDialog();
            }
        });
//Open dialog to Add ticket types

//set ticketType adapter to adapter
        adapter = new TicketTypeAdapter(ticketTypesList);
//set ticketType adapter to adapter
//get recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewForTicketTypeViews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
//get recycler view
//save to firestore
        view.findViewById(R.id.btnSaveForEventImageAndTicketTypes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFirestore();

            }
        });
//save to firestore
        view.findViewById(R.id.btnAddLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Map.class);
                startActivityForResult(intent, REQUEST_CODE_MAP);
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
                        cityName = address.getLocality();
                        placeName = address.getFeatureName();

                        // Use the cityName or placeName as needed
                        // For example, display in a toast
                        Toast.makeText(requireContext(), "Place: " + placeName + ", City: " + cityName, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveToFirestore() {
        WriteBatch batch = firestore.batch();
        CollectionReference ticketTypesCollection = firestore.collection("ticketTypes");
        for (TicketType ticketType : ticketTypesList) {
            ticketType.setEventUniqueId(eventUniqueId);
            DocumentReference docRef = ticketTypesCollection.document();
            batch.set(docRef, ticketType);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Batch write successful");
                    uploadImage();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error batch writing documents", e);
                });

    }

    //Method to upload image
    private void uploadImage() {
        Log.i(TAG, "call ui");
        if (imageUri != null) {
            final StorageReference myRef = mStorage.child("photo/" + imageUri.getLastPathSegment());
            myRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//get download url to store in string
                    myRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (uri != null) {
                                Log.i(TAG, "Image upload success");
                                photoUrl = uri.toString();
                                saveImageToFirebase();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "image cupload fail" + e);
                        }
                    });
//get download url to store in string
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "Image upload fail");
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.i(TAG, "image uri null");
        }
    }

    //Method to upload image
//method to save image to firebase
    private void saveImageToFirebase() {
        HashMap<String, Object> eventImage = new HashMap<>();
        eventImage.put("eventId", eventUniqueId);
        eventImage.put("imagePath", photoUrl);
        firestore.collection("eventImages")
                .add(eventImage)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Event img added id" + documentReference.getId());
                    saveLocationToFirebase(latitude, longitude);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding image", e);
                });

    }

    private void saveLocationToFirebase(double latitude, double longitude) {
        HashMap<String, Object> eventLocation = new HashMap<>();
        eventLocation.put("eventId", eventUniqueId);
        eventLocation.put("latitude", latitude);
        eventLocation.put("longitude", longitude);
        eventLocation.put("city", cityName);
        eventLocation.put("place", placeName);

        firestore.collection("eventLocations")
                .add(eventLocation)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Event location added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding location", e);
                });
    }


    //method to save image to firebase
    //method to open dialog to add ticket types
    private void showAddTicketTypeDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.add_ticket_type_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        EditText etTypeName = dialogView.findViewById(R.id.editTextTicketTypeName);
        EditText etPrice = dialogView.findViewById(R.id.editTextTicketPrice);
        EditText etQuantity = dialogView.findViewById(R.id.editTextTicketTypeQTY);
        Button btnAdd = dialogView.findViewById(R.id.btnAddTicketTypeToList);

        AlertDialog alertDialog = dialogBuilder.create();

        // Add button click listener
        btnAdd.setOnClickListener(view -> {
            String typeName = etTypeName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();

            if (!typeName.isEmpty() && !priceStr.isEmpty() && !quantityStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                int quantity = Integer.parseInt(quantityStr);

                // Create a new TicketType object
                TicketType newTicketType = new TicketType();
                newTicketType.setTypeName(typeName);
                newTicketType.setPrice(price);
                newTicketType.setQuantity(quantity);

                // Add newTicketType to your list
                ticketTypesList.add(newTicketType);


//                adapter.notifyItemInserted(ticketTypesList.size()-1);

//                for (TicketType s : ticketTypesList) {
//
//                    Log.i(TAG, s.getTypeName().toString());
//                }

                // Dismiss the dialog
                alertDialog.dismiss();
            } else {
                // Handle case when fields are empty
                // Show error message or prompt to fill all fields
            }
        });

        alertDialog.show();
    }
//method to open dialog to add ticket types

    // method to check permission to access gallery
    private void checkStoragePermission() {
        Log.i(TAG, "checkStoragePermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "wadie");
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "wadie1");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Log.i(TAG, "wadie2");
//method to pick image from gallery
                pickerImageFromGallery();
//method to pick image from gallery
            }
        } else {
            Log.i(TAG, "wadie3 ");
//method to pick image from gallery
            pickerImageFromGallery();
//method to pick image from gallery
        }
    }
// method to check permission to access gallery

    //method - picker image from gallery
    private void pickerImageFromGallery() {
        Log.i(TAG, "pickerImageFromGallery");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//  set image launcher
        launcher.launch(intent);
    }
//method - picker image from gallery


    //launcher for pick image form gallery
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        Log.i(TAG, "launcher");
                        imageUri = data.getData();
//convert image into Bitmap
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(
                                    getActivity().getContentResolver(),
                                    imageUri
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
//convert image into Bitmap
                    }
//set image to image view
                    if (imageUri != null) {
                        eventImageView.setImageBitmap(bitmap);
                    }
//set image to image view
                }
            }
    );
//launcher for pick image form gallery
}

//adapter for recyler view
class TPVH extends RecyclerView.ViewHolder {

    TextView typeName, price, quantity;

    public TPVH(@NonNull View itemView) {
        super(itemView);
        typeName = itemView.findViewById(R.id.textViewTicketTypeName);
        price = itemView.findViewById(R.id.textViewTicketTypePrice);
        quantity = itemView.findViewById(R.id.textViewTicketTypeQuantity);
    }
}
//adapter for recyler view

//ticket type adapter class
class TicketTypeAdapter extends RecyclerView.Adapter<TPVH> {
    private List<TicketType> ticketTypesList;

    public TicketTypeAdapter(List<TicketType> ticketTypesList) {
        this.ticketTypesList = ticketTypesList;
    }

    @NonNull
    @Override
    public TPVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View ticketTypeView = inflater.inflate(R.layout.single_ticket_type_view_layout, parent, false);
        return new TPVH(ticketTypeView);
    }

    @Override
    public void onBindViewHolder(@NonNull TPVH holder, int position) {
        holder.typeName.setText(ticketTypesList.get(position).getTypeName());
        holder.price.setText(String.valueOf(ticketTypesList.get(position).getPrice()));
        holder.quantity.setText(String.valueOf(ticketTypesList.get(position).getQuantity()));
    }

    @Override
    public int getItemCount() {
        return ticketTypesList.size();
    }

}
//ticket type adapter class