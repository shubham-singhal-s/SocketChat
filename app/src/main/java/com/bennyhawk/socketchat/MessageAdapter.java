package com.bennyhawk.socketchat;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;


class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {


    private RealmResults<Message> messageRealmResults;


    public MessageAdapter(Context context) {
        Realm mMessageStore;
        Realm.init(context);
        mMessageStore = Realm.getDefaultInstance();
        messageRealmResults = mMessageStore.where(Message.class).findAllSorted("mRecievedDate");

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return messageRealmResults.get(position).getmType();
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

        LinearLayout messageView = (LinearLayout) v.findViewById(R.id.message_view_card);
        LinearLayout messageViewColor = (LinearLayout) v.findViewById(R.id.message_view_card_color);


        switch (viewType) {
            case Message.TYPE_MESSAGE_USER:

                messageView.setGravity(Gravity.END);
                messageViewColor.setBackgroundColor(Color.parseColor("#E1FFC7"));

                break;

            case Message.TYPE_MESSAGE_OTHER:
                TextView userName = (TextView)v.findViewById(R.id.username);
                userName.setVisibility(View.VISIBLE);
                messageViewColor.setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;

            case Message.TYPE_LOG:

                messageView.setGravity(Gravity.CENTER);
                messageViewColor.setBackgroundColor(Color.parseColor("#D4EAF4"));

                break;

            case Message.TYPE_MESSAGE_USER_CONTINUE:
                messageView.setGravity(Gravity.END);
                messageView.setPadding(16,0,16,12);
                messageViewColor.setBackgroundColor(Color.parseColor("#E1FFC7"));
                break;

            case Message.TYPE_MESSAGE_OTHER_CONTINUE:
                messageView.setPadding(16,0,16,12);
                messageViewColor.setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {

        //Message message = messageRealmResults.get(position);

        //if (messageRealmResults.get(position).getmType() == Message.TYPE_LOG) {
        //    holder.mUsernameView.setVisibility(View.GONE);
        //    holder.mMessageView.setText(messageRealmResults.get(position).getmMessage());
       // } else {
            //holder.mUsernameView.setVisibility(View.VISIBLE);
            holder.mUsernameView.setText(messageRealmResults.get(position).getmUsername());
            holder.mMessageView.setText(messageRealmResults.get(position).getmMessage());
       // }
    }

    @Override
    public int getItemCount() {
        return messageRealmResults.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mUsernameView;
        private TextView mMessageView;

        ViewHolder(View itemView) {
            super(itemView);
            mUsernameView = (TextView) itemView.findViewById(R.id.username);
            mMessageView = (TextView) itemView.findViewById(R.id.message);

        }

    }
}
