package lk.jiat.xpect.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.dto.UserDTO;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VerifyActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        firebaseAuth = FirebaseAuth.getInstance();

        Retrofit retrofit =  new Retrofit.Builder()
//                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .baseUrl("http://192.168.137.1/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);
        findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            if (currentUser.isEmailVerified()){
                                Intent intent = new Intent(VerifyActivity.this, MainActivity.class);
                                intent.putExtra("openProfileFragment", true);
                                startActivity(intent);
                                finish();
                                UserDTO userDTO = new UserDTO();
                                userDTO.setFirebaseUserId(currentUser.getUid());
                                userDTO.setName(currentUser.getDisplayName());
                                Call<Void> voidCall = xpectWebService.addNewUser(userDTO);
                                voidCall.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()){
                                             // Close LoginActivity
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {

                                    }
                                });
                            }else {
                                Toast.makeText(VerifyActivity.this, "Please Verify Your Email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
        
    }
}