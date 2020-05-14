package com.example.fyp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fyp.Adapter.ServiceAdapter;
import com.example.fyp.Model.Service;
import com.example.fyp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchServiceFragment extends Fragment {

    private RecyclerView mServiceList;
    private DatabaseReference mDatabase;

    private ServiceAdapter serviceAdapter;
    private List<Service> mService;
    Intent intent;
    String query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_service, container, false);

        intent = getActivity().getIntent();
        query = intent.getStringExtra("query");

        mServiceList = view.findViewById(R.id.list_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        mServiceList.setLayoutManager(mLayoutManager);

        mService = new ArrayList<>();

        searchService(query);

        return view;
    }

    public void searchService(final String query) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Service");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mService.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    if (service.getName().toLowerCase().contains(query.toLowerCase()) || service.getCategory().toLowerCase().contains(query.toLowerCase())) {
                        if (!service.isDeleted()) {
                            mService.add(service);
                        }
                    }
                }

                serviceAdapter = new ServiceAdapter(getContext(), mService);
                mServiceList.setAdapter(serviceAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
