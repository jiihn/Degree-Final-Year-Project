package com.example.fyp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fyp.Model.Service;
import com.example.fyp.R;
import com.example.fyp.ServiceDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private Context mContext;
    private List<Service> mService;
    private DatabaseReference mDatabase;

    public ServiceAdapter(Context mContext, List<Service> mService){
        this.mService = mService;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.service_row, parent, false);
        ViewHolder holder = new ViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Service service = mService.get(position);
        holder.name.setText(service.getName());
        holder.price.setText(service.getPrice());
        holder.description.setText(service.getDescription());
        Glide.with(mContext).load(service.getImageURL()).into(holder.imageURL);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase = FirebaseDatabase.getInstance().getReference("Service");
                Intent intent = new Intent (mContext, ServiceDetailsActivity.class);
                intent.putExtra("id", service.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mService.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView description;
        public ImageView imageURL;
        public TextView price;

        public ViewHolder(@NonNull View itemView, Context mContext){
            super(itemView);

            name = itemView.findViewById(R.id.post_title);
            description = itemView.findViewById(R.id.post_desc);
            price = itemView.findViewById(R.id.post_price);
            imageURL = itemView.findViewById(R.id.post_image);
        }
    }
}
