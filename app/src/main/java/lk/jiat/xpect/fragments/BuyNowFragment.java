package lk.jiat.xpect.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;
import lk.jiat.xpect.entity.TicketType;


public class BuyNowFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();
    private static int MAX_QTY;
    private FirebaseFirestore firestore;
    private ImageView eventImageView;
    private EditText editQuantity;
    private TextView viewAmount;
    private String eventUniqueId;
    private String eventName;
    private String imagePath;
    private String typeQuantityString;
    private int calculateQuantity;
    private double calculateAmount;
    private String ticketTypeName = "hjgb";
    private double ticketTypePrice = 0;
    private String selectedTicketName;
    double selectedTicketPrice;
    private ArrayList<TicketType> ticketTypes;
    private ArrayList<String> namesAndPrices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        eventUniqueId = arguments.getString("eventUniqueId");
        eventName = arguments.getString("eventName");
        return inflater.inflate(R.layout.fragment_buy_now, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        eventImageView = view.findViewById(R.id.singleEventViewImage);
        editQuantity = view.findViewById(R.id.editTextAddQuantity);
        viewAmount = view.findViewById(R.id.viewAmount);
        Spinner spinner = view.findViewById(R.id.ticketTypeLoadSpinner);
//      get image from firebase
        firestore.collection("eventImages").whereEqualTo("eventId", eventUniqueId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        //
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        imagePath = document.getString("imagePath");
                        if (imagePath != null && !imagePath.isEmpty()) {

                            Picasso.get().load(imagePath).into(eventImageView);
                        } else {
//                                        Log.i(TAG,"image not set");
                        }
                        //
                    }
                });
//      get image from firebase
//get ticket types from firebase
        firestore.collection("ticketTypes").whereEqualTo("eventUniqueId", eventUniqueId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            ticketTypes = new ArrayList<>();
                            namesAndPrices = new ArrayList<>();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                TicketType ticketType = doc.toObject(TicketType.class);
                                ticketTypes.add(ticketType);

                                ticketTypeName = ticketType.getTypeName();

                                ticketTypePrice = ticketType.getPrice();
                                namesAndPrices.add(ticketTypeName + "- Rs." + ticketTypePrice);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, namesAndPrices);
                            adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                            spinner.setAdapter(adapter);
                        }
                    }
                });
//get ticket types from firebase
//spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTicket = namesAndPrices.get(position);
                String[] parts = selectedTicket.split("- Rs.");
                selectedTicketName = parts[0];
                selectedTicketPrice = Double.parseDouble(parts[1]);

                updateAmount();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//spinner
        editQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                typeQuantityString = editQuantity.getText().toString();
                for (TicketType t : ticketTypes){
                    if (t.getTypeName().toString().equals(selectedTicketName) && t.getPrice()==selectedTicketPrice){
                        MAX_QTY = t.getQuantity();
                    }
                }
                if (!typeQuantityString.isEmpty()) {
                    calculateQuantity = Integer.parseInt(typeQuantityString);
                    if (calculateQuantity <= MAX_QTY) {
                        calculateAmount = calculateQuantity * selectedTicketPrice;
                        viewAmount.setText(String.valueOf(calculateAmount));
                    }else{
                        editQuantity.setText(String.valueOf(MAX_QTY));
                        editQuantity.setSelection(editQuantity.getText().length());
                        Toast.makeText(getContext(), String.valueOf(MAX_QTY), Toast.LENGTH_SHORT).show();

                    }

                } else {
                    viewAmount.setText("0000.00");
                }
            }
        });
    }

    public void updateAmount(){



        typeQuantityString = editQuantity.getText().toString();

        if (!typeQuantityString.isEmpty()) {
            calculateQuantity = Integer.parseInt(typeQuantityString);
            calculateAmount = calculateQuantity * selectedTicketPrice;
            viewAmount.setText(String.valueOf(calculateAmount));
        } else {
            // Handle the case when editQuantity is empty or non-numeric
            viewAmount.setText("0000.00");
        }
    }
}