package com.example.fyp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fyp.Model.Service;
import com.example.fyp.R;
import com.example.fyp.ServiceDetailsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class MyProjectAdapter extends RecyclerView.Adapter<MyProjectAdapter.ViewHolder> {

    private Context mContext;
    private List<Service> mService;
    private DatabaseReference mDatabase;
    private DatabaseReference delReference;
    private DatabaseReference chgServiceRef;
    private DatabaseReference delServiceList;
    private FirebaseAuth mFirebaseAuth;
    private String currentUserId;

    public MyProjectAdapter(Context mContext, List<Service> mService) {
        this.mService = mService;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyProjectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_project, parent, false);
        MyProjectAdapter.ViewHolder holder = new MyProjectAdapter.ViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyProjectAdapter.ViewHolder holder, final int position) {
        final Service service = mService.get(position);
        holder.name.setText(service.getName());
        holder.dropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.dropDown, position);
            }
        });
        Glide.with(mContext).load(service.getImageURL()).into(holder.imageURL);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase = FirebaseDatabase.getInstance().getReference("Service");

                Intent intent = new Intent(mContext, ServiceDetailsActivity.class);
                intent.putExtra("id", service.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    private void showPopupMenu(final View view, int position) {
        final Service menuService = mService.get(position);
        // inflate menuAvailable
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        deleteFromMenu(menuService.getId(), menuService.getCategory());
                        return true;
                    case R.id.setAvailability:
                        setServiceAvailability(menuService.getId());
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    public void setServiceAvailability(final String sId){
        String options[] = {"Available", "Unavailable"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);

        builder.setTitle("Choose Action");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    mFirebaseAuth = FirebaseAuth.getInstance();

                    chgServiceRef = FirebaseDatabase.getInstance().getReference("Service").child(sId);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("available", true);

                    chgServiceRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(mContext, "Service Status Changed To Available", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (which == 1) {
                    mFirebaseAuth = FirebaseAuth.getInstance();

                    chgServiceRef = FirebaseDatabase.getInstance().getReference("Service").child(sId);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("available", false);

                    chgServiceRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(mContext, "Service Status Changed To Not Available", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        builder.create().show();
    }

    private void deleteFromMenu(final String sId, final String cId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage("Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        delServiceList = FirebaseDatabase.getInstance().getReference("Servicelist").child(currentUserId).child(sId);
        delServiceList.removeValue();

        DatabaseReference delFromCat = FirebaseDatabase.getInstance().getReference("Categorylist").child(cId).child(sId);
        delFromCat.removeValue();

        delReference = FirebaseDatabase.getInstance().getReference("Service").child(sId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("deleted", true);

        delReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(mContext, "Delete complete", Toast.LENGTH_SHORT).show();
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
        return mService.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView imageURL;
        public ImageButton dropDown;

        public ViewHolder(@NonNull View itemView, Context mContext) {
            super(itemView);

            name = itemView.findViewById(R.id.post_title);
            dropDown = itemView.findViewById(R.id.dropDown);
            imageURL = itemView.findViewById(R.id.post_image);
        }
    }
}
