package com.example.fyp.Adapter;

import android.content.Context;
import android.renderscript.Sampler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fyp.Model.Rating;
import com.example.fyp.Model.User;
import com.example.fyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder>{
    private Context mContext;
    List<Rating> mRating;
    private DatabaseReference mDatabase;

    public RatingAdapter(Context mContext, List<Rating> mRating) {
        this.mContext = mContext;
        this.mRating = mRating;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.commentratinglayout, parent, false);
        ViewHolder holder = new ViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Rating rating = mRating.get(position);

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);

        String timestamp = rating.getTimestamp();
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String date = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
        holder.postDate.setText(date);

        holder.ratingBar.setRating(rating.getRating());
        holder.commentTxt.setText(rating.getComment());

        DatabaseReference getUser = FirebaseDatabase.getInstance().getReference("Users").child(rating.getCommenter());

        getUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                holder.fname.setText(user.getFirst_name());
                holder.lname.setText(user.getLast_name());

                if (user.getImageURL().equals("default")) {
                    holder.imageURL.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(mContext.getApplicationContext()).load(user.getImageURL()).into(holder.imageURL);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mRating.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fname, lname;
        public ImageView imageURL;
        public RatingBar ratingBar;
        public TextView postDate;
        public TextView commentTxt;

        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            fname = itemView.findViewById(R.id.buyerFName);
            lname = itemView.findViewById(R.id.buyerLName);
            imageURL = itemView.findViewById(R.id.profile_image);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            commentTxt = itemView.findViewById(R.id.commentTxt);
            postDate = itemView.findViewById(R.id.postDate);
        }
    }
}
