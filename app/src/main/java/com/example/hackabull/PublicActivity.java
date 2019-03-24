package com.example.hackabull;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Scaledrone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static com.example.hackabull.MainActivity.EXTRA_ADDRESS;
import static com.example.hackabull.MainActivity.personal_id;

public class PublicActivity extends AppCompatActivity {
    SwipeRefreshLayout pullToRefresh;

    ArrayList<PublicMessage> contacts;
    RecyclerView rvContacts;
    Button btn_private;
    private Scaledrone scaledrone;
    private String roomName = "observable-room";

    private EditText editText;

    public BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "CommsActivity";
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    public String id = personal_id;
    String address;
    PublicMessageAdapter adapter;

    String new_messages = null;

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...
        // Lookup the recyclerview in activity layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public);

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
        }


        final Intent intent = getIntent();
        address = intent.getStringExtra(com.example.hackabull.MainActivity.EXTRA_ADDRESS);

        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);

        // Initialize contacts
        contacts = PublicMessage.createContactsList(0);

        // Create adapter passing in the sample user data
        adapter = new PublicMessageAdapter(contacts, PublicActivity.this, address);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);
        // Set layout manager to position the items

        // That's all!
        btn_private = findViewById(R.id.btn_private);
        btn_private.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PublicActivity.this, UserListActivity.class);
                intent.putExtra(EXTRA_ADDRESS, address);
                startActivity(intent);
            }
        });

        editText = (EditText) findViewById(R.id.editText);

        //messageAdapter = new MessageAdapter(this);
        //messagesView = (ListView) findViewById(R.id.messages_view);
        //messagesView.setAdapter(messageAdapter);

        //MemberData data = new MemberData(getRandomName(), getRandomColor());

        //scaledrone = new Scaledrone(channelID, data);
        /*scaledrone.connect(new Listener() {
            @Override
            public void onOpen() {
                System.out.println("Scaledrone connection open");
                scaledrone.subscribe(roomName, PrivateActivity.this);
            }

            @Override
            public void onOpenFailure(Exception ex) {
                System.err.println(ex);
            }

            @Override
            public void onFailure(Exception ex) {
                System.err.println(ex);
            }

            @Override
            public void onClosed(String reason) {
                System.err.println(reason);
            }
        });
        */
        ////////////

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Log.d("MESSAGE",address);
        final BluetoothDevice device = BTAdapter.getRemoteDevice(address);
        try {
            new PublicActivity.ConnectThread(device, false).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("MESSAGE","New_Messages: " + new_messages);

        File path = getFilesDir();
        File file = new File(path, "public_messages.txt");
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
            BufferedReader bReader = new BufferedReader(new InputStreamReader(openFileInput("public_messages.txt")));
            String line;
            StringBuffer text = new StringBuffer();
            int i = 0;
            while ((line = bReader.readLine()) != null) {
                Log.d("FILE",line);
                String[] parts = line.split("--:--:--:--")[1].split(":::");
                if (parts.length > 2) {
                    contacts.add(i, new PublicMessage(parts[0].split("_")[1], parts[1], true));
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
                String message = editText.getText().toString();

                final Intent intent = getIntent();
                Log.d("MESSAGE",address);
                final BluetoothDevice device = BTAdapter.getRemoteDevice(address);
                try {
                    new PublicActivity.ConnectThread(device, false).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("MESSAGE","New_Messages: " + new_messages);

                File path = getFilesDir();
                File file = new File(path, "public_messages.txt");
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
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(openFileInput("public_messages.txt")));
                    String line;
                    StringBuffer text = new StringBuffer();
                    int i = 0;
                    while ((line = bReader.readLine()) != null) {
                        Log.d("FILE",line);
                        String[] parts = line.split("--:--:--:--")[1].split(":::");
                        if (parts.length > 2) {
                            contacts.add(i, new PublicMessage(parts[0].split("_")[1], parts[1], true));
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

    public void sendMessage(View view) {
        String message = editText.getText().toString();

        final Intent intent = getIntent();
        Log.d("MESSAGE",address);
        final BluetoothDevice device = BTAdapter.getRemoteDevice(address);
        try {
            new PublicActivity.ConnectThread(device, true).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            String to_id = "Tessy_3524540358";
            String msg = null;
            if(sendMessage) {
                msg = getRandomString(256) + ":::" + id + ":::" + mEdit.getText().toString() + ":::" + "???" + ":::" + "NODE_INFORMATION";
            }
            else {
                msg = "???" + ":::" + id + ":::" +"???" + ":::" + to_id + ":::" + "NODE_INFORMATION";
            }
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());
            receive();
            editText.getText().clear();

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