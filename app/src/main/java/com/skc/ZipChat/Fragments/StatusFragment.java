package com.skc.ZipChat.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.skc.ZipChat.Activities.MainActivity;
import com.skc.ZipChat.Adapters.StatusAdapter;
import com.skc.ZipChat.Models.Status;
import com.skc.ZipChat.Models.User;
import com.skc.ZipChat.Models.UserStatus;
import com.skc.ZipChat.databinding.FragmentStatusBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.model.MyStory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusFragment extends Fragment {

    FragmentStatusBinding binding;
    StatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    ArrayList<Status> statuses;
    FirebaseDatabase database;
    DatabaseReference reference, reference1;
    ArrayList<User> users, users1;
    ProgressDialog dialog;
    User user, user1;
    String name, phNum, uid, pflImg, imgUrl;
    Activity activity;
    Date date;
    UserStatus status;
    Status sampleStatus;
    int i, len;
    SimpleDateFormat dateFormat;
    SimpleDateFormat timeFormat;
    Long lastUpdated, time;
    Cursor cursor;
    Intent intent;
    ArrayList<MyStory> myStories;
    int userStatusCount;
    Set<User> set;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StatusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatusFragment newInstance(String param1, String param2) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(inflater, container, false);

        activity = getActivity();

        intent = new Intent();

        dialog = new ProgressDialog(activity);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        date = new Date();

        users = new ArrayList<>();
        users1 = new ArrayList<>();

        set = new LinkedHashSet<>();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("stories");
        reference1 = database.getReference().child("users");

        uid = FirebaseAuth.getInstance().getUid();

        sampleStatus = new Status();

        userStatuses = new ArrayList<>();

        myStories = new ArrayList<>();

        statusAdapter = new StatusAdapter(activity, userStatuses);

        binding.statusList.setAdapter(statusAdapter);

        cursor = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        reference1.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                user = snapshot.getValue(User.class);

                pflImg = user.getProfileImage();

                reference.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myStories.clear();

                        lastUpdated = snapshot.child("lastUpdated").getValue(Long.class);

                        for (DataSnapshot statusSnapshot : snapshot.child("statuses").getChildren()) {
                            sampleStatus = statusSnapshot.getValue(Status.class);
                            myStories.add(new MyStory(sampleStatus.getImageUrl()));
                        }

                        userStatusCount = myStories.size();

                        if (myStories.isEmpty()) {
                            Glide.with(activity).load(pflImg).into(binding.image);
                            binding.msg.setText("Tap to add status update");
                            binding.statusView.setVisibility(View.GONE);
                        } else {
                            Glide.with(activity).load(myStories.get(userStatusCount-1).getUrl()).into(binding.image);
                            binding.msg.setText("Tap to view your status updates");
                            binding.statusView.setVisibility(View.VISIBLE);
                            binding.statusView.setPortionsCount(userStatusCount);
                            Log.i("Hello World", "" + userStatusCount);
                        }

                        binding.userStatus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (myStories.isEmpty()) {
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(intent, 0);
                                } else {
                                    new StoryView.Builder(((MainActivity)activity).getSupportFragmentManager())
                                            .setStoriesList(myStories) // Required
                                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                            .setTitleText("My Status") // Default is Hidden
                                            .setTitleLogoUrl(pflImg) // Default is Hidden
                                            .build() // Must be called before calling show method
                                            .show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (cursor.getCount() > 0) {
            reference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    binding.statusList.showShimmerAdapter();

                    users.clear();
                    users1.clear();

                    for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                        users.add(snapshot1.getValue(User.class));
                    }

                    len = users.size();

                    while (cursor.moveToNext()) {
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        for (i = 0; i<len; i++) {

                            user1 = users.get(i);

                            if(user1.getPhoneNumber().equals(phNum) && !user1.getUid().equals(uid)) {
                                user1.setName(name);
                                users1.add(user1);
                            }
                        }
                    }

                    set.addAll(users1);

                    users1.clear();
                    users1.addAll(set);

                    reference.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userStatuses.clear();
                            for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                                for (i = 0; i < users1.size(); i++)
                                {
                                    user1 = users1.get(i);

                                    String uid = storySnapshot.child("uid").getValue(String.class);

                                    if (!(uid == null)) {
                                        if (user1.getUid().equals(uid) && !uid.equals(FirebaseAuth.getInstance().getUid())) {
                                            lastUpdated = storySnapshot.child("lastUpdated").getValue(Long.class);
                                            status = new UserStatus(user1.getName(), uid, lastUpdated, null);
                                            statuses = new ArrayList<>();

                                            for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                                                sampleStatus = statusSnapshot.getValue(Status.class);
                                                statuses.add(sampleStatus);
                                            }

                                            status.setStatuses(statuses);
                                            userStatuses.add(status);
                                        }
                                    }
                                }
                            }

                            statusAdapter.notifyDataSetChanged();
                            binding.statusList.hideShimmerAdapter();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if(data.getData() != null) {
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");

                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    time = date.getTime();

                                    HashMap<String, Object> obj = new HashMap<>();
                                    obj.put("lastUpdated", time);
                                    obj.put("uid", uid);

                                    imgUrl = uri.toString();
                                    Status status = new Status(imgUrl, time);

                                    database.getReference()
                                            .child("stories")
                                            .child(uid)
                                            .updateChildren(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                database.getReference()
                                                        .child("stories")
                                                        .child(uid)
                                                        .child("statuses")
                                                        .push()
                                                        .setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            for (i = 0; i < users1.size(); i++)
                                                            {
                                                                database.getReference()
                                                                        .child("stories")
                                                                        .child(users1.get(i).getUid())
                                                                        .child(uid)
                                                                        .updateChildren(obj)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    database.getReference()
                                                                                            .child("stories")
                                                                                            .child(users1.get(i-1).getUid())
                                                                                            .child(uid)
                                                                                            .child("statuses")
                                                                                            .push()
                                                                                            .setValue(status);
                                                                                }
                                                                            }
                                                                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        dialog.dismiss();

                                                                        if (!task.isSuccessful()) {
                                                                            Toast.makeText(activity, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        }
    }
}