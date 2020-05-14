package com.example.fyp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.Model.Category;
import com.example.fyp.R;
import com.example.fyp.ServiceListActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    private Context mContext;
    List<Category> mCategory;
    private DatabaseReference mDatabase;

    public CategoryAdapter(Context mContext, List<Category> mCategory) {
        this.mContext = mContext;
        this.mCategory = mCategory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.categorylayout, parent, false);
        CategoryAdapter.ViewHolder holder = new CategoryAdapter.ViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Category category = mCategory.get(position);

        holder.categoryName.setText(category.getName());

        DatabaseReference listingRef = FirebaseDatabase.getInstance().getReference("Categorylist").child(category.getId());

        listingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long listCount = dataSnapshot.getChildrenCount();
                String count = String.valueOf(listCount);
                holder.serviceCount.setText(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), ServiceListActivity.class);
                intent.putExtra("id", category.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCategory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryName;
        public TextView serviceCount;

        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            categoryName = itemView.findViewById(R.id.categoryName);
            serviceCount = itemView.findViewById(R.id.serviceCount);
        }
    }
}
