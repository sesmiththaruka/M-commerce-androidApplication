package lk.jiat.xpect.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.dto.EventDTO;
import lk.jiat.xpect.dto.FavoriteDTO;
import lk.jiat.xpect.entity.Event;
import lk.jiat.xpect.entity.EventEntity;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HomeFragment extends Fragment {

    public static final String TAG = MainActivity.class.getName();
    //    private ProgressDialog progressDialog;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private RecyclerView.Adapter adapter;
    private ArrayList<EventDTO> eventDTOS;

    private ArrayList<EventEntity> eventEntities;
    private String name;
    private String date;
    private String time;
    private String description;
    private String imagePath;
    private String city;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        firestore.collection("eventEntity").whereEqualTo("status", "1")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()) {
                            List<DocumentSnapshot> documents = value.getDocuments();
                            eventDTOS = new ArrayList<>();

                            for (DocumentSnapshot documentSnapshot : documents){
                                if (documentSnapshot.exists()){
                                    String eventUniqueId = documentSnapshot.getString("eventUniqueId");
                                    String name = documentSnapshot.getString("name");
                                    String description = documentSnapshot.getString("description");
                                    String date = documentSnapshot.getString("date");
                                    String time = documentSnapshot.getString("time");
                                    String city = documentSnapshot.getString("city");
                                    String imagePath = documentSnapshot.getString("imagePath");
                                    EventDTO eventDTO = new EventDTO();
                                    eventDTO.setEventName(name);
                                    eventDTO.setEventUniqueId(eventUniqueId);
                                    eventDTO.setEventDescription(description);
                                    eventDTO.setEventDate(date);
                                    eventDTO.setEventTime(time);
                                    eventDTO.setEventLocation(city);
                                    eventDTO.setImageUrl(imagePath);

                                    eventDTO.setCategoryName("laa");

//                                    adapter.notifyDataSetChanged();
//                                    Log.i(TAG, "onEvent: ****************");
                                    firestore.collection("ticketTypes")
                                            .whereEqualTo("eventUniqueId", eventUniqueId)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                List<Double> prices = new ArrayList<>();

                                                if (!queryDocumentSnapshots.isEmpty()) {

                                                    for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {

                                                        double price = documentSnapshot1.getDouble("price");
                                                        prices.add(price);
                                                    }
                                                }


                                                if (!prices.isEmpty()) {

                                                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                                                    decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

                                                    double leastPrice = Collections.min(prices);
                                                    double highestPrice = Collections.max(prices);

                                                    String formattedLeastPrice = decimalFormat.format(leastPrice);
                                                    String formattedHighestPrice = decimalFormat.format(highestPrice);

                                                    eventDTO.setTicketPrice("Rs." + formattedLeastPrice + " - " + "Rs." + formattedHighestPrice);
                                                    eventDTOS.add(eventDTO);
                                                    adapter.notifyDataSetChanged();

                                                }
                                            })
                                            .addOnFailureListener(e1 -> Log.e(TAG, "Failed to fetch ticket types: " + e1.getMessage()));

                                }
                            }
//                            Log.i(TAG, "onEvent: "+eventDTOS.size());
//                            Log.i(TAG, "onEvent: "+eventDTOS.get(0).getEventName());
//                            Log.i(TAG, "onEvent: "+eventDTOS.get(1).getEventName());
                            adapter = new RecyclerView.Adapter() {
                                @NonNull
                                @Override
                                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                                    View eventView = inflater.inflate(R.layout.home_single_event_view_layout, parent, false);
                                    return new VH(eventView);
                                }

                                @Override
                                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                                    VH vh = (VH) holder;
                                    EventDTO currentEvent = eventDTOS.get(position);
                                    vh.eventName.setText(eventDTOS.get(position).getEventName());
                                    Picasso.get().load(eventDTOS.get(position).getImageUrl()).into(vh.eventImage);
                                    vh.date.setText(eventDTOS.get(position).getEventDate());
                                    vh.time.setText(eventDTOS.get(position).getEventTime());
                                    vh.location.setText(eventDTOS.get(position).getEventLocation());
                                    vh.ticketPrice.setText(eventDTOS.get(position).getTicketPrice());
