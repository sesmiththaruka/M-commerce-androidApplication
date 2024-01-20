package lk.jiat.xpect.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;


public class RegisterFragment extends Fragment {


    public static final String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        EditText registerEmail = view.findViewById(R.id.registerEmail);
        EditText registerPassword = view.findViewById(R.id.registerPassword);

        view.findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                if (!isValidEmail(email)) {
                    // Display toast for invalid email
                    Toast.makeText(getContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidPassword(password)) {
                    // Display toast for invalid password
                    Toast.makeText(getContext(), "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                firebaseAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Log.i(TAG,"create user success");

                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    user.sendEmailVerification();
                                    Toast.makeText(getContext(), "PLease verify your email", Toast.LENGTH_SHORT).show();
                                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.container, new VerifyFragment());
                                    transaction.commit();
                                }else {
                                    Log.w(TAG,"fail");
                                }
                            }
                        });


            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new LoginFragment());
                transaction.commit();
            }
        });

    }
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }
}