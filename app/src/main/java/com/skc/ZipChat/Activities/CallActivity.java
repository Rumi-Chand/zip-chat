package com.skc.ZipChat.Activities;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skc.ZipChat.databinding.ActivityCallBinding;

public class CallActivity extends AppCompatActivity {

    private ActivityCallBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference reference, reference1, reference2, reference3;
    private String type, callStatus, callStatus1, callReceiverUid, presence, currentId,  callerUid;
    private ConstraintLayout layout;
    private ConstraintSet set;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        currentId  = FirebaseAuth.getInstance().getUid();

        reference = database.getReference().child("calls").child(currentId);
        reference2 = reference.child("callStatus");

        type = getIntent().getStringExtra("callType");

        if (type.equals("Incoming Call")) {
            callerUid = getIntent().getStringExtra("callerUid");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                    } else {
                        binding.callStatus.setText("Call Ended");

                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            reference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    callStatus = snapshot.getValue(String.class);

                    if (snapshot.exists()) {
                        if (callStatus.equals("Progressing"))
                            callStatus1 = "Call Ended";
                        else
                            callStatus1 = "Call Declined";
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            binding.pickBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    reference2.setValue("Progressing");

                    reference2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            callStatus = snapshot.getValue(String.class);

                            if (snapshot.exists()) {
                                if (callStatus.equals("Progressing")) {
                                    binding.callStatus.setText("Call in progress");

                                    layout = binding.getRoot();
                                    set = new ConstraintSet();
                                    set.clone(layout);
                                    int startId = binding.hangUpOrDeclineBtn.getId();
                                    set.constrainHeight(startId, ConstraintSet.WRAP_CONTENT);
                                    set.constrainWidth(startId, ConstraintSet.WRAP_CONTENT);

                                    set.connect(startId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
                                    set.connect(startId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
                                    set.connect(startId, ConstraintSet.TOP, binding.callStatus.getId(), ConstraintSet.BOTTOM, 0);
                                    set.connect(startId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);

                                    set.applyTo(layout);

                                    binding.pickBtn.setVisibility(View.GONE);
                                }
                            } else {
                                binding.callStatus.setText("Call Ended");
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            binding.hangUpOrDeclineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    reference2.setValue(callStatus1);
                    binding.callStatus.setText(callStatus1);
                    finish();
                }
            });
        } else if (type.equals("Outgoing Call")) {
            layout = binding.getRoot();
            set = new ConstraintSet();
            set.clone(layout);
            int startId = binding.hangUpOrDeclineBtn.getId();
            set.constrainHeight(startId, ConstraintSet.WRAP_CONTENT);
            set.constrainWidth(startId, ConstraintSet.WRAP_CONTENT);

            set.connect(startId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
            set.connect(startId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
            set.connect(startId, ConstraintSet.TOP, binding.callStatus.getId(), ConstraintSet.BOTTOM, 0);
            set.connect(startId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);

            set.applyTo(layout);

            binding.pickBtn.setVisibility(View.GONE);

            callReceiverUid = getIntent().getStringExtra("callReceiverUid");

            reference1 = database.getReference().child("calls").child(callReceiverUid);

            database.getReference().child("presence").child(callReceiverUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    presence = snapshot.getValue(String.class);
                    
                    if (presence.equals("online")) {
                        callStatus = "Ringing";
                    } else {
                        callStatus = "Calling";
                    }

                    binding.callStatus.setText(callStatus);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            reference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        callStatus = snapshot.child("callStatus").getValue(String.class);
                        if (callStatus != null) {
                            if (callStatus.equals("Progressing")) {
                                binding.callStatus.setText("Call in progress");
                            } else if (callStatus.equals("Call Declined") || callStatus.equals("Call Ended")) {
                                binding.callStatus.setText(callStatus);

                                reference1.setValue(null);
                            }
                        }
                    } else {
                        reference.setValue(null);
                        finish();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            binding.hangUpOrDeclineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    binding.callStatus.setText("Call Ended");

                    reference1.setValue(null);
                }
            });
        } else {
            layout = binding.getRoot();
            set = new ConstraintSet();
            set.clone(layout);
            int startId = binding.hangUpOrDeclineBtn.getId();
            set.constrainHeight(startId, ConstraintSet.WRAP_CONTENT);
            set.constrainWidth(startId, ConstraintSet.WRAP_CONTENT);

            set.connect(startId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
            set.connect(startId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
            set.connect(startId, ConstraintSet.TOP, binding.callStatus.getId(), ConstraintSet.BOTTOM, 0);
            set.connect(startId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);

            set.applyTo(layout);

            binding.pickBtn.setVisibility(View.GONE);

            binding.callStatus.setText("Call Busy");

            binding.hangUpOrDeclineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("presence").child(currentId).setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("presence").child(currentId).setValue("offline");
    }
}