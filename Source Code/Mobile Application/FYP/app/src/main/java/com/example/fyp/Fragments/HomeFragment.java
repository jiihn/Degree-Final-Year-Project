package com.example.fyp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.fyp.Adapter.ServiceAdapter;
import com.example.fyp.Model.Service;
import com.example.fyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView mServiceList;
    private DatabaseReference mDatabase;

    private ServiceAdapter serviceAdapter;
    private List<Service> mService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home,container,false);

        mServiceList = view.findViewById(R.id.recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        mServiceList.setLayoutManager(mLayoutManager);

        mService = new ArrayList<Service>();

        readServices();

        return view;
    }

    private void readServices(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Service");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mService.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Service service = snapshot.getValue(Service.class);
                    if(!service.isDeleted()) {
                        mService.add(service);
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
