package lk.jiat.xpect.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.entity.Event;
import lk.jiat.xpect.model.Category;
import lk.jiat.xpect.service.XpectWebService;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PublicEventRegisterFragment extends Fragment {
    public static String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;
    private String userId;
    private String name;
    private int category;
    private String description;
    private String date;
    private String time;
    private int typeId = 2;
    private TextView dateView;
    private TextView timeView;
    private String eventUniqueId;
    private List<Category> categories;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_public_event_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        get date view & time view from xml
        dateView = view.findViewById(R.id.dateView);
        timeView = view.findViewById(R.id.timeView);
//        get date view & time view from xml
//        current user id
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
//        current user id

//        setup retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);
//       setup retrofit
//       load category from API
        Call<List<Category>> categoryCall = xpectWebService.getAllCategory();
        categoryCall.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    categories = response.body();
                    Spinner spinner = view.findViewById(R.id.categorySpinner);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Category selectedCategory = categories.get(position);
                            category = selectedCategory.getId();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    List<Category> sampleCategoryList = categories;
                    List<String> categoryNames = new ArrayList<>();

                    for (Category category : sampleCategoryList) {
                        categoryNames.add(category.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    spinner.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.i(TAG, t.getMessage());
            }
        });
//       load category from api

//        open date dialog
        Button button = view.findViewById(R.id.btnDate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
//        open date dialog
//        open time dialog
        view.findViewById(R.id.btnTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimeDialog();
            }
        });
//        open time dialog
//        register button
        view.findViewById(R.id.btnregisterEventNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//           get event name
                EditText editName = view.findViewById(R.id.editTextEventName);
                name = String.valueOf(editName.getText());
//           get event name

//           get event description
                EditText editDescription = view.findViewById(R.id.editTextDescription);
                description = editDescription.getText().toString();
//           get event description
//           get unique id
                eventUniqueId = generateUniqueId();
//           get unique id
                if (name.isEmpty()){
                    Toast.makeText(getContext(), "Event name is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (description.isEmpty()){
                    Toast.makeText(getContext(), "Event description is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (description.isEmpty()){
                    Toast.makeText(getContext(), "Event description is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (date == null || time == null) {
                    // Show error message for date or time not selected
                    Toast.makeText(getContext(), "Please select date and time", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault());
                try {
                    Date selectedDateTime = sdf.parse(date + "-" + time);
                    if (selectedDateTime != null && selectedDateTime.before(new Date())) {
                        // Show error message for past date selection
                        Toast.makeText(getContext(), "Please select a future date and time", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//           set data to the event
                Event event = new Event(name, description, date, time,eventUniqueId, userId, category, typeId);
//           set data to the event

//           api call
                Call<Void> voidCall = xpectWebService.registerEvent(event);

                voidCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.i(TAG,"save success");
                        Toast.makeText(getContext(), "Successfully saved", Toast.LENGTH_SHORT).show();
                        updateUi();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.i(TAG,"save fail");
                    }
                });
//            api call
            }
        });
//       register button
    }

//   goto next fragment method
    private void updateUi(){
//    set data to the bundle
        PublicEventRegisterAddImageAndTicketFragment publicEventRegisterAddTicketFragment = new PublicEventRegisterAddImageAndTicketFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uniqueId",eventUniqueId);
        bundle.putString("eventName",name);
        publicEventRegisterAddTicketFragment.setArguments(bundle);
//    set data to the bundle
//    fragment transaction
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, publicEventRegisterAddTicketFragment);
        transaction.commit();
//    fragment transaction
    }
//   go to next fragment method

//        date dialog
    private void openDialog() {
        DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String stringDate = String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(day);
                date = stringDate;
                dateView.setText(stringDate);
            }
        }, 2023, 11, 15); // Year, month, day for the initial date in the dialog
        dialog.show();
    }

//        date dialog
//        time dialog
    private void openTimeDialog() {
        TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                time = String.valueOf(hourOfDay) + "-" + String.valueOf(minute);
                timeView.setText(time);
                Toast.makeText(getContext(), time, Toast.LENGTH_SHORT).show();
            }
        }, 15, 00, true);
        dialog.show();
    }
//        time dialog

// Method to generate a unique ID
    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
    // Method to generate a unique ID
}