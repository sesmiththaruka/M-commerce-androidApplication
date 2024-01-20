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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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

public class SingleEventFragment extends Fragment implements OnMapReadyCallback {
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
    private MapView mapView;
    private GoogleMap map;
    private double longitude;
    private double latitude;

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
//
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        //

        ticketTypes = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        singleTicket = view.findViewById(R.id.eventSingleViewrecyclerView);
        btnBuyNow = view.findViewById(R.id.btnBuyNow);

        firestore.collection("eventEntity").whereEqualTo("eventUniqueId", eventUniqueId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            eventName = documentSnapshot.getString("name");
                            eventDescription = documentSnapshot.getString("description");
                            eventDate = documentSnapshot.getString("date");
                            eventTime = documentSnapshot.getString("time");
                            imagePath = documentSnapshot.getString("imagePath");
                            location = documentSnapshot.getString("city");
                            latitude = documentSnapshot.getDouble("locationLatitude");
                            longitude = documentSnapshot.getDouble("locationLongitude");
                            onMapReady(map);
                            eventNameView = view.findViewById(R.id.singleEventViewEventName);
                            eventDescriptionView = view.findViewById(R.id.singleEventViewDescription);
                            eventLocationView = view.findViewById(R.id.singleEventViewEventLocation);
                            eventDateView = view.findViewById(R.id.singleEventViewEventDate);
                            eventTimeView = view.findViewById(R.id.singleEventViewEventTime);
                            imageView = view.findViewById(R.id.singleEventViewImage);

                            eventNameView.setText(eventName);
                            eventDescriptionView.setText(eventDescription);
                            eventDateView.setText(eventDate);
                            eventTimeView.setText(eventTime);
                            eventLocationView.setText(location);

                            if (imagePath != null && !imagePath.isEmpty()) {
                                Picasso.get().load(imagePath).into(imageView);
                            } else {
//                                        Log.i(TAG,"image not set");
                            }
                        }
                    }
                }).addOnFailureListener(command -> {
                    command.getMessage();
                });


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
                // Pass the latitude and longitude to the new activity
                Intent intent = new Intent(getContext(), ViewLocationActivity.class);
                intent.putExtra("LATITUDE", latitude);
                intent.putExtra("LONGITUDE", longitude);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        LatLng eventLocation = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(eventLocation).title("Event Location"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 12f));
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