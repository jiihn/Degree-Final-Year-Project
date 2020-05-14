package com.example.fyp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.renderscript.Sampler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fyp.Model.JavaMailAPI;
import com.example.fyp.Model.Order;
import com.example.fyp.Model.Service;
import com.example.fyp.Model.User;
import com.example.fyp.R;
import com.example.fyp.ServiceDetailsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderPaidAdapter extends RecyclerView.Adapter<OrderPaidAdapter.ViewHolder>{

    private Context mContext;
    List<Order> mOrder;
    private DatabaseReference mDatabase;
    String buyerEmail, title, fname, lname;
    String userId;

    public OrderPaidAdapter(Context mContext, List<Order> mOrder) {
        this.mContext = mContext;
        this.mOrder = mOrder;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.orderpaidlayout, parent, false);
        OrderPaidAdapter.ViewHolder holder = new OrderPaidAdapter.ViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Order order = mOrder.get(position);
        final String serId = order.getServiceId();
        mDatabase = FirebaseDatabase.getInstance().getReference("Service").child(serId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Service service = dataSnapshot.getValue(Service.class);
                title = service.getName();
                holder.name.setText(service.getName());
                Glide.with(mContext).load(service.getImageURL()).into(holder.imageURL);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(order.isCancel() == true){
            holder.complete.setVisibility(View.GONE);
            holder.statusTxt.setVisibility(View.VISIBLE);
            holder.statusTxt.setText("CANCELLED");
        } else {
            holder.complete.setVisibility(View.VISIBLE);
            holder.statusTxt.setVisibility(View.GONE);
        }

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);

        String paidTimestamp = order.getPaidTimestamp();
        cal.setTimeInMillis(Long.parseLong(paidTimestamp));
        String paidDate = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        holder.paidDate.setText(paidDate);

        String dueTimestamp = order.getDueTimestamp();
        cal.setTimeInMillis(Long.parseLong(dueTimestamp));
        String dueDate = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        holder.dueDate.setText(dueDate);

        holder.complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, "COMPLETE", Toast.LENGTH_SHORT).show();
                completeOrder(order.getOrderId());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*mDatabase = FirebaseDatabase.getInstance().getReference("Order");
                String id = order.getOrderId();
                Toast.makeText(mContext, id, Toast.LENGTH_SHORT).show();*/
                Intent intent = new Intent (mContext, ServiceDetailsActivity.class);
                intent.putExtra("id", serId);
                mContext.startActivity(intent);
            }
        });
    }

    public void completeOrder(final String orderId){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage("Complete Order?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference completeRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);
                        long currentTimestamp = System.currentTimeMillis();
                        final String timestamp = String.valueOf(currentTimestamp);
                        final String acceptTimestamp =  String.valueOf(currentTimestamp+259200000);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("complete", true);
                        hashMap.put("rated", false);
                        hashMap.put("verified", false);
                        hashMap.put("completeTimestamp", timestamp);
                        hashMap.put("acceptTimestamp", acceptTimestamp);

                        completeRef.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                DatabaseReference OrderReference = FirebaseDatabase.getInstance().getReference("Order").child(orderId);

                                OrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Order order = dataSnapshot.getValue(Order.class);
                                        userId = order.getBuyer();

                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);

                                                buyerEmail = user.getEmail();
                                                fname = user.getFirst_name();
                                                lname = user.getLast_name();

                                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                                cal.setTimeInMillis(Long.parseLong(timestamp));
                                                String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                                                String mail = buyerEmail;
                                                String subject = "Order for " +title;
                                                String message = "Dear " +fname+ " " +lname+ ","
                                                        + System.getProperty("line.separator")
                                                        + System.getProperty("line.separator")
                                                        + "Your order of " +title+ " has been completed by the seller on " +dateTime+ ". Please review and verify the order. Thank you"
                                                        + System.getProperty("line.separator")
                                                        + System.getProperty("line.separator")
                                                        + "Freelancer Mobile Application";

                                                JavaMailAPI javaMailAPI = new JavaMailAPI (mContext, mail, subject, message);

                                                javaMailAPI.execute();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
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

    @Override
    public int getItemCount() {
        return mOrder.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView payment;
        public Button complete;
        public ImageView imageURL;
        public TextView paidDate;
        public TextView statusTxt;
        public TextView dueDate;

        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            name = itemView.findViewById(R.id.serviceName);
            payment = itemView.findViewById(R.id.servicePayment);
            imageURL = itemView.findViewById(R.id.serviceImage);
            complete = itemView.findViewById(R.id.completeBtn);
            paidDate = itemView.findViewById(R.id.servicePaidDate);
            statusTxt = itemView.findViewById(R.id.statustext);
            dueDate = itemView.findViewById(R.id.serviceDueDate);
        }
    }
}
