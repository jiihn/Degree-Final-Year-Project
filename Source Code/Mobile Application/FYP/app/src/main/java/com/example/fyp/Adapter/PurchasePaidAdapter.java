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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PurchasePaidAdapter extends RecyclerView.Adapter<PurchasePaidAdapter.ViewHolder> {

    private Context mContext;
    List<Order> mOrder;
    private DatabaseReference mDatabase;

    public PurchasePaidAdapter(Context mContext, List<Order> mOrder) {
        this.mContext = mContext;
        this.mOrder = mOrder;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.purchasepaidlistlayout, parent, false);
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
                holder.name.setText(service.getName());
                Glide.with(mContext).load(service.getImageURL()).into(holder.imageURL);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(order.isPaid() == true){
            paymentStatus = "PAID";
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

        String completeTimestamp = order.getCompleteTimestamp();
        cal.setTimeInMillis(Long.parseLong(completeTimestamp));
        String completeDate = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        String acceptTimestamp = order.getAcceptTimestamp();
        cal.setTimeInMillis(Long.parseLong(acceptTimestamp));
        String acceptDate = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        final String currentTimestamp = String.valueOf(System.currentTimeMillis());

        if(order.isCancel() == true){
            holder.cancelBtn.setVisibility(View.GONE);
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText("CANCELLED");
        } else {
            if (order.isPaid() == true && order.isComplete() == false) {
                holder.status.setVisibility(View.VISIBLE);

                if (Float.valueOf(dueTimestamp) <= Float.valueOf(currentTimestamp)) {
                    holder.status.setVisibility(View.GONE);
                    holder.cancelBtn.setVisibility(View.VISIBLE);
                }
            } else if (order.isPaid() == true && order.isComplete() == true) {
                holder.receive.setVisibility(View.VISIBLE);
                holder.paidDate.setVisibility(View.GONE);
                holder.dateText.setVisibility(View.GONE);
                holder.completeDate.setVisibility(View.VISIBLE);
                holder.completeDate.setText(completeDate);
                holder.completeText.setVisibility(View.VISIBLE);
                holder.dueDate.setVisibility(View.GONE);
                holder.dueDateText.setVisibility(View.GONE);
                holder.acceptText.setVisibility(View.VISIBLE);
                holder.acceptDate.setVisibility(View.VISIBLE);
                holder.acceptDate.setText(acceptDate);
            }
        }

        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderId = order.getOrderId();
                cancelOrder(orderId, serId, order.getBuyer());
            }
        });

        holder.receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(order.isTransfer() == true) {
                    verifyOrder(order.getOrderId());
                } else if(order.isTransfer() == false){
                    transferFund(order.getSeller(), order.getOrderId(), order.getServiceId());
                }
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

    public void cancelOrder(final String orderId, final String serId, final String buyerId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage("Cancel order?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference cancelOrderRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("cancel", true);

                        cancelOrderRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                DatabaseReference getPriceRef = FirebaseDatabase.getInstance().getReference("Service").child(serId);

                                getPriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Service service = dataSnapshot.getValue(Service.class);

                                        Double servicePrice = Double.valueOf(service.getPrice());

                                        Double calculate = servicePrice;

                                        final Float transferAmount = Float.valueOf(String.valueOf(calculate));

                                        DatabaseReference getAmountRef = FirebaseDatabase.getInstance().getReference("Users").child(buyerId);

                                        getAmountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);

                                                Float currentAmount = user.getEwallet();

                                                Float totalAmount = currentAmount + transferAmount;

                                                DatabaseReference transferRef = FirebaseDatabase.getInstance().getReference("Users").child(buyerId);

                                                HashMap<String, Object> transferMap = new HashMap<>();

                                                transferMap.put("ewallet", totalAmount);

                                                transferRef.updateChildren(transferMap);

                                                /*DatabaseReference updateOrderRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);

                                                HashMap<String, Object> orderMap = new HashMap<>();

                                                orderMap.put("refund", true);

                                                updateOrderRef.updateChildren(orderMap);*/

                                                Toast.makeText(mContext, "Fund successfully transferred to your e-wallet", Toast.LENGTH_SHORT).show();
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

    public void transferFund(final String sellerId, final String orderId, final String serId){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage("Received Order?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference verRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("verified", true);

                        verRef.updateChildren(hashMap);

                        DatabaseReference getPriceRef = FirebaseDatabase.getInstance().getReference("Service").child(serId);

                        getPriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Service service = dataSnapshot.getValue(Service.class);

                                Double servicePrice = Double.valueOf(service.getPrice());

                                Double calculate = servicePrice * 0.94;

                                final Float transferAmount = Float.valueOf(String.valueOf(calculate));

                                DatabaseReference getAmountRef = FirebaseDatabase.getInstance().getReference("Users").child(sellerId);

                                getAmountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);

                                        Float currentAmount = user.getEwallet();

                                        Float totalAmount = currentAmount + transferAmount;

                                        DatabaseReference transferRef = FirebaseDatabase.getInstance().getReference("Users").child(sellerId);

                                        HashMap<String, Object> transferMap = new HashMap<>();

                                        transferMap.put("ewallet", totalAmount);

                                        transferRef.updateChildren(transferMap);

                                        DatabaseReference updateOrderRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);

                                        HashMap<String, Object> orderMap = new HashMap<>();

                                        orderMap.put("transfer", true);

                                        updateOrderRef.updateChildren(orderMap);

                                        Toast.makeText(mContext, "Fund successfully transferred to seller's e-wallet", Toast.LENGTH_SHORT).show();

                                        String sellerEmail = user.getEmail();
                                        String fname = user.getFirst_name();
                                        String lname = user.getLast_name();
                                        long currentTimestamp = System.currentTimeMillis();
                                        final String timestamp = String.valueOf(currentTimestamp);
                                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                        cal.setTimeInMillis(Long.parseLong(timestamp));
                                        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                                        String mail = sellerEmail;
                                        String subject = "Fund has been transferred to your e-wallet";
                                        String message = "Dear " +fname+ " " +lname+ ","
                                                + System.getProperty("line.separator")
                                                + System.getProperty("line.separator")
                                                + "A service has been received by a buyer on " +dateTime+ ". The remaining fund (deducted 6%) has been transferred to your e-wallet. Please review your e-wallet"
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

    public void verifyOrder(final String orderId){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage("Received Order?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference verRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("verified", true);

                        verRef.updateChildren(hashMap);
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
        public TextView status;
        public ImageView imageURL;
        public TextView paidDate;
        public TextView dueDate;
        public TextView dateText;
        public TextView completeText;
        public TextView completeDate;
        public TextView dueDateText;
        public TextView acceptText;
        public TextView acceptDate;
        public Button receive;
        public Button cancelBtn;
        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            name = itemView.findViewById(R.id.serviceName);
            imageURL = itemView.findViewById(R.id.serviceImage);
            status = itemView.findViewById(R.id.statustext);
            paidDate = itemView.findViewById(R.id.paidDate);
            dateText = itemView.findViewById(R.id.datetxt);
            dueDate = itemView.findViewById(R.id.serviceDueDate);
            dueDateText = itemView.findViewById(R.id.dueDatetxt);
            completeText = itemView.findViewById(R.id.completeText);
            completeDate = itemView.findViewById(R.id.completeDate);
            acceptText = itemView.findViewById(R.id.acceptText);
            acceptDate = itemView.findViewById(R.id.acceptDate);
            receive = itemView.findViewById(R.id.receiveBtn);
            cancelBtn = itemView.findViewById(R.id.cancelBtn);
        }
    }
}
