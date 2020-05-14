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

import com.example.fyp.Adapter.CategoryAdapter;
import com.example.fyp.Model.Category;
import com.example.fyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchCategoryFragment extends Fragment {

    private RecyclerView mCategoryList;
    private DatabaseReference mDatabase;

    private CategoryAdapter categoryAdapter;
    private List<Category> mCategory;

    Intent intent;
    String query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_category, container, false);

        intent = getActivity().getIntent();
        query = intent.getStringExtra("query");

        mCategoryList = view.findViewById(R.id.list_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        mCategoryList.setLayoutManager(mLayoutManager);

        mCategory = new ArrayList<>();

        searchCategory(query);

        return view;
    }

    public void searchCategory(final String query){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Category");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mCategory.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category.getName().toLowerCase().contains(query.toLowerCase())) {
                        mCategory.add(category);
                    }
                }

                categoryAdapter = new CategoryAdapter(getContext(), mCategory);
                mCategoryList.setAdapter(categoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
