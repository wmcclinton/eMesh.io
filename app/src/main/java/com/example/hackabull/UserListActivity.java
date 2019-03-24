package com.example.hackabull;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import static com.example.hackabull.MainActivity.EXTRA_ADDRESS;
import static com.example.hackabull.MainActivity.personal_id;

public class UserListActivity extends AppCompatActivity {

    ArrayList<Contact> contacts;
    RecyclerView rvContacts;

    Button btn_public;
    SwipeRefreshLayout pullToRefresh;

    ContactsAdapter adapter;

    String new_messages = null;

    public BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "CommsActivity";
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    public String id = personal_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...
        // Lookup the recyclerview in activity layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        final Intent intent = getIntent();
        final String address = intent.getStringExtra(com.example.hackabull.MainActivity.EXTRA_ADDRESS);

        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);

        // Initialize contacts
        contacts = Contact.createContactsList(20);
        // Create adapter passing in the sample user data
        adapter = new ContactsAdapter(contacts, UserListActivity.this, address);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);
        // Set layout manager to position the items

        // That's all!
        btn_public = findViewById(R.id.btn_public);
        btn_public.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserListActivity.this, PublicActivity.class);
                intent.putExtra(EXTRA_ADDRESS, address);
                startActivity(intent);
            }
        });

        try {
            synchronized(contacts){
                contacts.clear();
                adapter.notifyDataSetChanged();
            }
            BufferedReader bReader = new BufferedReader(new InputStreamReader(openFileInput("official_messages.txt")));
            String line;
            StringBuffer text = new StringBuffer();
            int i = 0;
            while ((line = bReader.readLine()) != null) {
                String[] parts = line.split("--:--:--:--")[0].split(":::");
                if(parts.length > 2) {
                    contacts.add(i, new Contact(parts[0].split("_")[1],parts[1], true));
                    adapter.notifyItemInserted(i);
                    i++;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                final Intent intent = getIntent();
                Log.d("MESSAGE",address);
                final BluetoothDevice device = BTAdapter.getRemoteDevice(address);
                try {
                    new UserListActivity.ConnectThread(device, false).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("MESSAGE","New_Messages: " + new_messages);

                File path = getFilesDir();
                File file = new File(path, "official_messages.txt");
                FileOutputStream stream = null;

                try {
                    stream = new FileOutputStream(file);
                    //stream = new FileOutputStream(file, true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    try {
                        String msg = new_messages;
                        stream.write(msg.getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    synchronized(contacts){
                        contacts.clear();
                        adapter.notifyDataSetChanged();
                    }
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(openFileInput("global_messages.txt")));
                    String line;
                    StringBuffer text = new StringBuffer();
                    int i = 0;
                    while ((line = bReader.readLine()) != null) {
                        Log.d("FILE",line);
                        String[] parts = line.split("--:--:--:--")[0].split(":::");
                        if(parts.length > 2) {
                            contacts.add(i, new Contact(parts[0].split("_")[1],parts[1], true));
                            adapter.notifyItemInserted(i);
                            i++;
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                pullToRefresh.setRefreshing(false);
            }
        });

    }

    public class ConnectThread extends Thread {
        private ConnectThread(BluetoothDevice device, Boolean sendMessage) throws IOException {
            /*if (mmSocket != null) {
                if(mmSocket.isConnected()) {
                    send();
                }
            }*/
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
                tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            BTAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.v(TAG, "Connection exception!");
                try {
                    mmSocket.close();
                    /*mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                    mmSocket.connect();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } */
                } catch (IOException closeException) {

                }
            }
            send(sendMessage);
        }

        public void send(Boolean sendMessage) throws IOException {
            EditText mEdit = (EditText)findViewById(R.id.editText);
            String to_id = "???";
            String msg = null;
            msg = "???" + ":::" + id + ":::" + "???" + ":::" + "???" + ":::" + "NODE_INFORMATION";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());
            receive();
        }

        public void receive() throws IOException {
            InputStream mmInputStream = mmSocket.getInputStream();
            byte[] buffer = new byte[256];
            int bytes;

            try {
                bytes = mmInputStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                Log.d(TAG, "Received: " + readMessage);
                Log.d("MESSAGE","Recieved: " + readMessage);
                new_messages = readMessage;
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Problems occurred!");
                return;
            }
        }
    }

}