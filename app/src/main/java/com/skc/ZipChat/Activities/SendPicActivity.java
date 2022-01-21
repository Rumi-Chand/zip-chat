package com.skc.ZipChat.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skc.ZipChat.Models.User;
import com.skc.ZipChat.R;
import com.skc.ZipChat.databinding.ActivitySendPicBinding;

import org.jetbrains.annotations.NotNull;

public class SendPicActivity extends AppCompatActivity {

    ActivitySendPicBinding binding;

    FirebaseDatabase database;

    String name, phNum;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendPicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar2);

        binding.backBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        database = FirebaseDatabase.getInstance();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();

        String uID = intent.getStringExtra("uID");

        Uri selectedImg = intent.getParcelableExtra("selectedImg");

        binding.imgVIew.setImageURI(selectedImg);

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        database.getReference().child("users").child(uID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);

                        if (cursor.getCount() > 0) {

                            while (cursor.moveToNext()) {
                                name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                phNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                if (user.getPhoneNumber().equals(phNum)) {
                                    binding.nameBox3.setText(name);
                                }
                            }

                        }

                        Glide.with(SendPicActivity.this).load(user.getProfileImage())
                                .placeholder(R.drawable.avatar)
                                .into(binding.pflImg2);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        binding.sendBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK,new Intent().setData(selectedImg).putExtra("msgTxt", binding.msgBox.getText().toString()));
                finish();
            }
        });
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
}