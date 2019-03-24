package com.example.hackabull;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

import static com.example.hackabull.MainActivity.EXTRA_ADDRESS;

public class PrivateActivity extends AppCompatActivity{

    // replace this with a real channelID from Scaledrone dashboard
    private String channelID = "";
    private String roomName = "observable-room";
    private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;

    //////////

    public BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "CommsActivity";
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    public String id = MainActivity.personal_id;

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public class ConnectThread extends Thread {
        private ConnectThread(BluetoothDevice device) throws IOException {
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
            send();
        }

        public void send() throws IOException {
            EditText mEdit = (EditText)findViewById(R.id.editText);
            String to_id = "Tessy_3524540358";
            String msg = getRandomString(256) + ":::" + id + ":::" + mEdit.getText().toString() + ":::" + to_id + ":::" + "NODE_INFORMATION";
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
                mmSocket.close();
                Toast.makeText(getApplicationContext(), "Message Sent",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PrivateActivity.this, UserListActivity.class);
                final String address = MainActivity.main_address;
                intent.putExtra(EXTRA_ADDRESS, address);
                finish();
                startActivity(intent);
            } catch (IOException e) {
                Log.e(TAG, "Problems occurred!");
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private);
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        editText = (EditText) findViewById(R.id.editText);

        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);


        ////////////

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(openFileInput("global_messages.txt")));
            String line;
            StringBuffer text = new StringBuffer();
            int i = 0;
            while ((line = bReader.readLine()) != null) {
                Log.d("FILE",line);
                String[] parts = line.split(":::");
                if(parts.length > 2) {
                    Log.d("TESTTEST",id + ":::" + parts[0]);
                    messageAdapter.add(new Message(parts[1],new MemberData(getRandomName(), getRandomColor()), id != parts[0]));

                    messageAdapter.notifyDataSetChanged();
                    i++;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mmSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent intent = new Intent(PrivateActivity.this, UserListActivity.class);
        final String address = MainActivity.main_address;;
        intent.putExtra(EXTRA_ADDRESS, address);
        finish();
        startActivity(intent);
    }

    public void sendMessage(View view) {
        String message = editText.getText().toString();

        final Intent intent = getIntent();
        final String address = MainActivity.main_address;
        final BluetoothDevice device = BTAdapter.getRemoteDevice(address);
        try {
            new ConnectThread(device).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};

        System.out.print("Enter Username: ");

        return (
                adjs[(int) Math.floor(Math.random() * adjs.length)] +
                        "_" +
                        nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
}

class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public MemberData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "MemberData{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}


