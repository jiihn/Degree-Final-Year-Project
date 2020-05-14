package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fyp.Adapter.ServiceAdapter;
import com.example.fyp.Fragments.CategoryFragment;
import com.example.fyp.Fragments.ChatFragment;
import com.example.fyp.Fragments.HomeFragment;
import com.example.fyp.Model.Chat;
import com.example.fyp.Model.Order;
import com.example.fyp.Model.Orderlist;
import com.example.fyp.Model.Service;
import com.example.fyp.Model.User;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomepageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    CircleImageView profile_image;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser;
    private DatabaseReference getUser, reference, serviceRef, orderRef, purchaseRef;
    private String userId;
    private TextView firstName, NavLastName, NavFirstName, email, ewallet;
    private ImageView NavVerified;
    MenuItem myProject, purchaseList, orderList;
    TextView chat;
    MaterialSearchView searchView;
    long serviceCount;

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homepage);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(HomepageActivity.this);
        Menu menu = navigationView.getMenu();
        myProject = menu.findItem(R.id.my_project);
        purchaseList = menu.findItem(R.id.purchase);
        orderList = menu.findItem(R.id.order);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    Toast.makeText(HomepageActivity.this, "Please login", Toast.LENGTH_SHORT);
                    Intent i = new Intent(HomepageActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        };

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        searchView = findViewById(R.id.search_view);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        mFirebaseAuth = FirebaseAuth.getInstance();
        userId = mFirebaseAuth.getCurrentUser().getUid();
        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        firstName = findViewById(R.id.fnameData);
        NavLastName = hView.findViewById(R.id.lname);
        NavFirstName = hView.findViewById(R.id.fname);
        NavVerified = hView.findViewById(R.id.verifiedLogo);
        email = hView.findViewById(R.id.emailAdd);
        ewallet = hView.findViewById(R.id.ewalletTxt);
        profile_image = hView.findViewById(R.id.profile_picture);


        getUser.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user.isVerified() == true) {
                        NavVerified.setVisibility(View.VISIBLE);
                    } else {
                        NavVerified.setVisibility(View.GONE);
                    }

                    if(user.isRejected() == true){
                        if (!HomepageActivity.this.isFinishing()){
                            showNotification();
                        }
                    }
                    firstName.setText(user.getFirst_name());
                    NavFirstName.setText(user.getFirst_name());
                    NavLastName.setText(user.getLast_name());
                    email.setText(user.getEmail());
                    ewallet.setText(String.format("%.2f", Float.valueOf(user.getEwallet())));

                    if (user.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                    }
                } else {
                    Toast.makeText(HomepageActivity.this, "No user details found. Please fill your details", Toast.LENGTH_LONG).show();
                    Intent intToInsertDetails = new Intent(HomepageActivity.this, InsertDetailsActivity.class);
                    startActivity(intToInsertDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                        unread++;
                    }
                }

                viewPagerAdapter.addFragment(new HomeFragment(), "Home");
                viewPagerAdapter.addFragment(new CategoryFragment(), "Category");

                if (unread == 0) {
                    viewPagerAdapter.addFragment(new ChatFragment(), "Chat");
                } else {
                    viewPagerAdapter.addFragment(new ChatFragment(), "(" + unread + ") Chat");
                }


                viewPager.setAdapter(viewPagerAdapter);

                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        serviceRef = FirebaseDatabase.getInstance().getReference("Servicelist").child(userId);

        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                serviceCount = dataSnapshot.getChildrenCount();

                myProject.setTitle("My Project (" + serviceCount + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        purchaseRef = FirebaseDatabase.getInstance().getReference("Purchaselist").child(userId);

        purchaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long purchaseListCount = dataSnapshot.getChildrenCount();

                purchaseList.setTitle("Purchase (" + purchaseListCount + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        orderRef = FirebaseDatabase.getInstance().getReference("Orderlist").child(userId);

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long orderListCount = dataSnapshot.getChildrenCount();

                orderList.setTitle("Order (" + orderListCount + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intToProf = new Intent(HomepageActivity.this, ProfileActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToProf);
            }
        });
    }

    public void showNotification(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomepageActivity.this);
        builder.setMessage("Your verification application has been rejected by the admin please re-verify your identity")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clearNotification();
                        Intent intent = new Intent(HomepageActivity.this, VerificationActivity.class);
                        startActivity(intent);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearNotification();
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    public void clearNotification(){
        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        HashMap<String, Object> update = new HashMap<>();
        update.put("rejected", false);

        updateRef.updateChildren(update);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.home:
                Intent intToHome = new Intent(HomepageActivity.this, HomepageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToHome);
                break;
            case R.id.viewProfile:
                Intent intToViewProf = new Intent(HomepageActivity.this, ViewProfileActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intToViewProf.putExtra("id", firebaseUser.getUid());
                startActivity(intToViewProf);
                break;
            case R.id.verification:
                Intent intoToVerification = new Intent(HomepageActivity.this, VerificationActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intoToVerification);
                break;
            case R.id.ewallet:
                Intent intToEWallet = new Intent(HomepageActivity.this, EWalletActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToEWallet);
                break;
            case R.id.add_project:
                Intent intToProj = new Intent(HomepageActivity.this, AddProjectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToProj);
                break;
            case R.id.my_project:
                Intent intToMyProj = new Intent(HomepageActivity.this, MyProjectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToMyProj);
                break;
            case R.id.profile:
                Intent intToProf = new Intent(HomepageActivity.this, ProfileActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToProf);
                break;
            case R.id.purchase:
                Intent intToPurc = new Intent(HomepageActivity.this, PurchaseActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToPurc);
                break;
            case R.id.order:
                Intent intToOrder = new Intent(HomepageActivity.this, OrderActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToOrder);
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intToMain = new Intent(HomepageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intToMain);
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        if (searchView != null) {
            searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Intent intSearch = new Intent(HomepageActivity.this, SearchResultActivity.class);
                    intSearch.putExtra("query", query);
                    startActivity(intSearch);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }

    /*private void search(String str){
        ArrayList<Service> list = new ArrayList<>();
        for(Service object : list){
            if(object.getName().toLowerCase().contains(str.toLowerCase()))
            {
                list.add(object);
            }
        }

        ServiceAdapter serviceAdapter = new ServiceAdapter(list);
        recyclerView
    }
*/
    public void status(final String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
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
