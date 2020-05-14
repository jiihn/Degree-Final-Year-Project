package com.example.fyp.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fyp.Adapter.UserAdapter;
import com.example.fyp.Model.User;
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

public class SearchUserFragment extends Fragment {

    private RecyclerView mUserList;
    private DatabaseReference mDatabase;

    private UserAdapter userAdapter;
    private List<User> mUser;
    Intent intent;
    FirebaseUser fuser;
    String query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_user, container, false);

        intent = getActivity().getIntent();
        query = intent.getStringExtra("query");

        mUserList = view.findViewById(R.id.list_view);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        mUser = new ArrayList<>();

        searchUser(query);

        return view;
    }

    public void searchUser(final String query) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getFirst_name().toLowerCase().contains(query.toLowerCase()) || user.getLast_name().toLowerCase().contains(query.toLowerCase())) {
                        if(!user.getId().equals(fuser.getUid())) {
                            mUser.add(user);
                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(), mUser, true);
                mUserList.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
