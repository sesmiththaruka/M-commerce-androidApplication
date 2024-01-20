package lk.jiat.xpect.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.activity.ViewLocationActivity;
import lk.jiat.xpect.dto.EventDTO;
import lk.jiat.xpect.entity.Event;
import lk.jiat.xpect.entity.TicketType;
import lk.jiat.xpect.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SingleEventFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();
    private Button btnBuyNow;
    private RecyclerView.Adapter adapter;
    private List<TicketType> ticketTypes;
    private int ticketTypeQty;
    private double ticketTypePrice;
    private String ticketTypeName;
    private FirebaseFirestore firestore;
    private String eventUniqueId;

    private ImageView imageView;
    private TextView eventNameView;
    private TextView eventDescriptionView;
    private TextView eventLocationView;
    private TextView eventDateView;
    private TextView eventTimeView;
    private String imagePath;
    private String eventName;
    private String eventDescription;
    private String eventDate;
    private String eventTime;
    private String eventUserId;
    private String location;
    private int categoryId;

    private RecyclerView singleTicket;
    //    private String ticketTypeName;
//    private String ticketTypePrice;
    private String ticketTypeQuantity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            eventUniqueId = bundle.getString("uniqueId");
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getContext(), eventUniqueId, Toast.LENGTH_SHORT).show();
        ticketTypes = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        singleTicket = view.findViewById(R.id.eventSingleViewrecyclerView);
        btnBuyNow = view.findViewById(R.id.btnBuyNow);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);
        Call<Event> eventCall = xpectWebService.getEventByUniqueId(eventUniqueId);
        eventCall.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful()) {
                    Event event = response.body();
                    eventName = event.getName().toString();
                    eventDescription = event.getDescription().toString();
                    eventDate = event.getDate().toString();
                    eventTime = event.getTime().toString();
                    eventUserId = event.getUserId().toString();
                    categoryId = event.getCategoryId();

                    eventNameView = view.findViewById(R.id.singleEventViewEventName);
                    eventDescriptionView = view.findViewById(R.id.singleEventViewDescription);
                    eventLocationView = view.findViewById(R.id.singleEventViewEventLocation);
                    eventDateView = view.findViewById(R.id.singleEventViewEventDate);
                    eventTimeView = view.findViewById(R.id.singleEventViewEventTime);

                    eventNameView.setText(eventName);
                    eventDescriptionView.setText(eventDescription);

                    eventDateView.setText(eventDate);
                    eventTimeView.setText(eventTime);

                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {

            }
        });

//      get image from firebase
        firestore.collection("eventImages").whereEqualTo("eventId", eventUniqueId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        //
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        imageView = view.findViewById(R.id.singleEventViewImage);
                        imagePath = document.getString("imagePath");

                        if (imagePath != null && !imagePath.isEmpty()) {

                            Picasso.get().load(imagePath).into(imageView);
                        } else {
//                                        Log.i(TAG,"image not set");
                        }
                        //
                    }
                });
//      get image from firebase
        //retrive eventlocation from firebase
        firestore.collection("eventLocations")
                .whereEqualTo("eventId", eventUniqueId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.i(TAG, "Set loca");
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String loc = documentSnapshot.getString("city");
                        location = loc;
                        eventLocationView.setText(location);

                    }
                }).addOnFailureListener(o -> Log.e(TAG, "Firestore data retrieval failed: " + o.getMessage()));
        //retrive eventlocation from firebase
//get ticket types from firebase
        firestore.collection("ticketTypes").whereEqualTo("eventUniqueId", eventUniqueId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            ticketTypeName = document.getString("typeName");
                            Long ticketTypePriceStr = document.getLong("price");
                            Long ticketTypeQtyStr = document.getLong("quantity");

                            if (ticketTypeName != null && ticketTypePriceStr != null && ticketTypeQtyStr != null) {
                                try {
                                    ticketTypePrice = ticketTypePriceStr;
                                    ticketTypeQty = Math.toIntExact(ticketTypeQtyStr);

                                    TicketType ticketType = new TicketType(ticketTypeName, ticketTypePrice, ticketTypeQty);
                                    ticketTypes.add(ticketType);
                                    Log.i(TAG, "Work");

                                } catch (NumberFormatException e) {
                                    // Handle parsing errors
                                    Log.e(TAG, "Error parsing price or quantity: " + e.getMessage());
                                }
                            } else {
                                // Handle missing fields
                                Log.e(TAG, "One or more fields are null");
                            }
                        }
                        //create adapter for reclyer view
                        adapter = new RecyclerView.Adapter() {
                            @NonNull
                            @Override
                            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                LayoutInflater inflater = LayoutInflater.from(getActivity());
                                View ticketView = inflater.inflate(R.layout.ticket_types_view_reclyer_view_layout, parent, false);
                                return new TVH(ticketView);
                            }

                            @Override
                            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                                TVH tvh = (TVH) holder;
                                TicketType currentTicket = ticketTypes.get(position);
                                tvh.typeNameView.setText(ticketTypes.get(position).getTypeName());
                                tvh.typePriceView.setText(String.valueOf(ticketTypes.get(position).getPrice()));
                                tvh.typeQtyView.setText(String.valueOf(ticketTypes.get(position).getQuantity()));
                            }

                            @Override
                            public int getItemCount() {
//                                Log.i(TAG, String.valueOf(ticketTypes.size()));
                                return ticketTypes.size();
                            }
                        };
                        //create adapter for reclyer view

                        singleTicket.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
                        singleTicket.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.i(TAG, "No ticket found");
                    }
                });
//get ticket types from firebase

        btnBuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("eventUniqueId", eventUniqueId);
                bundle.putString("eventName", eventName);
                BuyNowFragment buyNowFragment = new BuyNowFragment();
                buyNowFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, buyNowFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        view.findViewById(R.id.locationIconForView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "clcked", Toast.LENGTH_SHORT).show();
                firestore.collection("eventLocations")
                        .whereEqualTo("eventId", eventUniqueId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");

                                // Pass the latitude and longitude to the new activity
                                Intent intent = new Intent(getContext(), ViewLocationActivity.class);
                                intent.putExtra("LATITUDE", latitude);
                                intent.putExtra("LONGITUDE", longitude);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure to retrieve location
                        });
            }
        });
    }

}

class TVH extends RecyclerView.ViewHolder {
    TextView typeNameView, typePriceView, typeQtyView;

    public TVH(@NonNull View itemView) {
        super(itemView);
        typeNameView = itemView.findViewById(R.id.ticketTypeName);
        typePriceView = itemView.findViewById(R.id.ticketTypePrice);
        typeQtyView = itemView.findViewById(R.id.ticketTypeQty);
    }
}