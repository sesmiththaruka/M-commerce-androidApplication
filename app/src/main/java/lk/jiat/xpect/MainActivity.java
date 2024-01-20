package lk.jiat.xpect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.jiat.xpect.fragments.AddEventTypeFragment;
import lk.jiat.xpect.fragments.FavoriteEventFragment;
import lk.jiat.xpect.fragments.HomeFragment;
import lk.jiat.xpect.fragments.LoginFragment;
import lk.jiat.xpect.fragments.ProfileFragment;
import lk.jiat.xpect.fragments.RegisterFragment;
import lk.jiat.xpect.fragments.VerifyFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    //    side nav
    private DrawerLayout drawerLayout;

    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);
    }

    /////////////////////////////////////////////////////////////
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        if (item.getItemId() == R.id.sideNavHome) {
            loadFragment(new HomeFragment());
        }else if (item.getItemId() == R.id.bottomNavHome){
            loadFragment(new HomeFragment());
        }else if (item.getItemId() == R.id.bottomNavPer){
            loadFragment(new ProfileFragment());
        }else if (item.getItemId() == R.id.bottomNavAdd){
            loadFragment(new AddEventTypeFragment());
        }else if (item.getItemId() == R.id.bottomNavFavo){
            loadFragment(new FavoriteEventFragment());
            // Inside your existing activity
        }else if (item.getItemId() == R.id.sideNavLogout){
            loadFragment(new HomeFragment());
            firebaseAuth.signOut();
            loadFragment(new HomeFragment());
        }

        return true;
    }

    public void loadFragment(Fragment fragment) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();


        if (fragment instanceof HomeFragment){
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.commit();
        }else {
            Toast.makeText(this, "pf", Toast.LENGTH_SHORT).show();
            if (currentUser != null){
                Toast.makeText(this, currentUser.getEmail().toString(), Toast.LENGTH_SHORT).show();
                if (currentUser.isEmailVerified()){
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment)
                            .commit();
                }else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,new VerifyFragment())
                            .commit();
                }
            }else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,new LoginFragment())
                        .commit();
            }
        }


    }
}