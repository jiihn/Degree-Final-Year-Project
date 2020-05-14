package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.Model.Category;
import com.example.fyp.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AddProjectActivity extends AppCompatActivity {

    private DatabaseReference databaseReference, reference, getUser;
    FirebaseDatabase database;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser fuser, firebaseUser;

    private Spinner categorySpinner;
    private String selectedCategory;
    private TextView serviceName, priceText, descriptionText;
    private Button confirmbtn, choosebtn;
    private StorageReference mStorageRef;
    private ImageView img;
    private StorageTask uploadTask;
    ProgressBar progressBar;

    public Uri imguri;

    String currentUserId;

    private String byCurrentDate;
    private String byCurrentTime;
    Spinner duration;
    String selectedDuration;
    String cId;

    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Project");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if(user.isVerified() == false){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddProjectActivity.this);
                    builder.setMessage("You are not a verified user. Please verify you account to add a project")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(AddProjectActivity.this, VerificationActivity.class);
                                    startActivity(intent);
                                }
                            });
                    builder.create().show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        serviceName = findViewById(R.id.serviceName);
        priceText = findViewById(R.id.priceText);
        descriptionText = findViewById(R.id.descriptionText);
        confirmbtn = findViewById(R.id.confirmbtn);
        choosebtn = findViewById(R.id.choosebtn);
        img=findViewById(R.id.upload_image);
        progressBar = findViewById(R.id.progressBar);
        duration = findViewById(R.id.durationSpinner);
        progressBar.setVisibility(View.GONE);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service");
        key = databaseReference.push().getKey();

        choosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Filechooser();
            }
        });

        addItemOnCategorySpinner();
        addItemOnDurationSpinner();

        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertService();
            }
        });
    }

    public String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    public void Filechooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imguri=data.getData();
            img.setImageURI(imguri);
        }
    }

    public void insertService(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service");
        mStorageRef = FirebaseStorage.getInstance().getReference("ServiceImages").child(key);

        long currentTimestamp = System.currentTimeMillis();
        final String timestamp = String.valueOf(currentTimestamp);

        final String service = serviceName.getText().toString().trim();
        final String price = priceText.getText().toString().trim();
        final String description = descriptionText.getText().toString().trim();
        final String getDuration = duration.getSelectedItem().toString().trim();

        if(TextUtils.isEmpty(service)){
            serviceName.setError("Please enter service name");
            serviceName.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(price)){
            priceText.setError("Please enter a price");
            priceText.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(description)){
            descriptionText.setError("Please enter description");
            descriptionText.requestFocus();
            return;
        }

        if(getDuration == "Select a duration")
        {
            duration.performClick();
            Toast.makeText(AddProjectActivity.this, "Please select a duration", Toast.LENGTH_LONG).show();
            return;
        }

        if(imguri != null){
            Toast.makeText(AddProjectActivity.this, "Uploading", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);

            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    +"."+getExtension(imguri));

            uploadTask = fileReference.putFile(imguri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        HashMap userMap = new HashMap();
                        userMap.put("name",service);
                        userMap.put("price",price);
                        userMap.put("category",cId);
                        userMap.put("description",description);
                        userMap.put("timestamp",timestamp);
                        userMap.put("by",currentUserId);
                        userMap.put("id",key);
                        userMap.put("duration",getDuration);
                        userMap.put("imageURL", mUri);
                        userMap.put("deleted", false);
                        userMap.put("available", true);

                        databaseReference.child(key).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(AddProjectActivity.this, "Upload complete!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);

                                    final DatabaseReference serviceRef = FirebaseDatabase.getInstance().getReference("Servicelist").child(currentUserId).child(key);

                                    serviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.exists()){
                                                serviceRef.child("id").setValue(key);

                                                DatabaseReference catRef = FirebaseDatabase.getInstance().getReference().child("Categorylist").child(cId).child(key);

                                                HashMap catMap = new HashMap();
                                                catMap.put("serviceId",key);

                                                catRef.setValue(catMap);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    Intent intent = new Intent(AddProjectActivity.this,HomepageActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Toast.makeText(AddProjectActivity.this, "Error occured, please ensure that you have entered service information correctly",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(AddProjectActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddProjectActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(AddProjectActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void addItemOnDurationSpinner(){
        final String durationList[] = {"Select a duration", "1", "2", "3", "4", "5","6", "7", "8", "9", "10", "11", "12", "13", "14", "13", "14",
                                        "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", };

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, durationList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duration.setAdapter(stateAdapter);

        duration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedDuration = durationList[position];

                return;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void addItemOnCategorySpinner(){
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        databaseReference.child("Category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<String> categoryList = new ArrayList<>();

                for (DataSnapshot categorySnapshot: dataSnapshot.getChildren()){
                    Category getCategory = categorySnapshot.getValue(Category.class);
                    String category = getCategory.getName();

                    if(category != null){
                        categoryList.add(category);
                    }
                }

                categorySpinner = findViewById(R.id.categorySpinner);
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(AddProjectActivity.this, android.R.layout.simple_spinner_item, categoryList);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(categoryAdapter);

                categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                        DatabaseReference getCatIdRef = FirebaseDatabase.getInstance().getReference("Category");

                        Query query = getCatIdRef.limitToFirst(position+1);

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot getId : dataSnapshot.getChildren()){
                                    Category category = getId.getValue(Category.class);

                                    cId = category.getId();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void status(final String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
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
