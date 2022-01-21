package com.skc.ZipChat.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skc.ZipChat.Adapters.UsersAndGrpsAdapter;
import com.skc.ZipChat.Models.User;
import com.skc.ZipChat.databinding.ActivitySelectContactBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

public class SelectContactActivity extends AppCompatActivity {

    ActivitySelectContactBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users, users1;;
    UsersAndGrpsAdapter usersAndGrpsAdapter;
    User user;
    String name, phNum;
    ActionBar actionBar;
    int i;
    Set<User> set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        actionBar = getSupportActionBar();
        actionBar.setTitle("Select contact");
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS}, 0);
            return;
        }

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        users1 = new ArrayList<>();

        set = new LinkedHashSet<>();

        usersAndGrpsAdapter = new UsersAndGrpsAdapter(this, users1);

        binding.recyclerView2.setAdapter(usersAndGrpsAdapter);

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (cursor.getCount() > 0) {

            database.getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    users.clear();
                    users1.clear();

                    for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                        users.add(snapshot1.getValue(User.class));
                    }

                    int len = users.size();

                    if (cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            phNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            for (i = 0; i<len; i++) {

                                user = users.get(i);

                                if(user.getPhoneNumber().equals(phNum) && !user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                    user.setName(name);
                                    users1.add(user);
                                }
                            }
                        }
                    }

                    Collections.sort(users1, new Comparator<User>() {
                        @Override
                        public int compare(User lhs, User rhs) {
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    });

                    set.addAll(users1);

                    users1.clear();
                    users1.addAll(set);

                    usersAndGrpsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("offline");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}