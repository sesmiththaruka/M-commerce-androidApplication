package lk.jiat.xpect.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;


public class SignUpActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        EditText registerEmail = findViewById(R.id.registerEmail);
        EditText registerPassword = findViewById(R.id.registerPassword);

        findViewById(R.id.btnSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                if (!isValidEmail(email)) {
                    // Display toast for invalid email
                    Toast.makeText(SignUpActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }else if (!isValidPassword(password)) {
                    // Display toast for invalid password
                    Toast.makeText(SignUpActivity.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    Log.i(TAG, email);
                    Log.i(TAG, password);
                    firebaseAuth.createUserWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Log.i(TAG,"Create User Success");
                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        user.sendEmailVerification();
                                        Toast.makeText(SignUpActivity.this, "PLease verify your email", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUpActivity.this, VerifyActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    }else {
                                        Log.w(TAG,"fail");
                                        Exception exception = task.getException();
                                        if (exception instanceof FirebaseAuthException) {
                                            FirebaseAuthException firebaseAuthException = (FirebaseAuthException) exception;
                                            Toast.makeText(SignUpActivity.this, "Your Email Already Exist", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }
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
