package lk.jiat.xpect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import lk.jiat.xpect.activity.LoginActivity;
import lk.jiat.xpect.activity.SplashActivity;
import lk.jiat.xpect.activity.VerifyActivity;
import lk.jiat.xpect.entity.User;
import lk.jiat.xpect.fragments.AddEventTypeFragment;
import lk.jiat.xpect.fragments.FavoriteEventFragment;
import lk.jiat.xpect.fragments.HomeFragment;

import lk.jiat.xpect.fragments.NotificationFragment;
import lk.jiat.xpect.fragments.ProfileFragment;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {
    public static final String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    //    side nav
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;


    private String currentUserDisplayName;
    private String currentUserEmail;
    private String currentUserid;
    private String currentUserImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (getIntent().getBooleanExtra("openProfileFragment", false)) {
            // Open ProfileFragment
            loadFragment(new ProfileFragment());
        }

        setContentView(R.layout.activity_main);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            getFCMToken();
            currentUserDisplayName = currentUser.getDisplayName();
            currentUserEmail = currentUser.getEmail();
            currentUserid = currentUser.getUid();
        }


        firestore.collection("user").whereEqualTo("userId", currentUserid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        currentUserImagePath = documentSnapshot.getString("imagePath");
                    }
                }).addOnFailureListener(e -> {

                });


        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        loadFragment(new HomeFragment());

        //
        EditText textInputSearch = findViewById(R.id.textInputSearch);
        ImageView imageViewSearch = findViewById(R.id.imageViewSearch);

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textInputSearch.getVisibility() == View.VISIBLE) {
                    // EditText is visible, hide it with animation
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textInputSearch, "alpha", 1f, 0f);
                    fadeOut.setDuration(500);
                    fadeOut.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            textInputSearch.setVisibility(View.GONE);
                        }
                    });
                    fadeOut.start();
                } else {
                    // EditText is hidden, show it with animation and focus
                    textInputSearch.setVisibility(View.VISIBLE);
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textInputSearch, "alpha", 0f, 1f);
                    fadeIn.setDuration(500);
                    fadeIn.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            textInputSearch.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(textInputSearch, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                    fadeIn.start();
                }
            }
        });


        //


        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.open();
            try {
                ImageView imageView = findViewById(R.id.userImageSlideView);
                TextView name = findViewById(R.id.userNameSlideView);
                TextView email = findViewById(R.id.userEmailSlideView);
                NavigationView navigationView = findViewById(R.id.navigationView);
                Menu navMenu = navigationView.getMenu();
// Find items within the group
                MenuItem sideNavHome = navMenu.findItem(R.id.sideNavHome);
                MenuItem sideNavProfile = navMenu.findItem(R.id.sideNavProfile);
                MenuItem sideNavFavourite = navMenu.findItem(R.id.sideNavFavourite);
                MenuItem sideNavLogout = navMenu.findItem(R.id.sideNavLogout);
                MenuItem sideNavLogin = navMenu.findItem(R.id.sideNavLogin);
                if (firebaseAuth.getCurrentUser() != null) {
                    Log.i(TAG, "onClick: Log In");
                    sideNavLogin.setVisible(false);
                    sideNavHome.setVisible(true);
                    sideNavProfile.setVisible(true);
                    sideNavFavourite.setVisible(true);
                    sideNavLogout.setVisible(true);
                } else {
                    // User is not logged in
                    Log.i(TAG, "onClick: Log out");
                    sideNavHome.setVisible(true);
                    sideNavProfile.setVisible(false);
                    sideNavFavourite.setVisible(false);
                    sideNavLogout.setVisible(false);
                    sideNavLogin.setVisible(true);
                }
                Picasso.get().load(currentUserImagePath).into(imageView);
                name.setText(currentUserDisplayName);
                email.setText(currentUserEmail);
            } catch (Exception e) {

            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);


//        search

        EditText searchText = findViewById(R.id.textInputSearch);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://192.168.1.112:8080/api/v1/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                XpectWebService xpectWebService = retrofit.create(XpectWebService.class);




            }
        });
//        search

    }

    /////////////////////////////////////////////////////////////
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        if (item.getItemId() == R.id.sideNavHome) {

            loadFragment(new HomeFragment());
        } else if (item.getItemId() == R.id.bottomNavHome) {
            loadFragment(new HomeFragment());
        } else if (item.getItemId() == R.id.bottomNavPer) {
            loadFragment(new ProfileFragment());
        } else if (item.getItemId() == R.id.bottomNavAdd) {
            loadFragment(new AddEventTypeFragment());
        } else if (item.getItemId() == R.id.bottomNavFavo) {
            loadFragment(new FavoriteEventFragment());
            // Inside your existing activity
        } else if (item.getItemId() == R.id.bottomNavNoti) {
            loadFragment(new NotificationFragment());
            // Inside your existing activity
        } else if (item.getItemId() == R.id.sideNavLogin) {
            drawerLayout.close();
//            loadFragment(new LoginFragment());
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

            // Inside your existing activity
        } else if (item.getItemId() == R.id.sideNavLogout) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Logout");
            builder.setMessage("Are you sure you want to log out?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    firebaseAuth.signOut();
                    drawerLayout.close();
                    loadFragment(new HomeFragment());
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Do nothing or handle cancellation
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return true;
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                User user = new User();
                user.setUserId(firebaseAuth.getCurrentUser().getUid());
                user.setFCMToken(token);
                firestore.collection("user").whereEqualTo("userId", firebaseAuth.getCurrentUser().getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        documentSnapshot.getReference().update("fcmtoken", token)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Log.d(TAG, "FCM Token Update success");
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e(TAG, "Error updating FCM Token");
                                                    }
                                                });
                                    }
                                } else {
                                    Log.d(TAG, "ERRor");
                                }
                            }
                        });

            }
        });
    }

    public void loadFragment(Fragment fragment) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (fragment instanceof HomeFragment) {
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.commit();
        } else {

            if (currentUser != null) {
                Toast.makeText(this, currentUser.getEmail().toString(), Toast.LENGTH_SHORT).show();
                if (currentUser.isEmailVerified()) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                            .commit();
                } else {
                    Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }
}