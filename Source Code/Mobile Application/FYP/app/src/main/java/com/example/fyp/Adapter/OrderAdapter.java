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
import com.example.fyp.R;
import com.example.fyp.ServiceDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context mContext;
    List<Order> mOrder;
    private DatabaseReference mDatabase;

    public OrderAdapter(Context mContext, List<Order> mOrder) {
        this.mContext = mContext;
        this.mOrder = mOrder;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.orderlistlayout, parent, false);
        OrderAdapter.ViewHolder holder = new OrderAdapter.ViewHolder(view, mContext);
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
                holder.price.setText(service.getPrice());
                Glide.with(mContext).load(service.getImageURL()).into(holder.imageURL);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(order.isPaid() == true){
            paymentStatus = "PAID";
            holder.status.setText("PAID");
        } else
        {
            paymentStatus = "PENDING";
        }

        String timestamp = order.getTimestamp();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        holder.payment.setText(paymentStatus);
        holder.date.setText(dateTime);

        if(order.isCancel() == true){
            holder.status.setVisibility(View.GONE);
            holder.cancelled.setVisibility(View.VISIBLE);
            holder.payment.setText("Cancelled");
        }

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
        public TextView date;
        public TextView payment;
        public TextView price;
        public TextView status;
        public TextView cancelled;
        public ImageView imageURL;
        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            name = itemView.findViewById(R.id.serviceName);
            date = itemView.findViewById(R.id.serviceDate);
            payment = itemView.findViewById(R.id.servicePayment);
            price = itemView.findViewById(R.id.servicePrice);
            imageURL = itemView.findViewById(R.id.serviceImage);
            status = itemView.findViewById(R.id.pendingtext);
            cancelled = itemView.findViewById(R.id.cancelledtext);
        }
    }
}
