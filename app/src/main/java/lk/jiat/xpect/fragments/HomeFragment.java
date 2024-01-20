package lk.jiat.xpect.fragments;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.dto.EventDTO;
import lk.jiat.xpect.dto.FavoriteDTO;
import lk.jiat.xpect.model.Event;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HomeFragment extends Fragment {

    public static final String TAG = MainActivity.class.getName();
    private List<EventDTO> events;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private RecyclerView.Adapter adapter;

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
//        Log.i(TAG, "Home");
        //        setup retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);
//       setup retrofit
        Call<ArrayList<EventDTO>> allEventCall = xpectWebService.getAllEvent();
        allEventCall.enqueue(new Callback<ArrayList<EventDTO>>() {
            @Override
            public void onResponse(Call<ArrayList<EventDTO>> call, Response<ArrayList<EventDTO>> response) {
                if (response.isSuccessful()) {
                    events = response.body();
                    if (events != null) {
                        Log.i(TAG, String.valueOf(events.size()));
                        for (EventDTO e : events) {

                            //retrive img from firebase
                            firestore.collection("eventImages")
                                    .whereEqualTo("eventId", e.getEventUniqueId())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {

                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                            String imageUrl = documentSnapshot.getString("imagePath");
                                            e.setImageUrl(imageUrl);

                                            adapter.notifyDataSetChanged();
                                        }
                                    }).addOnFailureListener(o -> Log.e(TAG, "Firestore data retrieval failed: " + o.getMessage()));
                            //retrive img from firebase
                            //retrive ticket price from firebase

                            firestore.collection("ticketTypes")
                                    .whereEqualTo("eventUniqueId", e.getEventUniqueId())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        List<Double> prices = new ArrayList<>();

                                        if (!queryDocumentSnapshots.isEmpty()) {

                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                                double price = documentSnapshot.getDouble("price");
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

                                            e.setTicketPrice("Rs." + formattedLeastPrice + " - " + "Rs." + formattedHighestPrice);

                                        }
                                    })
                                    .addOnFailureListener(e1 -> Log.e(TAG, "Failed to fetch ticket types: " + e1.getMessage()));

                            //retrive ticket price from firebase
                            //retrive eventlocation from firebase
                            firestore.collection("eventLocations")
                                    .whereEqualTo("eventId", e.getEventUniqueId())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {

                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            Log.i(TAG,"Set loca");
                                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                            String imageUrl = documentSnapshot.getString("city");
                                            e.setEventLocation(imageUrl);


                                        }
                                    }).addOnFailureListener(o -> Log.e(TAG, "Firestore data retrieval failed: " + o.getMessage()));
                            //retrive eventlocation from firebase

                        }
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
                                EventDTO currentEvent = events.get(position);
                                vh.eventName.setText(events.get(position).getEventName());
                                Picasso.get().load(events.get(position).getImageUrl()).into(vh.eventImage);
                                vh.date.setText(events.get(position).getEventDate());
                                vh.time.setText(events.get(position).getEventTime());
                                vh.location.setText(events.get(position).getEventLocation());
                                vh.ticketPrice.setText(events.get(position).getTicketPrice());

                                holder.itemView.setOnClickListener(view -> {

                                    SingleEventFragment singleEventFragment = new SingleEventFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("uniqueId", events.get(position).getEventUniqueId());
                                    singleEventFragment.setArguments(bundle);
                                    FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.replace(R.id.container, singleEventFragment);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
//                                    Log.i(TAG,"clicked"+events.get(position).getEventUniqueId());
                                });
                                vh.starIcon.setOnClickListener(view -> {
                                    EventDTO clickedEvent = events.get(holder.getAdapterPosition());
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
                                return events.size();
                            }
                        };


                        RecyclerView recyclerView = fragment.findViewById(R.id.eventLoadRecyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<EventDTO>> call, Throwable t) {
                Log.i(TAG, t.getMessage());
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
                        firestore.collection("favoriteEvents").document(event.getEventUniqueId())
                                .set(event)
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