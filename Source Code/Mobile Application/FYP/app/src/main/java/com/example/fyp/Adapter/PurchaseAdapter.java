package com.example.fyp.Adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.fyp.Model.JavaMailAPI;
import com.example.fyp.Model.Order;

import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.Model.Service;
import com.example.fyp.Model.User;
import com.example.fyp.R;
import com.example.fyp.ServiceDetailsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {

    private Context mContext;
    List<Order> mOrder;
    private DatabaseReference mDatabase;
    String sellerEmail, title, fname, lname, servicePrice;
    String userId;
    String duration;
    float calDuration, convertDuration;
    FirebaseUser fuser;

    public PurchaseAdapter(Context mContext, List<Order> mOrder) {
        this.mContext = mContext;
        this.mOrder = mOrder;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.purchaselistlayout, parent, false);
        ViewHolder holder = new ViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Order order = mOrder.get(position);
        String paymentStatus;
        final String serId = order.getServiceId();
        mDatabase = FirebaseDatabase.getInstance().getReference("Service").child(serId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Service service = dataSnapshot.getValue(Service.class);
                userId = service.getBy();
                title = service.getName();
                servicePrice = service.getPrice();
                holder.name.setText(service.getName());
                holder.price.setText(service.getPrice());
                Glide.with(mContext).load(service.getImageURL()).into(holder.imageURL);
                duration = service.getDuration();
                convertDuration = Float.valueOf(duration);
                calDuration = (convertDuration * 86400000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (order.isPaid() == true) {
            paymentStatus = "PAID";
        } else {
            paymentStatus = "PENDING";
        }

        String timestamp = order.getTimestamp();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        holder.payment.setText(paymentStatus);
        holder.date.setText(dateTime);

        if (order.isCancel() == true) {
            holder.cancel.setVisibility(View.GONE);
            holder.pay.setVisibility(View.GONE);
            holder.cancelled.setVisibility(View.VISIBLE);
        }

        holder.pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderId = order.getOrderId();
                payOrder(orderId);
            }
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderId = order.getOrderId();
                cancelOrder(orderId);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*mDatabase = FirebaseDatabase.getInstance().getReference("Order");
                String id = order.getOrderId();
                Toast.makeText(mContext, id, Toast.LENGTH_SHORT).show();*/
                Intent intent = new Intent(mContext, ServiceDetailsActivity.class);
                intent.putExtra("id", serId);
                mContext.startActivity(intent);
            }
        });
    }

    public void payOrder(final String orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage("Make Payment?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        DatabaseReference getUserRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);
                        getUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final Order order = dataSnapshot.getValue(Order.class);

                                DatabaseReference checkFundRef = FirebaseDatabase.getInstance().getReference("Users").child(order.getBuyer());

                                checkFundRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);

                                        Float ewallet = user.getEwallet();

                                        if (ewallet < Float.valueOf(servicePrice)) {
                                            Toast.makeText(mContext, "Insufficient fund, please top up", Toast.LENGTH_SHORT).show();
                                            dialog.cancel();
                                            return;
                                        } else {
                                            Float remainingAmount = ewallet - Float.valueOf(servicePrice);

                                            DatabaseReference updateFundRef = FirebaseDatabase.getInstance().getReference("Users").child(order.getBuyer());

                                            HashMap<String, Object> fundMap = new HashMap<>();
                                            fundMap.put("ewallet", remainingAmount);

                                            updateFundRef.updateChildren(fundMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    DatabaseReference payRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);
                                                    final String timestamp = String.valueOf(System.currentTimeMillis());
                                                    float dueDuration = calDuration + Float.valueOf(timestamp);
                                                    String result = String.format("%.0f", Float.parseFloat(String.valueOf(dueDuration)));


                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("paid", true);
                                                    hashMap.put("paidTimestamp", timestamp);
                                                    hashMap.put("completeTimestamp", "0");
                                                    hashMap.put("acceptTimestamp", "0");
                                                    hashMap.put("dueTimestamp", result);

                                                    payRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                                                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    User user = dataSnapshot.getValue(User.class);

                                                                    sellerEmail = user.getEmail();
                                                                    fname = user.getFirst_name();
                                                                    lname = user.getLast_name();

                                                                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                                                    cal.setTimeInMillis(Long.parseLong(timestamp));
                                                                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                                                                    String mail = sellerEmail;
                                                                    String subject = "Order for " + title;
                                                                    String message = "Dear " + fname + " " + lname + ","
                                                                            + System.getProperty("line.separator")
                                                                            + System.getProperty("line.separator")
                                                                            + "You have an order for " + title + " and the order is paid on " + dateTime + ". Please review the order. Thank you"
                                                                            + System.getProperty("line.separator")
                                                                            + System.getProperty("line.separator")
                                                                            + "Freelancer Mobile Application";

                                                                    JavaMailAPI javaMailAPI = new JavaMailAPI(mContext, mail, subject, message);

                                                                    javaMailAPI.execute();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        dialog.cancel();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    public void cancelOrder(final String orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage("Cancel order?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference cancelOrderRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("cancel", true);

                        cancelOrderRef.updateChildren(hashMap);
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

    @Override
    public int getItemCount() {
        return mOrder.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView date;
        public TextView payment;
        public TextView price;
        public TextView cancelled;
        public ImageView imageURL;
        public Button cancel;
        public Button pay;

        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            name = itemView.findViewById(R.id.serviceName);
            date = itemView.findViewById(R.id.serviceDate);
            payment = itemView.findViewById(R.id.servicePayment);
            price = itemView.findViewById(R.id.servicePrice);
            imageURL = itemView.findViewById(R.id.serviceImage);
            cancel = itemView.findViewById(R.id.cancelBtn);
            pay = itemView.findViewById(R.id.payBtn);
            cancelled = itemView.findViewById(R.id.cancelledtext);
        }
    }
}
