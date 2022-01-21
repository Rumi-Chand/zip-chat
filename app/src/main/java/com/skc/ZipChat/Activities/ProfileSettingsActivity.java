package com.skc.ZipChat.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.skc.ZipChat.Models.User;
import com.skc.ZipChat.R;
import com.skc.ZipChat.databinding.ActivityProfileSettingsBinding;
import com.skc.ZipChat.databinding.ChangeNameOrAboutDialogBinding;

import org.jetbrains.annotations.NotNull;

public class ProfileSettingsActivity extends AppCompatActivity {

    ActivityProfileSettingsBinding binding;

    ActionBar actionBar;

    DatabaseReference reference;

    View view;

    ChangeNameOrAboutDialogBinding changeNameOrAboutDialogBinding;

    String name, aboutInfo, uid;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        uid = FirebaseAuth.getInstance().getUid();

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.change_name_or_about_dialog, null);
        changeNameOrAboutDialogBinding = ChangeNameOrAboutDialogBinding.bind(view);

        reference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                Glide.with(ProfileSettingsActivity.this).load(user.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(binding.pflImg);

                name = user.getName();

                aboutInfo = user.getAboutInfo();

                binding.nameBox2.setText(name);

                binding.aboutBox.setText(aboutInfo);

                binding.phNumBox.setText(user.getPhoneNumber());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        binding.name2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNameOrAboutDialogBinding.nameOrAboutBox.setText(name);

                AlertDialog dialog = new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Enter your name")
                        .setView(changeNameOrAboutDialogBinding.getRoot())
                        .setPositiveButton("save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                progressDialog.setMessage("Updating name...");
                                progressDialog.show();

                                reference.child("name")
                                        .setValue(changeNameOrAboutDialogBinding.nameOrAboutBox.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(ProfileSettingsActivity.this, "Successfully updated name", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(ProfileSettingsActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .create();
            }
        });

        binding.about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNameOrAboutDialogBinding.nameOrAboutBox.setText(aboutInfo);

                AlertDialog dialog = new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Enter your about info")
                        .setView(changeNameOrAboutDialogBinding.getRoot())
                        .setPositiveButton("save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                progressDialog.setMessage("Updating about info...");
                                progressDialog.show();

                                reference.child("aboutInfo")
                                        .setValue(changeNameOrAboutDialogBinding.nameOrAboutBox.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(ProfileSettingsActivity.this, "Successfully updated about info", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(ProfileSettingsActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .create();
            }
        });

        binding.pflImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                progressDialog.setMessage("Updating profile image...");
                progressDialog.show();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profiles").child(uid);
                storageReference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    reference.child("profileImage")
                                            .setValue(uri.toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(ProfileSettingsActivity.this, "Successfully updated profile image", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(ProfileSettingsActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileSettingsActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}