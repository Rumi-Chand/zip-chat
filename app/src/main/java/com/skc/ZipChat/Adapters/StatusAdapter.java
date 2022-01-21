package com.skc.ZipChat.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skc.ZipChat.Activities.MainActivity;
import com.skc.ZipChat.Models.Status;
import com.skc.ZipChat.Models.User;
import com.skc.ZipChat.Models.UserStatus;
import com.skc.ZipChat.R;
import com.skc.ZipChat.databinding.ItemStatusBinding;

import java.util.ArrayList;
import java.util.Date;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    Activity activity;
    ArrayList<UserStatus> userStatuses;
    String name;
    Intent intent;
    FirebaseDatabase database;
    Date date;
    User user;
    ArrayList<MyStory> myStories;
    UserStatus userStatus;

    public StatusAdapter(Activity activity, ArrayList<UserStatus> userStatuses) {
        this.activity = activity;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        database = FirebaseDatabase.getInstance();

        intent = new Intent();

        date = new Date();

        myStories = new ArrayList<>();

        userStatus = userStatuses.get(position);

        for(Status status : userStatus.getStatuses()) {
            myStories.add(new MyStory(status.getImageUrl()));
        }

        Glide.with(activity).load(myStories.get(myStories.size()-1).getUrl()).into(holder.binding.image);

        holder.binding.statusView.setPortionsCount(userStatus.getStatuses().size());

        holder.binding.lastUpdated.setText("Last updated");

        database.getReference().child("users").child(userStatus.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                name = userStatus.getName();

                holder.binding.name3.setText(name);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new StoryView.Builder(((MainActivity)activity).getSupportFragmentManager())
                                .setStoriesList(myStories) // Required
                                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                .setTitleText(name) // Default is Hidden
                                .setTitleLogoUrl(user.getProfileImage()) // Default is Hidden
                                .build() // Must be called before calling show method
                                .show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {

        ItemStatusBinding binding;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemStatusBinding.bind(itemView);
        }
    }
}