//                                    progressDialog.dismiss();
                                    holder.itemView.setOnClickListener(view -> {

                                        SingleEventFragment singleEventFragment = new SingleEventFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("uniqueId", eventDTOS.get(position).getEventUniqueId());
                                        singleEventFragment.setArguments(bundle);
                                        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                        fragmentTransaction.replace(R.id.container, singleEventFragment);
                                        fragmentTransaction.addToBackStack(null);
                                        fragmentTransaction.commit();
//                                    Log.i(TAG,"clicked"+events.get(position).getEventUniqueId());
                                    });
                                    vh.starIcon.setOnClickListener(view -> {
                                        EventDTO clickedEvent = eventDTOS.get(holder.getAdapterPosition());
                                        FavoriteDTO favoriteDTO = new FavoriteDTO();
                                        favoriteDTO.setEventUniqueId(clickedEvent.getEventUniqueId());
                                        favoriteDTO.setUserId(firebaseAuth.getCurrentUser().getUid());
                                        favoriteDTO.setEventName(clickedEvent.getEventName());
                                        favoriteDTO.setEventDescription(clickedEvent.getEventDescription());
                                        favoriteDTO.setEventTime(clickedEvent.getEventTime());
                                        favoriteDTO.setEventDate(clickedEvent.getEventDate());
                                        favoriteDTO.setCategoryName(clickedEvent.getCategoryName());
                                        favoriteDTO.setImageUrl(clickedEvent.getImageUrl());
                                        favoriteDTO.setEventLocation(clickedEvent.getEventLocation());
                                        favoriteDTO.setTicketPrice(clickedEvent.getTicketPrice());
                                        addToFavorites(favoriteDTO);
                                    });
                                }

                                @Override
                                public int getItemCount() {

                                    return eventDTOS.size();
                                }
                            };


                            RecyclerView recyclerView = fragment.findViewById(R.id.eventLoadRecyclerView);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });


    }

    private void addToFavorites(FavoriteDTO event) {


        firestore.collection("favoriteEvents")
                .whereEqualTo("eventUniqueId", event.getEventUniqueId())
                .whereEqualTo("userId", event.getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        firestore.collection("favoriteEvents")
                                .document(documentSnapshot.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Event removed from  favorites");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing event from favorites", e);
                                });
                    } else {
                        HashMap<String, Object> eventMap = new HashMap<>();
                        eventMap.put("eventUniqueId",event.getEventUniqueId());
                        eventMap.put("userId",event.getUserId());
                        firestore.collection("favoriteEvents").document(event.getEventUniqueId())
                                .set(eventMap)
                                .addOnSuccessListener(aVoid -> {

                                    Log.d(TAG, "Event added to favorites");
                                })
                                .addOnFailureListener(e -> {

                                    Log.e(TAG, "Error adding event to favorites", e);
                                });
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error chhecking for existing event in favorites", e);
                });


    }


}

class VH extends RecyclerView.ViewHolder {

    ImageView eventImage, calendarIcon, clockIcon, locationIcon;
    TextView eventName, date, time, location, ticketPrice;
    Button btnBuyTicket, starIcon;

    public VH(@NonNull View itemView) {
        super(itemView);
        eventImage = itemView.findViewById(R.id.eventImage);
        starIcon = itemView.findViewById(R.id.btnAddFavorite);
        calendarIcon = itemView.findViewById(R.id.calenderIcon);
        clockIcon = itemView.findViewById(R.id.clockIcon);
        locationIcon = itemView.findViewById(R.id.locationIcon);
        eventName = itemView.findViewById(R.id.eventName);
        date = itemView.findViewById(R.id.date);
        time = itemView.findViewById(R.id.time);
        location = itemView.findViewById(R.id.location);
        ticketPrice = itemView.findViewById(R.id.ticketPrice);
        btnBuyTicket = itemView.findViewById(R.id.btnBuyTicket);
    }
}