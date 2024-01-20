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
import java.util.List;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.dto.EventDTO;
import lk.jiat.xpect.dto.FavoriteDTO;


public class FavoriteEventFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String currentUserId;

    private RecyclerView.Adapter adapter;
    private ArrayList<EventDTO> eventDTOS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        firestore.collection("favoriteEvents").whereEqualTo("userId", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null){

                            eventDTOS = new ArrayList<>();
                            for (DocumentSnapshot doc : value.getDocuments()){
//                                FavoriteDTO favoriteDTO = doc.toObject(FavoriteDTO.class);
//                                favoriteEvents.add(favoriteDTO);
                                String eventUniqueId = doc.getString("eventUniqueId");
                                firestore.collection("eventEntity").whereEqualTo("eventUniqueId",eventUniqueId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                if (!value.isEmpty()){
                                                    DocumentSnapshot documentSnapshot = value.getDocuments().get(0);

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
                                        });
                            }
                            adapter = new RecyclerView.Adapter() {
                               @NonNull
                               @Override
                               public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                   LayoutInflater inflater = LayoutInflater.from(getActivity());
                                   View eventView = inflater.inflate(R.layout.home_single_event_view_layout, parent, false);
                                   return new FEVH(eventView);
                               }

                               @Override
                               public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                                  FEVH fevh = (FEVH) holder;

                                   fevh.eventName.setText(eventDTOS.get(position).getEventName());
                                   Picasso.get().load(eventDTOS.get(position).getImageUrl()).into(fevh.eventImage);
                                   fevh.date.setText(eventDTOS.get(position).getEventDate());
                                   fevh.time.setText(eventDTOS.get(position).getEventTime());
                                   fevh.location.setText(eventDTOS.get(position).getEventLocation());
                                   fevh.ticketPrice.setText(eventDTOS.get(position).getTicketPrice());

                                   holder.itemView.setOnClickListener(v -> {
                                       SingleEventFragment singleEventFragment = new SingleEventFragment();
                                       Bundle bundle = new Bundle();
                                       bundle.putString("uniqueId", eventDTOS.get(position).getEventUniqueId());
                                       singleEventFragment.setArguments(bundle);
                                       FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                       fragmentTransaction.replace(R.id.container, singleEventFragment);
                                       fragmentTransaction.addToBackStack(null);
                                       fragmentTransaction.commit();
                                   });

                                   fevh.starIcon.setOnClickListener(view -> {
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

                            RecyclerView recyclerView = view.findViewById(R.id.recyclerViewFavoriteEvents);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
//      get favorite events from firebase

    }
    private void addToFavorites(FavoriteDTO event) {
        // Assuming you have a Firestore collection 'favoriteEvents' to store favorite events

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
                                    // Successfully added to favorites
                                    // You can show a message or update UI accordingly
                                    Log.d(TAG, "Event added to favorites");
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to add to favorites
                                    Log.e(TAG, "Error adding event to favorites", e);
                                });
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error chhecking for existing event in favorites", e);
                });


    }
}

class FEVH extends RecyclerView.ViewHolder{
    ImageView eventImage, calendarIcon, clockIcon, locationIcon;
    TextView eventName, date, time, location, ticketPrice;
    Button btnBuyTicket, starIcon;
    public FEVH(@NonNull View itemView) {
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

