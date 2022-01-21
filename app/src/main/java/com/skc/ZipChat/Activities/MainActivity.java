package com.skc.ZipChat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.skc.ZipChat.Fragments.ChatsFragment;
import com.skc.ZipChat.Fragments.StatusFragment;
import com.skc.ZipChat.Interfaces.CallInterface;
import com.skc.ZipChat.R;
import com.skc.ZipChat.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseDatabase database;
    private FragmentTransaction chatsTrans, transaction;
    private String currentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new CallInterface(this).addCallListener();

        currentID = FirebaseAuth.getInstance().getUid();

        database = FirebaseDatabase.getInstance();

        chatsTrans  = getSupportFragmentManager().beginTransaction();
        chatsTrans.replace(R.id.content, new ChatsFragment());
        chatsTrans.commit();

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                database.getReference()
                        .child("users")
                        .child(currentID)
                        .child("token")
                        .setValue(token);
            }
        });

        binding.bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                transaction  = getSupportFragmentManager().beginTransaction();

                switch (item.getItemId()) {
                    case R.id.chats:
                        transaction.replace(R.id.content, new ChatsFragment());
                        transaction.commit();
                        break;
                    case R.id.status:
                        transaction.replace(R.id.content, new StatusFragment());
                        transaction.commit();
                        break;
                    case R.id.calls:
                        Toast.makeText(MainActivity.this, "Calls clicked", Toast.LENGTH_SHORT).show();
                        break;
                }

                return true;
            }
        });

        binding.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SelectContactActivity.class));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group:
                startActivity(new Intent(MainActivity.this, GroupChatActivity.class));
                break;
            case R.id.search:
                Toast.makeText(this, "Search clicked.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "Settings Clicked.", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        database.getReference().child("presence").child(currentID).setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();

        database.getReference().child("presence").child(currentID).setValue("offline");
    }
}