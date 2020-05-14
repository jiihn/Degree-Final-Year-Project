package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fyp.Model.Service;
import com.example.fyp.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ServiceDetailsActivity extends AppCompatActivity {

    DatabaseReference userRef, buyerRef, reference, orderRef, orderListRef, purchaseListRef, getUser;
    DatabaseReference delReference, delServiceList;
    DatabaseReference user;
    FirebaseUser fuser;

    Intent intent;
    String serviceId;

    ImageView image;
    TextView desc;
    TextView price;
    TextView posted_first;
    TextView posted_last;
    TextView posted_duration;
    TextView posted_time;
    TextView posted_available;
    Button chat, edit;
    String userId, categoryId;
    Button deletebtn, orderbtn;
    FirebaseAuth mFirebaseAuth;
    String currentUserId;
    String sellerEmail, buyerEmail, title, fname, lname, buyerFName, buyerLName;
    LinearLayout poster_name;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_details);

        image = findViewById(R.id.post_image);
        desc = findViewById(R.id.post_desc);
        price = findViewById(R.id.post_price);
        chat = findViewById(R.id.chatBtn);
        edit = findViewById(R.id.editBtn);
        deletebtn = findViewById(R.id.deleteBtn);
        orderbtn = findViewById(R.id.orderBtn);
        posted_first = findViewById(R.id.post_byFirst);
        posted_last = findViewById(R.id.post_byLast);
        posted_time = findViewById(R.id.post_time);
        posted_duration = findViewById(R.id.post_duration);
        poster_name = findViewById(R.id.poster_name);
        posted_available = findViewById(R.id.post_available);

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        buyerRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        buyerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                buyerEmail = user.getEmail();
                buyerFName = user.getFirst_name();
                buyerLName = user.getLast_name();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        intent = getIntent();
        serviceId = intent.getStringExtra("id");

        storageReference = FirebaseStorage.getInstance().getReference("ServiceImages").child(serviceId);
        reference = FirebaseDatabase.getInstance().getReference("Service").child(serviceId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Service service = dataSnapshot.getValue(Service.class);

                title = service.getName();

                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setTitle(title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                userId = service.getBy();
                categoryId = service.getCategory();

                userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        posted_first.setText(user.getFirst_name());
                        posted_last.setText(user.getLast_name());
                        fname = user.getFirst_name();
                        lname = user.getLast_name();
                        sellerEmail = user.getEmail();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(Long.parseLong(service.getTimestamp()));
                String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                posted_time.setText(dateTime);

                posted_duration.setText(String.valueOf(service.getDuration()));

                desc.setText(service.getDescription());
                price.setText(service.getPrice());
                Glide.with(getApplicationContext()).load(service.getImageURL()).into(image);

                if (currentUserId.equals(userId)) {
                    orderbtn.setVisibility(View.GONE);
                    deletebtn.setVisibility(View.VISIBLE);
                    chat.setVisibility(View.GONE);
                    edit.setVisibility(View.VISIBLE);
                    posted_available.setText("Available");

                    if (service.isDeleted()) {
                        deletebtn.setVisibility(View.GONE);
                        edit.setVisibility(View.GONE);
                        posted_available.setText("Not Available");
                    }

                    if (!service.isAvailable()) {
                        posted_available.setText("Not Available");
                    }
                } else {
                    orderbtn.setVisibility(View.VISIBLE);
                    deletebtn.setVisibility(View.GONE);
                    chat.setVisibility(View.VISIBLE);
                    edit.setVisibility(View.GONE);
                    posted_available.setText("Available");

                    if (service.isDeleted()) {
                        orderbtn.setVisibility(View.GONE);
                        chat.setVisibility(View.GONE);
                        posted_available.setText("Not Available");
                    }

                    if (!service.isAvailable()) {
                        posted_available.setText("Not Available");
                        chat.setVisibility(View.GONE);
                        orderbtn.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        poster_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToProfile = new Intent(ServiceDetailsActivity.this, ViewProfileActivity.class);
                intentToProfile.putExtra("id", userId);
                startActivity(intentToProfile);
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServiceDetailsActivity.this, ChatActivity.class);
                intent.putExtra("id", userId);
                startActivity(intent);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditServiceDialog();
            }
        });

        orderbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeOrder();
            }
        });

        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteService(serviceId, categoryId);
            }
        });
    }

    public void showEditServiceDialog() {
        String options[] = {"Edit Picture", "Edit Service Name", "Edit Description", "Edit Duration", "Edit Price", "Edit Availability"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ServiceDetailsActivity.this);

        builder.setTitle("Choose Action");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openImage();
                } else if (which == 1) {
                    showEditDialog("name", "Service Name");
                } else if (which == 2) {
                    showEditDialog("description", "Service Description");
                } else if (which == 3) {
                    showEditDialog("duration", "Service Duration");
                } else if (which == 4) {
                    showEditDialog("price", "Service Price");
                } else if (which == 5) {
                    editAvailability("available");
                }
            }
        });

        builder.create().show();
    }

    public void editAvailability(final String key) {
        final ProgressDialog pd = new ProgressDialog(ServiceDetailsActivity.this);
        String options[] = {"Available", "Not Available"};
        pd.setMessage("Changing status");
        pd.show();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ServiceDetailsActivity.this);

        builder.setTitle("Select Availability");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, true);

                    reference.updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(ServiceDetailsActivity.this, "Service set to available", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(ServiceDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else if (which == 1) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, false);

                    reference.updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(ServiceDetailsActivity.this, "Service set to not available", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(ServiceDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        builder.create().show();
    }

    public void showEditDialog(final String key, final String title) {
        final ProgressDialog pd = new ProgressDialog(ServiceDetailsActivity.this);
        pd.setMessage("Uploading");
        pd.show();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ServiceDetailsActivity.this);
        builder.setTitle("Update " + title);
        LinearLayout linearLayout = new LinearLayout(ServiceDetailsActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(ServiceDetailsActivity.this);
        editText.setHint("Enter " + title);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setCancelable(false);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();

                if (!TextUtils.isEmpty(value)) {
                    if (key == "price") {
                        String regexStr = "^[0-9]*$";
                        if (!value.matches(regexStr)) {
                            pd.dismiss();
                            Toast.makeText(ServiceDetailsActivity.this, "Please enter number only for " + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        pd.show();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put(key, value);

                        reference.updateChildren(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(ServiceDetailsActivity.this, "Edit Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(ServiceDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else if (key == "duration") {
                        String regexStr = "^[0-9]*$";
                        if (!value.matches(regexStr)) {
                            pd.dismiss();
                            Toast.makeText(ServiceDetailsActivity.this, "Please enter number only for " + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (Integer.parseInt(value) > 30) {
                            pd.dismiss();
                            Toast.makeText(ServiceDetailsActivity.this, "Please enter no more than 30 days for " + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        pd.show();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put(key, value);

                        reference.updateChildren(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(ServiceDetailsActivity.this, "Edit Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(ServiceDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        pd.show();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put(key, value);

                        reference.updateChildren(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(ServiceDetailsActivity.this, "Edit Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(ServiceDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    pd.dismiss();
                    Toast.makeText(ServiceDetailsActivity.this, "Edit Fail, Please Enter " + title, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.dismiss();
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = ServiceDetailsActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadimage() {
        final ProgressDialog pd = new ProgressDialog(ServiceDetailsActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Service").child(serviceId);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(ServiceDetailsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ServiceDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(ServiceDetailsActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ServiceDetailsActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadimage();
            }
        }
    }

    private void deleteService(final String serviceId, final String cId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceDetailsActivity.this);

        builder.setMessage("Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        delServiceList = FirebaseDatabase.getInstance().getReference("Servicelist").child(currentUserId).child(serviceId);
                        delServiceList.removeValue();

                        DatabaseReference delFromCat = FirebaseDatabase.getInstance().getReference("Categorylist").child(cId).child(serviceId);
                        delFromCat.removeValue();

                        delReference = FirebaseDatabase.getInstance().getReference("Service").child(serviceId);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("deleted", true);

                        delReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ServiceDetailsActivity.this, "Delete complete", Toast.LENGTH_SHORT).show();

                                Intent intToHome = new Intent(ServiceDetailsActivity.this, HomepageActivity.class);
                                startActivity(intToHome);
                                finish();
                            }
                        });
                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void makeOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceDetailsActivity.this);

        builder.setMessage("Make order?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        orderRef = FirebaseDatabase.getInstance().getReference().child("Order");
                        final String key = orderRef.push().getKey();

                        final String timestamp = String.valueOf(System.currentTimeMillis());

                        HashMap<String, Object> orderMap = new HashMap<>();

                        orderMap.put("buyer", currentUserId);
                        orderMap.put("seller", userId);
                        orderMap.put("timestamp", timestamp);
                        orderMap.put("serviceId", serviceId);
                        orderMap.put("orderId", key);
                        orderMap.put("paid", false);
                        orderMap.put("cancel", false);
                        orderMap.put("transfer", false);
                        orderMap.put("complete", false);
                        orderMap.put("verified", false);
                        orderMap.put("rated", false);

                        orderRef.child(key).setValue(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                purchaseListRef = FirebaseDatabase.getInstance().getReference().child("Purchaselist").child(currentUserId).child(key);

                                purchaseListRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()) {
                                            purchaseListRef.child("id").setValue(key);
                                            purchaseListRef.child("timestamp").setValue(timestamp);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                orderListRef = FirebaseDatabase.getInstance().getReference().child("Orderlist").child(userId).child(key);

                                orderListRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()) {
                                            orderListRef.child("id").setValue(key);
                                            orderListRef.child("timestamp").setValue(timestamp);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                Toast.makeText(ServiceDetailsActivity.this, "Order Complete", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ServiceDetailsActivity.this, PurchaseActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
