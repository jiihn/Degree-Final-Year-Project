package com.example.fyp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fyp.Model.Order;
import com.example.fyp.Model.Service;
import com.example.fyp.Model.User;
import com.example.fyp.R;
import com.example.fyp.ServiceDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>{

    private Context mContext;
    List<Order> mOrder;
    private DatabaseReference mDatabase;

    public OrderHistoryAdapter(Context mContext, List<Order> mOrder) {
        this.mContext = mContext;
        this.mOrder = mOrder;
    }

    @NonNull
    @Override
    public OrderHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.orderhistorylayout, parent, false);
        ViewHolder holder = new ViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderHistoryAdapter.ViewHolder holder, int position) {
        final Order order = mOrder.get(position);
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

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);

        String completeTimestamp = order.getCompleteTimestamp();
        cal.setTimeInMillis(Long.parseLong(completeTimestamp));
        String completeDate = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        if (order.isPaid() == true && order.isComplete() == true) {
            if (order.isTransfer() == false) {
                if (order.isVerified() == false) {
                    final String currentTimestamp = String.valueOf(System.currentTimeMillis());

                    if (Float.valueOf(order.getAcceptTimestamp()) <= Float.valueOf(currentTimestamp)) {

                        DatabaseReference getPriceRef = FirebaseDatabase.getInstance().getReference("Service").child(serId);

                        getPriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Service service = dataSnapshot.getValue(Service.class);

                                Double servicePrice = Double.valueOf(service.getPrice());

                                Double calculate = servicePrice * 0.94;

                                final Float transferAmount = Float.valueOf(String.valueOf(calculate));

                                DatabaseReference getAmountRef = FirebaseDatabase.getInstance().getReference("Users").child(order.getSeller());

                                getAmountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);

                                        Float currentAmount = user.getEwallet();

                                        Float totalAmount = currentAmount + transferAmount;

                                        DatabaseReference transferRef = FirebaseDatabase.getInstance().getReference("Users").child(order.getSeller());

                                        HashMap<String, Object> transferMap = new HashMap<>();

                                        transferMap.put("ewallet", totalAmount);

                                        transferRef.updateChildren(transferMap);

                                        DatabaseReference updateOrderRef = FirebaseDatabase.getInstance().getReference("Order").child(order.getOrderId());

                                        HashMap<String, Object> orderMap = new HashMap<>();

                                        orderMap.put("transfer", true);

                                        updateOrderRef.updateChildren(orderMap);

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
                }
            }
        }

        holder.completeDate.setText(completeDate);

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

    @Override
    public int getItemCount() {
        return mOrder.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public ImageView imageURL;
        public Button rate;
        public TextView completeDate;

        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            name = itemView.findViewById(R.id.serviceName);
            imageURL = itemView.findViewById(R.id.serviceImage);
            rate = itemView.findViewById(R.id.rateBtn);
            completeDate = itemView.findViewById(R.id.completeDate);
        }
    }
}
