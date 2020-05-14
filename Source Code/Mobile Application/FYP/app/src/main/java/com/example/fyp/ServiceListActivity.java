package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.Adapter.CategoryAdapter;
import com.example.fyp.Adapter.ServiceAdapter;
import com.example.fyp.Model.Category;
import com.example.fyp.Model.Categorylist;
import com.example.fyp.Model.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceListActivity extends AppCompatActivity {

    Intent intent;
    String categoryListId, currentUserId;
    DatabaseReference categoryListRef, mDatabase, titleRef, getUser, reference;
    TextView resulttxt;
    FirebaseUser fuser;

    private ServiceAdapter serviceAdapter;

    private List<Service> mService;
    private List<Categorylist> mList;
    RecyclerView serviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);
        serviceList = findViewById(R.id.list_view);
        serviceList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = fuser.getUid();

        resulttxt = findViewById(R.id.resultTxt);

        intent = getIntent();
        categoryListId = intent.getStringExtra("id");

        mList = new ArrayList<>();

        titleRef = FirebaseDatabase.getInstance().getReference("Category").child(categoryListId);

        titleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                getSupportActionBar().setTitle(category.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        categoryListRef = FirebaseDatabase.getInstance().getReference("Categorylist").child(categoryListId);

        categoryListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mList.clear();

                if(dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Categorylist categorylist = snapshot.getValue(Categorylist.class);
                        mList.add(categorylist);
                    }

                    readServices();
                } else{
                    //Toast.makeText(getApplicationContext(), "No Service Found", Toast.LENGTH_SHORT).show();
                    resulttxt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void readServices(){
        mService = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("Service");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mService.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Service service = snapshot.getValue(Service.class);

                        for (Categorylist categorylist : mList) {
                            if (service.getId().equals(categorylist.getServiceId())) {
                                mService.add(service);
                            }
                        }
                }

                serviceAdapter = new ServiceAdapter(getApplicationContext(), mService);
                serviceList.setAdapter(serviceAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void status(final String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        getUser.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("status", status);

                    reference.updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
