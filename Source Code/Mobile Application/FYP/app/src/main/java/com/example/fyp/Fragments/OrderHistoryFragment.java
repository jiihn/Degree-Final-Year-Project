package com.example.fyp.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fyp.Adapter.OrderHistoryAdapter;
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

public class OrderHistoryFragment extends Fragment {

    private OrderHistoryAdapter orderHistoryAdapter;

    private List<Order> mOrder;
    RecyclerView historyList;
    private List<Orderlist> orderlists;

    DatabaseReference mDatabase, reference;
    FirebaseUser fuser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        historyList = view.findViewById(R.id.list_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        historyList.setLayoutManager(mLayoutManager);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        orderlists = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Orderlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderlists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Orderlist orderlist = snapshot.getValue(Orderlist.class);
                    orderlists.add(orderlist);
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

                    for(Orderlist orderlist : orderlists) {
                        if(order.getOrderId().equals(orderlist.getId())) {
                            if(order.isPaid() && order.isComplete()) {
                                mOrder.add(order);
                            }
                        }
                    }
                }

                orderHistoryAdapter = new OrderHistoryAdapter(getContext(), mOrder);
                historyList.setAdapter(orderHistoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
