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

import com.example.fyp.Adapter.CategoryAdapter;
import com.example.fyp.Model.Category;
import com.example.fyp.Model.Categorylist;
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

public class CategoryFragment extends Fragment {

    private CategoryAdapter categoryAdapter;

    private List<Category> mCategory;
    private List<Categorylist> mList;
    RecyclerView categoryList;

    DatabaseReference mDatabase, reference;
       @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
           View view = inflater.inflate(R.layout.fragment_category, container, false);

           categoryList = view.findViewById(R.id.list_view);
           LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
           mLayoutManager.setStackFromEnd(true);
           mLayoutManager.setReverseLayout(true);
           categoryList.setLayoutManager(mLayoutManager);

           readCategory();

           return view;
    }

    public void readCategory(){
        mCategory = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("Category");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mCategory.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Category category = snapshot.getValue(Category.class);
                    mCategory.add(category);
                }

                categoryAdapter = new CategoryAdapter(getContext(), mCategory);
                categoryList.setAdapter(categoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
