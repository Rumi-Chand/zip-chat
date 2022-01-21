package com.skc.ZipChat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skc.ZipChat.Activities.ChatActivity;
import com.skc.ZipChat.Activities.MainActivity;
import com.skc.ZipChat.Models.User;
import com.skc.ZipChat.R;
import com.skc.ZipChat.databinding.RowConversationBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UsersAndGrpsAdapter extends RecyclerView.Adapter<UsersAndGrpsAdapter.UsersViewHolder> implements SectionIndexer {

    Context context;
    ArrayList<User> users;
    String lastMsg, lastMsgDate, lastMsgTime, lastMsgType;

    private ArrayList<Integer> mSectionPositions;

    public UsersAndGrpsAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        for (int i = 0, size = users.size(); i < size; i++) {
            String section = String.valueOf(users.get(i).getName().charAt(0)).toUpperCase();
            if (!sections.contains(section)) {
                sections.add(section);
                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mSectionPositions.get(sectionIndex);
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);

        String senderRoom = FirebaseAuth.getInstance().getUid() + user.getUid();

        if (context.getClass() == MainActivity.class) {
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.child("lastMsgType").exists()) {
                                lastMsg = snapshot.child("lastMsg").getValue(String.class);
                                lastMsgDate = snapshot.child("lastMsgDate").getValue(String.class);
                                lastMsgTime = snapshot.child("lastMsgTime").getValue(String.class);
                                lastMsgType = snapshot.child("lastMsgType").getValue(String.class);


                                Calendar cal = Calendar.getInstance();

                                /*int msgYear, month, year, msgMonth, msgDay, day;

                                msgYear= 2000 + valueOf(lastMsgDate.substring(6));
                                msgMonth = valueOf(lastMsgDate.substring(3, 5));
                                month = cal.get(Calendar.MONTH);
                                year = cal.get(Calendar.YEAR);
                                msgDay = valueOf(lastMsgDate.substring(0,2));
                                day = cal.get(Calendar.DAY_OF_MONTH);*/


                                if (lastMsgDate.equals(new SimpleDateFormat("dd/MM/yy").format(new Date()))) {
                                    holder.binding.dateOrTime.setText(lastMsgTime);
                                /*} else if (year == msgYear) {
                                    if (month == msgMonth) {
                                        if (msgDay == day - 1) {
                                            holder.binding.dateOrTime.setText("Yesterday");
                                        }
                                    } else if (msgMonth == month-1) {
                                        cal.set(year, msgMonth, 1);
                                        if (msgDay == new GregorianCalendar().getActualMaximum(Calendar.DAY_OF_MONTH) && day == 1) {
                                            holder.binding.dateOrTime.setText("Yesterday");
                                        }
                                    }
                                } else if (((msgYear == year-1 && msgMonth == 11) && (msgDay == 31 && month == 0)) && (day == 1)) {
                                    holder.binding.dateOrTime.setText("Yesterday");
                                */} else {
                                    holder.binding.dateOrTime.setText(lastMsgDate);
                                }

                                holder.binding.lastMsgOrAboutInfo.setText(lastMsg);

                                if (lastMsgType.equals("img&caption") || lastMsgType.equals("img")) {
                                    holder.binding.lastMsgOrAboutInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_img, 0, 0, 0);
                                } else if (lastMsgType.equals("msg deleted")) {
                                    holder.binding.lastMsgOrAboutInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.block, 0, 0, 0);
                                } else {
                                    holder.binding.lastMsgOrAboutInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                }
                            } else {
                                holder.binding.lastMsgOrAboutInfo.setText("Tap to chat");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            holder.binding.lastMsgOrAboutInfo.setText(user.getAboutInfo());
        }

        holder.binding.username.setText(user.getName());

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("uid", user.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }

}
