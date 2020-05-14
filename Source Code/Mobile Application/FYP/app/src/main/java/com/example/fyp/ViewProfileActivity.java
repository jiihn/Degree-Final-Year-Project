package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.fyp.Adapter.RatingAdapter;
import com.example.fyp.Model.Rating;
import com.example.fyp.Model.Ratinglist;
import com.example.fyp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    CircleImageView image_profile;
    TextView first_name, last_name;
    TextView city, state, phoneNum, overallRating, slashFive;
    ImageButton chatBtn;

    DatabaseReference reference, ratingRef, getUser;

    StorageReference storageReference;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser fuser;

    Intent intent;

    String userid, userId;

    private RatingAdapter ratingAdapter;

    private List<Rating> mRating;
    RecyclerView recyclerView;
    private List<Ratinglist> ratingLists;

    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image_profile = findViewById(R.id.profile_image);
        first_name = findViewById(R.id.fname);
        last_name = findViewById(R.id.lname);
        city = findViewById(R.id.city_txt);
        state = findViewById(R.id.state_txt);
        phoneNum = findViewById(R.id.phoneNum_txt);
        chatBtn = findViewById(R.id.chatBtn);
        overallRating = findViewById(R.id.overallRating);
        slashFive = findViewById(R.id.slashFive);

        intent = getIntent();
        userid = intent.getStringExtra("id");

        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        mFirebaseAuth = FirebaseAuth.getInstance();
        userId = mFirebaseAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference("profilePicture").child(userid);
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                first_name.setText(user.getFirst_name());
                last_name.setText(user.getLast_name());
                city.setText(user.getCity());
                state.setText(user.getState());
                phoneNum.setText(user.getPhone_number());

                if (user.getImageURL().equals("default")) {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView = findViewById(R.id.list_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(ViewProfileActivity.this);
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(mLayoutManager);

        ratingLists = new ArrayList<>();

        ratingRef = FirebaseDatabase.getInstance().getReference("Ratinglist").child(userid);

        ratingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ratingLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ratinglist ratinglist = snapshot.getValue(Ratinglist.class);
                    ratingLists.add(ratinglist);
                }

                readRating();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(userid.equals(fuser.getUid())){
            chatBtn.setVisibility(View.GONE);
        } else{
            chatBtn.setVisibility(View.VISIBLE);
        }

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this, ChatActivity.class);
                intent.putExtra("id", userid);
                startActivity(intent);
            }
        });
    }

    public void readRating(){
        mRating = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("Rating");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRating.clear();
                float total=0, count=0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Rating rating = snapshot.getValue(Rating.class);

                    for(Ratinglist ratinglist : ratingLists) {
                        if(rating.getRatingId().equals(ratinglist.getId())) {
                            mRating.add(rating);

                            total += rating.getRating();
                            count += 1;
                        }
                    }
                }

                ratingAdapter = new RatingAdapter(ViewProfileActivity.this, mRating);
                recyclerView.setAdapter(ratingAdapter);

                float totalRating = Float.valueOf(total/count);

                if(count == 0){
                    overallRating.setText("No Rating");
                    slashFive.setText("");
                }else {
                    overallRating.setText(Float.toString(totalRating));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void status(final String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        getUser.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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

