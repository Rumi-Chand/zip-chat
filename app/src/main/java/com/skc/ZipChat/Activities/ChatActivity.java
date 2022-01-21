package com.skc.ZipChat.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.skc.ZipChat.Adapters.MessagesAdapter;
import com.skc.ZipChat.Interfaces.CallInterface;
import com.skc.ZipChat.Models.Message;
import com.skc.ZipChat.Models.User;
import com.skc.ZipChat.R;
import com.skc.ZipChat.databinding.ActivityChatBinding;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private MessagesAdapter adapter;
    private ArrayList<Message> messages;
    private String senderRoom, receiverRoom, senderUid, receiverUid, name, phNum, token, lastMsg, lastMsgType;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference reference, reference1, reference2, reference3, reference4;
    private ProgressDialog dialog;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private Uri selectedImg;
    private Message msg;
    private User user, currentUser;
    private Cursor cursor;
    private Message message;
    private CallInterface callInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        dateFormat = new SimpleDateFormat("dd/MM/yy");
        timeFormat = new SimpleDateFormat("h:mm a");

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = database.getReference().child("chats");
        reference1 = database.getReference().child("recentChatProfiles");
        reference2 = database.getReference().child("users");
        reference3 = reference.child(senderRoom);
        reference4 = reference.child(receiverRoom);

        callInterface = new CallInterface(this);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        messages = new ArrayList<>();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

       cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

       reference2.child(receiverUid).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               user = snapshot.getValue(User.class);

               token = user.getToken();

               if (cursor.getCount() > 0) {
                   while (cursor.moveToNext()) {
                       name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                       phNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                       if (user.getPhoneNumber().equals(phNum)) {
                           if (name.length() < 16) {
                               binding.name.setText(name);
                           } else {
                               binding.name.setText(name.substring(0, 11) + "...");
                           }
                           break;
                       }
                   }

               }

               Glide.with(ChatActivity.this).load(user.getProfileImage())
                       .placeholder(R.drawable.avatar)
                       .into(binding.profile);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

       reference2.child(senderUid).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               currentUser = snapshot.getValue(User.class);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if (!status.isEmpty()) {
                        if (status.equals("offline")) {
                            binding.status.setVisibility(View.GONE);
                        } else {
                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        callInterface.addCallClientService(receiverUid);

        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        binding.recyclerView.setAdapter(adapter);

        reference.child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            lastMsg = messages.get(messages.size() - 1).getMessage();

                            lastMsgType = messages.get(messages.size() - 1).getMessageType();

                            reference3.child("lastMsg").setValue(lastMsg);
                            reference3.child("lastMsgType").setValue(lastMsgType);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        reference.child(receiverRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            lastMsg = messages.get(messages.size() - 1).getMessage();

                            lastMsgType = messages.get(messages.size() - 1).getMessageType();

                            reference4.child("lastMsg").setValue(lastMsg);

                            reference4.child("lastMsgType").setValue(lastMsgType);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();

                Date date = new Date();

                message = new Message(messageTxt, "no img", senderUid, date.getTime());
                binding.messageBox.setText("");

                String randomKey = database.getReference().push().getKey();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsgDate", dateFormat.format(date));
                lastMsgObj.put("lastMsgTime", timeFormat.format(date));

                reference.child(senderRoom).updateChildren(lastMsgObj);
                reference.child(receiverRoom).updateChildren(lastMsgObj);

                reference.child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message);

                reference.child(receiverRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        reference1.child(senderUid).child(receiverUid).setValue(user);
                        reference1.child(receiverUid).child(senderUid).setValue(currentUser);
                    }
                });

                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        if (user.getPhoneNumber().equals(phNum)) {
                            break;
                        }
                    }

                }

                database.getReference()
                        .child("contactNames")
                        .child(senderUid)
                        .child(receiverUid)
                        .setValue(name).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        sendNotification(name, message.getMessage(), token);
                    }
                });
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("online");
                }
            };
        });
    }

    void sendNotification(String name, String msg, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name)
                    .put("body", msg);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data)
                    .put("to", token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map  = new HashMap<>();

                    String key = "Key=AAAATxxom20:APA91bGDTlacVMBtL6kWqJB9s9CPLocpM8m65EdLiZWBcBXzxYxlKQNjxJ-FTE8ozPVBXHg4sxzjSmoXw1TsixQtUzQAkRNwyVp7GVTes5nFO6H0bzmyKzswYI9-hOgwmh1wcBgEQi1-";

                    map.put("Authorization", key);
                    map.put("Content-Type", "application/json");
                    return map;
                }
            };

            queue.add(request);
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.getData() != null) {
                if (requestCode == 1) {
                    selectedImg = data.getData();

                    Intent intent = new Intent(this, SendPicActivity.class);
                    intent.putExtra("uID", receiverUid)
                            .putExtra("selectedImg", selectedImg);

                    startActivityForResult(intent, 2);
                }
                if (requestCode == 2) {
                    if (resultCode == Activity.RESULT_OK) {
                        dialog.show();
                        selectedImg = data.getData();

                        Calendar calendar = Calendar.getInstance();
                        StorageReference storageReference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                        storageReference.putFile(selectedImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Date date = new Date();

                                            String msgTxt = data.getStringExtra("msgTxt");

                                            if (!msgTxt.isEmpty()) {
                                                msg = new Message(msgTxt, "img&caption", senderUid, date.getTime());
                                            } else {
                                                msg = new Message("Photo", "img", senderUid, date.getTime());
                                            }

                                            msg.setImageUrl(uri.toString());

                                            binding.messageBox.setText("");

                                            String randomKey = database.getReference().push().getKey();

                                            HashMap<String, Object> lastMsgObj = new HashMap<>();
                                            lastMsgObj.put("lastMsgDate", dateFormat.format(date));
                                            lastMsgObj.put("lastMsgTime", timeFormat.format(date));

                                            reference.child(senderRoom).updateChildren(lastMsgObj);
                                            reference.child(receiverRoom).updateChildren(lastMsgObj);


                                            reference.child(senderRoom)
                                                    .child("messages")
                                                    .child(randomKey)
                                                    .setValue(msg).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    reference.child(receiverRoom)
                                                            .child("messages")
                                                            .child(randomKey)
                                                            .setValue(msg).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            database.getReference()
                                                                    .child("recentChatProfiles")
                                                                    .child(senderUid)
                                                                    .child(receiverUid).
                                                                    setValue(user);

                                                            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

                                                            if (cursor.getCount() > 0) {
                                                                while (cursor.moveToNext()) {
                                                                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                                                    phNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                                                    if (user.getPhoneNumber().equals(phNum)) {
                                                                        break;
                                                                    }
                                                                }

                                                            }

                                                            database.getReference()
                                                                    .child("contactNames")
                                                                    .child(senderUid)
                                                                    .child(receiverUid)
                                                                    .setValue(name).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {

                                                                }
                                                            });
                                                        }
                                                    });
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.voiceCall:
                callInterface.call();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("presence").child(senderUid).setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("presence").child(senderUid).setValue("offline");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(ChatActivity.this, MainActivity.class));
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}