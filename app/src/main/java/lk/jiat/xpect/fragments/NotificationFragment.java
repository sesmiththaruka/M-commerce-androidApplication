package lk.jiat.xpect.fragments;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.activity.NotificationViewActivity;
import lk.jiat.xpect.dto.EventDTO;
import lk.jiat.xpect.entity.Event;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private ArrayList<EventDTO> events;
    private RecyclerView.Adapter adapter;
    private HashMap eventsStatus;

    //  setup notification
    private String channelId = "info";
//  setup notification


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);

        firestore.collection("eventStatus").whereEqualTo("userId", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()) {
                            Log.d(TAG, "onEvent: value is not empty");
                            List<DocumentSnapshot> documents = value.getDocuments();
                            events = new ArrayList<>();
                            eventsStatus = new HashMap();
                            for (DocumentSnapshot documentSnapshot : documents) {

                                String status = documentSnapshot.getString("status");
                                String eventId = documentSnapshot.getString("eventUniqueId");
                                Log.i(TAG, "onEvent: sta"+status);
                                Log.i(TAG, "onEvent: sta"+eventId);
                                eventsStatus.put("eventId", eventId);
                                eventsStatus.put("status", status);

                                firestore.collection("eventEntity").whereEqualTo("eventUniqueId",eventId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                if (!value.isEmpty()){
                                                    EventDTO eventDTO = new EventDTO();
                                                    DocumentSnapshot documentSnapshot1 = value.getDocuments().get(0);
                                                    String imagePath = documentSnapshot1.getString("imagePath");
                                                    eventDTO.setImageUrl(imagePath);
                                                    if (eventsStatus.equals("1")) {
                                                        eventDTO.setCategoryName("1");
                                                        Log.i(TAG, eventId + " - Your Event is approved");

                                                    } else if (status.equals("2")) {
                                                        eventDTO.setCategoryName("2");
                                                        Log.e(TAG, eventId + " - Your event is Rejected");
                                                    }
                                                    events.add(eventDTO);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });


//                                Call<EventDTO> eventCall = xpectWebService.getEventByUniqueIdEventDTO(eventId);
//                                eventCall.enqueue(new Callback<EventDTO>() {
//                                    @Override
//                                    public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
//                                        if (response.isSuccessful()) {
//                                            Log.i(TAG, "onResponse: response success");
//                                            EventDTO eventDTO = response.body();
//
//                                            firestore.collection("eventImages")
//                                                    .whereEqualTo("eventId",eventId)
//                                                    .get()
//                                                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                                                        if (!queryDocumentSnapshots.isEmpty()){
//                                                            DocumentSnapshot documentSnapshot1 = queryDocumentSnapshots.getDocuments().get(0);
//                                                            String imagePath = documentSnapshot1.getString("imagePath");
//                                                            eventDTO.setImageUrl(imagePath);
//                                                            if (eventsStatus.equals("1")) {
//                                                                eventDTO.setCategoryName("1");
//                                                                Log.i(TAG, eventId + " - Your Event is approved");
//
//                                                            } else if (status.equals("2")) {
//                                                                eventDTO.setCategoryName("2");
//                                                                Log.e(TAG, eventId + " - Your event is Rejected");
//                                                            }
//
//                                                            Log.i(TAG, "onResponse: set image");
//                                                            events.add(eventDTO);
//
//                                                            Log.i(TAG, "onResponse: set event");
//                                                            adapter.notifyDataSetChanged();
//                                                        }
//                                                    });
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<EventDTO> call, Throwable t) {
//
//                                    }
//                                });




                            }

                            adapter = new RecyclerView.Adapter() {
                                @NonNull
                                @Override
                                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                                    View eventView = inflater.inflate(R.layout.single_notification_view, parent, false);
                                    return new NVH(eventView);
                                }

                                @Override
                                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                                    NVH nvh = (NVH) holder;
                                    Picasso.get().load(events.get(position).getImageUrl()).into(nvh.eventImage);
                                    nvh.eventName.setText(events.get(position).getEventName());
                                    if (events.get(position).getCategoryName().equals("1")){
                                        nvh.eventName.setTextColor(1);
                                        nvh.eventStatus.setText("Approved");
                                    }else if (events.get(position).getCategoryName().equals("2")){
                                        nvh.eventName.setTextColor(2);
                                        nvh.eventStatus.setText("Rejected");
                                    }

                                }

                                @Override
                                public int getItemCount() {
                                    return events.size();
                                }
                            };
                            RecyclerView recyclerView = view.findViewById(R.id.recyclerViewNotification);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(adapter);
                        }else {
                            Log.i(TAG, "onEvent: value is empty");
                        }
                    }
                });


//        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.1.112:8080/api/v1/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);


    }
}

class NVH extends RecyclerView.ViewHolder {
    ImageView eventImage;
    TextView eventName,eventStatus;

    public NVH(@NonNull View itemView) {
        super(itemView);

        eventImage = itemView.findViewById(R.id.notification_event_view);
        eventName = itemView.findViewById(R.id.notification_event_name);
        eventStatus = itemView.findViewById(R.id.notification_event_status);

    }
}