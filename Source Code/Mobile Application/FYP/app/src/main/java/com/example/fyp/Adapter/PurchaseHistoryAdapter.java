package com.example.fyp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class PurchaseHistoryAdapter extends RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder>{

    private Context mContext;
    List<Order> mOrder;
    private DatabaseReference mDatabase;
    FirebaseUser fuser;
    String seller;
    String fname, lname;

    public PurchaseHistoryAdapter(Context mContext, List<Order> mOrder) {
        this.mContext = mContext;
        this.mOrder = mOrder;
    }

    @NonNull
    @Override
    public PurchaseHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.purchasehistorylayout, parent, false);
        ViewHolder holder = new ViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        final Order order = mOrder.get(position);
        final String serId = order.getServiceId();
        mDatabase = FirebaseDatabase.getInstance().getReference("Service").child(serId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Service service = dataSnapshot.getValue(Service.class);
                holder.name.setText(service.getName());
                Glide.with(mContext).load(service.getImageURL()).into(holder.imageURL);
                seller = service.getBy();

                DatabaseReference rateRef = FirebaseDatabase.getInstance().getReference("Users").child(seller);
                rateRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        fname = user.getFirst_name();
                        lname = user.getLast_name();
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

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);

        String completeTimestamp = order.getCompleteTimestamp();
        cal.setTimeInMillis(Long.parseLong(completeTimestamp));
        String completeDate = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        String dueTimestamp = order.getDueTimestamp();
        cal.setTimeInMillis(Long.parseLong(dueTimestamp));
        String dueDate = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        holder.completeDate.setText(completeDate);
        holder.dueDate.setText(dueDate);

        DatabaseReference check = FirebaseDatabase.getInstance().getReference("Order").child(order.getOrderId());

        check.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Order order = dataSnapshot.getValue(Order.class);

                if(order.isRated()){
                    holder.rate.setText("Rated");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference checkRated = FirebaseDatabase.getInstance().getReference("Order").child(order.getOrderId());

                checkRated.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Order order = dataSnapshot.getValue(Order.class);

                        if(order.isRated()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                            builder.setTitle("You already rated this service and seller.");
                            builder.setPositiveButton("Rate again", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    try {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        View layout= null;
                                        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                        layout = inflater.inflate(R.layout.rating, null);
                                        final RatingBar ratingBar = layout.findViewById(R.id.ratingBar);
                                        final EditText comment = layout.findViewById(R.id.commentTxt);
                                        builder.setTitle("Rate " +fname+ " " +lname);
                                        builder.setMessage("Rate this seller based on their quality of service");
                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Float value = ratingBar.getRating();
                                                String text = comment.getText().toString().trim();

                                                DatabaseReference findRating = FirebaseDatabase.getInstance().getReference("Rating").child(order.getRatingId());
                                                final String timestamp = String.valueOf(System.currentTimeMillis());

                                                HashMap<String, Object> reRatingMap = new HashMap<>();
                                                reRatingMap.put("rating", value);
                                                reRatingMap.put("comment", text);
                                                reRatingMap.put("commenter", fuser.getUid());
                                                reRatingMap.put("timestamp", timestamp);

                                                findRating.updateChildren(reRatingMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        DatabaseReference ratingListRef = FirebaseDatabase.getInstance().getReference("Ratinglist").child(order.getSeller()).child(order.getRatingId());

                                                        HashMap<String, Object> ratingListMap = new HashMap<>();
                                                        ratingListMap.put("id", order.getRatingId());

                                                        ratingListRef.updateChildren(ratingListMap);

                                                        Toast.makeText(mContext, "Your rating has been updated.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.setCancelable(false);
                                        builder.setView(layout);
                                        builder.show();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            try {

                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                View layout= null;
                                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                layout = inflater.inflate(R.layout.rating, null);
                                final RatingBar ratingBar = layout.findViewById(R.id.ratingBar);
                                final EditText comment = layout.findViewById(R.id.commentTxt);
                                builder.setTitle("Rate " +fname+ " " +lname);
                                builder.setMessage("Rate this seller based on their quality of service");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Float value = ratingBar.getRating();
                                        String text = comment.getText().toString().trim();

                                        DatabaseReference ratingRef = FirebaseDatabase.getInstance().getReference("Rating");
                                        final String key = ratingRef.push().getKey();
                                        final String timestamp = String.valueOf(System.currentTimeMillis());

                                        HashMap<String, Object> ratingMap = new HashMap<>();
                                        ratingMap.put("rating", value);
                                        ratingMap.put("comment", text);
                                        ratingMap.put("timestamp", timestamp);
                                        ratingMap.put("commenter", fuser.getUid());
                                        ratingMap.put("orderId", order.getOrderId());
                                        ratingMap.put("ratingId", key);

                                        ratingRef.child(key).setValue(ratingMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                DatabaseReference ratingListRef = FirebaseDatabase.getInstance().getReference("Ratinglist").child(order.getSeller()).child(key);

                                                HashMap<String, Object> ratingListMap = new HashMap<>();
                                                ratingListMap.put("id", key);

                                                ratingListRef.setValue(ratingListMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference("Order").child(order.getOrderId());

                                                        HashMap<String, Object> updateMap = new HashMap<>();
                                                        updateMap.put("rated", true);
                                                        updateMap.put("ratingId", key);

                                                        updateRef.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(mContext, "Successfully rate seller", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.setCancelable(false);
                                builder.setView(layout);
                                builder.show();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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

    @Override
    public int getItemCount() {
        return mOrder.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView imageURL;
        public Button rate;
        public TextView completeDate;
        public TextView dueDate;

        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            name = itemView.findViewById(R.id.serviceName);
            imageURL = itemView.findViewById(R.id.serviceImage);
            rate = itemView.findViewById(R.id.rateBtn);
            completeDate = itemView.findViewById(R.id.completeDate);
            dueDate = itemView.findViewById(R.id.dueDate);
        }
    }
}
