package com.skc.ZipChat.Interfaces;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skc.ZipChat.Activities.CallActivity;

public class CallInterface {

    private String callerUid, currentID = FirebaseAuth.getInstance().getUid(), callStatus, uid;
    private Intent intent;
    private Activity activity;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();;
    private DatabaseReference reference = database.getReference().child("calls"), reference1;
    private String callType;

    public CallInterface(Activity activity) {
        this.activity = activity;
    }

    public void addCallClientService(String uid) {
        this.uid = uid;

        reference1 = reference.child(uid);

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callType = "Outgoing Call";
                }
                else {
                    callType = "Call Busy";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addCallListener() {
        reference.child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callStatus = snapshot.child("callStatus").getValue(String.class);

                    if (callStatus != null) {
                        if (((!callStatus.equals("Call Declined")) && (!callStatus.equals("Progressing"))) && !callStatus.equals("Call Ended")) {
                            callerUid = snapshot.child("callerUid").getValue(String.class);

                            intent = new Intent(activity, CallActivity.class);
                            intent.putExtra("callerUid", callerUid).putExtra("callType", "Incoming Call");
                            activity.startActivity(intent);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void call() {
        if (callType.equals("Outgoing Call")) {
            reference1.child("callerUid").setValue(currentID);
            reference1.child("callStatus").setValue(callType);
        }

        reference.child(currentID).setValue("Call Busy");

        intent = new Intent(activity, CallActivity.class);
        intent.putExtra("callReceiverUid", uid).putExtra("callType", callType);
        activity.startActivity(intent);

    }
}
