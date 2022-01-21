package com.skc.ZipChat.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skc.ZipChat.Adapters.UsersAndGrpsAdapter;
import com.skc.ZipChat.Models.Status;
import com.skc.ZipChat.Models.User;
import com.skc.ZipChat.databinding.FragmentChatsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    FragmentChatsBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users, users1;
    UsersAndGrpsAdapter usersAndGrpsAdapter;
    ProgressDialog dialog;
    User user, user1;
    DatabaseReference reference;
    String name, phNum, uid, imgUrl;
    Activity activity;
    Date date;
    Status status;
    int i;
    Set<User> set;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
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
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        activity = getActivity();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("users");

        users = new ArrayList<>();
        users1 = new ArrayList<>();

        set = new LinkedHashSet<>();

        uid = FirebaseAuth.getInstance().getUid();

        usersAndGrpsAdapter = new UsersAndGrpsAdapter(activity, users1);

        binding.recyclerView.setAdapter(usersAndGrpsAdapter);
        binding.recyclerView.showShimmerAdapter();

        Cursor cursor = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        database.getReference().child("recentChatProfiles").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                users1.clear();
                users.clear();

                for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                    users.add(snapshot1.getValue(User.class));
                }

                int len = users.size();

                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        for (i = 0; i < len; i++) {
                            user1 = users.get(i);

                            if(user1.getPhoneNumber().equals(phNum)) {
                                user1.setName(name);
                                users1.add(user1);
                            }
                        }
                    }
                }

                set.addAll(users1);

                users1.clear();
                users1.addAll(set);

                usersAndGrpsAdapter.notifyDataSetChanged();
                binding.recyclerView.hideShimmerAdapter();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }
}