package lk.jiat.xpect.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.dto.UserDTO;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class EditProfileFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private int userDbId;
    private String currentUserId;
    private String displayName;
    private String currentUserEmail;
    private String userName;
    private String userBIO;
    private String userEmail;
    private String photoUrl;
    private Uri imageUri;
    private String imagePath;
    private Bitmap bitmap;
    private StorageReference mStorage;
    private FirebaseStorage storage;
    private ImageView userImageView;
    private EditText editTextUserName;
    private EditText editTextUserBIO;
    private EditText editTextUserEmail;
    private Button btnSaveUserDetails;
    private MaterialCardView materialCardViewSelectUserImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorage = storage.getReference();
        currentUser = firebaseAuth.getCurrentUser();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        displayName = currentUser.getDisplayName();
        currentUserEmail = currentUser.getEmail();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, currentUserId);
        editTextUserName = view.findViewById(R.id.editTextUserName);
        editTextUserBIO = view.findViewById(R.id.editTextUserBIO);
        editTextUserEmail = view.findViewById(R.id.editTextUserEmail);
        materialCardViewSelectUserImage = view.findViewById(R.id.materialCardViewSelectUserImage);
        userImageView = view.findViewById(R.id.viewUploadedUserImage);
        btnSaveUserDetails = view.findViewById(R.id.btnSaveUserDetails);

        userEmail = currentUser.getEmail().toString();
        editTextUserEmail.setText(userEmail);
        materialCardViewSelectUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermission();
            }
        });


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);
        Call<UserDTO> calluserByUniqueId = xpectWebService.getUserByUniqueId(currentUserId);
        calluserByUniqueId.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    UserDTO userDTO = response.body();
                    if (userDTO != null) {
                        userName = userDTO.getName().toString();
                        userBIO = userDTO.getBio();
                        userDbId = userDTO.getId();
                        Log.i(TAG, userName);
                        editTextUserName.setText(userName);
                        editTextUserBIO.setText(userBIO);
                        btnSaveUserDetails.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UserDTO userDTO = new UserDTO();
                                userDTO.setId(userDbId);
                                userDTO.setName(editTextUserName.getText().toString());
                                userDTO.setBio(editTextUserBIO.getText().toString());
                                userDTO.setFirebaseUserId(currentUserId);
                                Call<Void> voidCall = xpectWebService.updateUser(userDTO);
                                voidCall.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {

                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {

                                    }
                                });
                                if (imageUri != null) {
                                    Log.e(TAG, "ImageUri");
                                    uploadImage();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {

            }
        });
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

    }

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

    private void pickerImageFromGallery() {
        Log.i(TAG, "pickerImageFromGallery");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//  set image launcher
        launcher.launch(intent);
    }

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
                        Log.e(TAG, imageUri.toString());
                        userImageView.setImageBitmap(bitmap);
                    }
//set image to image view
                    Toast.makeText(getContext(), "Profile Details successfully updated", Toast.LENGTH_SHORT).show();
                }
            }
    );

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
        eventImage.put("userId", currentUserId);
        eventImage.put("imagePath", photoUrl);
        firestore.collection("userImages")
                .add(eventImage)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "user img added id" + documentReference.getId());

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding image", e);
                });

    }
}