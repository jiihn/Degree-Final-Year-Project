package com.example.fyp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fyp.Adapter.PurchasePaidAdapter;
import com.example.fyp.Model.Order;
import com.example.fyp.Model.Orderlist;
import com.example.fyp.Model.Purchaselist;
import com.example.fyp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PurchaseCompleteFragment extends Fragment {

    private PurchasePaidAdapter purchasePaidAdapter;

    private List<Order> mOrder;
    RecyclerView purchaselist;
    private List<Purchaselist> usersList;

    DatabaseReference mDatabase, reference;
    FirebaseUser fuser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_purchase_complete, container, false);

        purchaselist = view.findViewById(R.id.recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        purchaselist.setLayoutManager(mLayoutManager);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Purchaselist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Purchaselist purchaselist = snapshot.getValue(Purchaselist.class);
                    usersList.add(purchaselist);
                }

                readOrder();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    public void readOrder(){
        mOrder = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("Order");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mOrder.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Order order = snapshot.getValue(Order.class);

                    for(Purchaselist purchaselist : usersList) {
                        if(order.getOrderId().equals(purchaselist.getId())) {
                            if (order.isPaid() && !order.isVerified()) {
                                mOrder.add(order);
                            }
                        }
                    }
                }

                purchasePaidAdapter = new PurchasePaidAdapter(getContext(), mOrder);
                purchaselist.setAdapter(purchasePaidAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
