package com.example.hackabull;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;
import static com.example.hackabull.MainActivity.EXTRA_ADDRESS;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class PublicMessageAdapter extends RecyclerView.Adapter<PublicMessageAdapter .ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView messageTextView;
        LinearLayout box;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            messageTextView = (TextView) itemView.findViewById(R.id.message_content);
            box = itemView.findViewById(R.id.box);
        }
    }

    private List<PublicMessage> mContacts;
    private Context mContext;
    private  String mAddress;

    // Pass in the contact array into the constructor
    public PublicMessageAdapter (List<PublicMessage> contacts, Context context, String address) {
        mContacts = contacts;
        mContext = context;
        mAddress = address;

    }


    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_public_message, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // Get the data model based on position
        PublicMessage contact = mContacts.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(contact.getName());

        // Set item views based on your views and data model
        TextView textView2 = viewHolder.messageTextView;
        textView2.setText(contact.getMessage());

        viewHolder.box.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PrivateActivity.class);
                String address = mAddress;
                intent.putExtra(EXTRA_ADDRESS, address);
                mContext.startActivity(intent);
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mContacts.size();
    }
}