package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EWalletActivity extends AppCompatActivity {

    TextView amount;
    Button depositBtn, withdrawBtn;
    DatabaseReference mDatabase, reference, getUser;
    String userId;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser, fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ewallet);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("E-Wallet");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        userId = mFirebaseAuth.getCurrentUser().getUid();

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        amount = findViewById(R.id.ewalletAmount);
        depositBtn = findViewById(R.id.depositbtn);
        withdrawBtn = findViewById(R.id.withdrawbtn);

        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                amount.setText(String.format("%.2f", Float.valueOf(user.getEwallet())));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        depositBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deposit();
            }
        });

        withdrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withdraw();
            }
        });
    }

    public void deposit(){
        final ProgressDialog pd = new ProgressDialog(EWalletActivity.this);
        pd.setMessage("Adding Money");
        pd.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(EWalletActivity.this);
        builder.setTitle("Add Fund");
        LinearLayout linearLayout = new LinearLayout(EWalletActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(EWalletActivity.this);
        //editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        //editText.setRawInputType((Configuration.KEYBOARD_12KEY));
        editText.setHint("Enter amount to be added");
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                String regexStr = "^\\d+(\\.\\d+)?$";
                //String regexStr = "^[0-9]*$";

                if(value.matches(regexStr)){
                    final DatabaseReference addFundRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    addFundRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            Float currentAmount = user.getEwallet();

                            Float totalAmount = Float.valueOf(value) + currentAmount;

                            DatabaseReference updateFund = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                            HashMap<String, Object> result = new HashMap<>();
                            result.put("ewallet", totalAmount);

                            updateFund.updateChildren(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    pd.dismiss();

                                    Toast.makeText(EWalletActivity.this, "Successfully added fund", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                } else{
                    pd.dismiss();
                    Toast.makeText(EWalletActivity.this, "Please enter numeric value only.", Toast.LENGTH_SHORT).show();
                    return;
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

    public void withdraw(){
        final ProgressDialog pd = new ProgressDialog(EWalletActivity.this);
        pd.setMessage("Withdrawing Money");
        pd.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(EWalletActivity.this);
        builder.setTitle("Withdraw Fund");
        LinearLayout linearLayout = new LinearLayout(EWalletActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(EWalletActivity.this);
        editText.setHint("Enter amount to withdraw");
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                String regexStr = "^\\d+(\\.\\d+)?$";
                //String regexStr = "^[0-9]*$";

                if(value.matches(regexStr)){
                    final DatabaseReference withdrawFundRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    withdrawFundRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            Float currentAmount = user.getEwallet();

                            if(Float.valueOf(value) <= currentAmount){
                                Float totalAmount = currentAmount - Float.valueOf(value);

                                DatabaseReference updateFund = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                                HashMap<String, Object> result = new HashMap<>();
                                result.put("ewallet", totalAmount);

                                updateFund.updateChildren(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        pd.dismiss();

                                        Toast.makeText(EWalletActivity.this, "Successfully withdraw fund", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else{
                                pd.dismiss();
                                Toast.makeText(EWalletActivity.this, "Insufficient fund", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                } else{
                    pd.dismiss();
                    Toast.makeText(EWalletActivity.this, "Please enter numeric value only.", Toast.LENGTH_SHORT).show();
                    return;
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
