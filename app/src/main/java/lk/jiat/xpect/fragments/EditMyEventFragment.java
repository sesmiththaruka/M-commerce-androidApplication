package lk.jiat.xpect.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.entity.Event;
import lk.jiat.xpect.model.Category;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class EditMyEventFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    private String eventUniqueId;
    private String eventName;
    private String eventDescription;
    private String eventDate;
    private String eventTime;
    private String date;
    private String time;
    private List<Category> categories;
    private ImageView eventImageUpdate;
   private EditText eventNameUpdate;
   private EditText eventDescriptionUpdate;
   private TextView eventDateUpdate ;
   private TextView eventTimeUpdate ;
   private int category;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle arguments = getArguments();
        eventUniqueId = arguments.getString("eventUniqueId");
        return inflater.inflate(R.layout.fragment_edit_my_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, eventUniqueId);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);
        Call<Event> eventByUniqueId = xpectWebService.getEventByUniqueId(eventUniqueId);
        eventByUniqueId.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful()) {
                    Event event = response.body();
                    eventName = event.getName();
                    eventDescription = event.getDescription();
                    eventDate = event.getDate();
                    eventTime = event.getTime();
                    eventImageUpdate = view.findViewById(R.id.imageViewEditEventImage);
                     eventNameUpdate = view.findViewById(R.id.editTextEditEventEditName);
                     eventDescriptionUpdate = view.findViewById(R.id.editTextEditEventEditDescription);
                     eventDateUpdate = view.findViewById(R.id.editTextEditEventEditDate);
                     eventTimeUpdate = view.findViewById(R.id.editTextEditEventEditTime);

                     eventNameUpdate.setText(eventName);
                     eventDescriptionUpdate.setText(eventDescription);
                     eventDateUpdate.setText(eventDate);
                     eventTimeUpdate.setText(eventTime);

                     firestore.collection("eventImages").whereEqualTo("eventId",eventUniqueId)
                             .get()
                             .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()){
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    String imagePath = documentSnapshot.getString("imagePath");
                                    Picasso.get().load(imagePath).into(eventImageUpdate);
                                }
                             });
                }

                ImageButton getDateBtn = view.findViewById(R.id.imageBtnSelectDate);
                getDateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDateDialog();
                    }
                });

                ImageButton getTimeBtn = view.findViewById(R.id.imageBtnSelectTime);
                getTimeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openTimeDialog();
                    }
                });
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {

            }
        });
        Call<List<Category>> categoryCall = xpectWebService.getAllCategory();
        categoryCall.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    categories = response.body();
                    Spinner spinner = view.findViewById(R.id.updateCategorySpinner);
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
        view.findViewById(R.id.btnUpdateEvent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextfinalEventName = view.findViewById(R.id.editTextEditEventEditName);
                String finalEventName = editTextfinalEventName.getText().toString();

                EditText editTextfinalEventDescription = view.findViewById(R.id.editTextEditEventEditDescription);
                String finalEventDescription = editTextfinalEventDescription.getText().toString();

                TextView textViewfinalEventDate = view.findViewById(R.id.editTextEditEventEditDate);
                String finalEventDate = textViewfinalEventDate.getText().toString();

                TextView textViewfinalEventTime = view.findViewById(R.id.editTextEditEventEditTime);
                String finalEventTime = textViewfinalEventTime.getText().toString();

                Event event = new Event(finalEventName,finalEventDescription,finalEventDate,finalEventTime,eventUniqueId,currentUserId,category,1);

                Call<Void> voidCall = xpectWebService.updateEvent(event);
                voidCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(getContext(), "Update successfull", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });

            }
        });
    }
    private void openDateDialog() {
        DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String stringDate = String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(day);
                eventDate = stringDate;
                eventDateUpdate.setText(stringDate);
            }
        }, 2023, 11, 15); // Year, month, day for the initial date in the dialog
        dialog.show();
    }
    private void openTimeDialog() {
        TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                eventTime = String.valueOf(hourOfDay) + "-" + String.valueOf(minute);
                eventTimeUpdate.setText(time);
                Toast.makeText(getContext(), time, Toast.LENGTH_SHORT).show();
            }
        }, 15, 00, true);
        dialog.show();
    }
}