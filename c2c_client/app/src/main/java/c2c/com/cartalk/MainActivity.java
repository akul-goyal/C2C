package c2c.com.cartalk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback{

    //---Variables----------------------------------------------------------------------------------

    // UI
    FrameLayout viewContainer;
    Button editInfoButton;
    Button sendMessage;
    Button getMessage;
    boolean editProfileState = false;
    TextView name;
    TextView plate;
    TextView type;
    TextView color;
    EditText editName;
    EditText editPlate;
    EditText editType;
    EditText editColor;
    String nameString;
    String plateString;
    String typeString;
    String colorString;
    String message;

    // To Server
    JSONObject locationPackage;
    JSONObject messagePackage;
    JSONObject messageRequestPackage;

    // Location
    LocationManager locationHelper;
    double longitude;
    double latitude;

    // Server and Memory
    ServerConnection server;
    MemoryManager memory;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    ArrayList<String> nearbyCars = new ArrayList<>();


    //---Set Up-------------------------------------------------------------------------------------
    /**
        Functions for setting up the activity
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up memory and server managers
        memory = new MemoryManager();
        server = new ServerConnection();



        // Views
        setFullScreen();
        setContentView(R.layout.activity_main);
        setViews();


        // set up location
        locationHelper = new LocationManager(this);
        locationHelper.checkpermission();
        if (locationHelper.checkPlayServices()) {
            locationHelper.buildGoogleApiClient();
        }

        // set up timer to run location pings
        setUpLocationPingLoop();

        //
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
    }



    // every 30 seconds, gets location and sends to server
    void setUpLocationPingLoop() {
        final Handler handler = new Handler();
        final int delay = 30000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                setUpLocationPackageForServer();
                server.sendLocationToServer(locationPackage);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }



    // sets app to full screen
    void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    // sets up view ids and button clicks
    void setViews() {

        // Edit Info Button
        editInfoButton = (Button)findViewById(R.id.editProfileButton);
        editInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchEditProfileView();
            }
        });

        sendMessage = (Button)findViewById(R.id.activate_voice);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceRecognitionActivity();
            }
        });

        getMessage = (Button)findViewById(R.id.get_messages);
        getMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpMessageRequest();
                String response = server.sendMessageRequestToServer(messageRequestPackage);
            }
        });

        // Profile Info
        viewContainer = (FrameLayout) findViewById(R.id.profileFrame);
        setUpViewProfile();
    }


    // called to initially set up view profile on startup
    void setUpViewProfile() {
        View inflatedLayout= getLayoutInflater().inflate(R.layout.view_profile, null, false);
        name = inflatedLayout.findViewById(R.id.name);
        plate = inflatedLayout.findViewById(R.id.plateNumber);
        type = inflatedLayout.findViewById(R.id.carType);
        color = inflatedLayout.findViewById(R.id.carColor);

        String[] userInfo = memory.getProfileInfo(this);
        name.setText(userInfo[0]);
        plate.setText(userInfo[1]);
        type.setText(userInfo[2]);
        color.setText(userInfo[3]);

        viewContainer.addView(inflatedLayout);
        editInfoButton.setText(getResources().getString(R.string.edit_button));
    }

    // switch between view and edit profile. saves info to memory
    void switchEditProfileView() {
        if (editProfileState) {

            // switch to VIEW, save Edit info

            // save edited info
            nameString = editName.getText().toString();
            plateString = editPlate.getText().toString();
            typeString = editType.getText().toString();
            colorString = editColor.getText().toString();
            memory.saveProfile(this, nameString, plateString, typeString, colorString);

            // switch to view
            viewContainer.removeAllViews();
            View inflatedLayout= getLayoutInflater().inflate(R.layout.view_profile, null, false);
            name = inflatedLayout.findViewById(R.id.name);
            plate = inflatedLayout.findViewById(R.id.plateNumber);
            type = inflatedLayout.findViewById(R.id.carType);
            color = inflatedLayout.findViewById(R.id.carColor);

            // set strings
            name.setText(nameString);
            plate.setText(plateString);
            type.setText(typeString);
            color.setText(colorString);

            viewContainer.addView(inflatedLayout);
            editInfoButton.setText(getResources().getString(R.string.edit_button));


        } else {

            // switch to EDIT
            viewContainer.removeAllViews();
            View inflatedLayout= getLayoutInflater().inflate(R.layout.edit_profile, null, false);
            editName = inflatedLayout.findViewById(R.id.edit_name);
            editPlate = inflatedLayout.findViewById(R.id.edit_plate);
            editType = inflatedLayout.findViewById(R.id.edit_type);
            editColor = inflatedLayout.findViewById(R.id.edit_color);


            // load info into edit texts from memory
            String[] profileInfo = memory.getProfileInfo(this);
            editName.setText(profileInfo[0]);
            editPlate.setText(profileInfo[1]);
            editType.setText(profileInfo[2]);
            editColor.setText(profileInfo[3]);

            viewContainer.addView(inflatedLayout);
            editInfoButton.setText(getResources().getString(R.string.save_button));

        }
        editProfileState = !editProfileState;
    }

    //---BlueTooth----------------------------------------------------------------------------------

    BluetoothAdapter mBluetoothAdapter = null;


    void changeDeviceName() {
        String[] info = memory.getProfileInfo(this);
        mBluetoothAdapter.setName(info[1]);
    }

    public String getLocalBluetoothName() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        String name = mBluetoothAdapter.getName();
        if (name==null) {
            System.out.println("Name is null!");
            name = mBluetoothAdapter.getAddress();
        }
        return name;
    }

    // discover devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                nearbyCars.add(deviceName);
            }
        }
    };

    //---Voice--------------------------------------------------------------------------------------

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say 'Hey Google' to send message");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        startVoiceRecognitionActivity();
    }




    //---Packaging Info-----------------------------------------------------------------------------
    /**
     * Functions that prepare json objects to send to server
     * Also functions that get that info
     */

    // set up messages_request
    void setUpMessageRequest() {
        String[] userInfo = memory.getProfileInfo(this);
        messageRequestPackage = new JSONObject();
        try {
            messageRequestPackage.put("plate_num", userInfo[1]);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        changeDeviceName();
    }
    // gets everything ready in locationMap
    /**
     * plate_num
     * car_type
     * car_color
     * longitude
     * latitude
     * send_time
     * nearby_cars
     */
    void setUpLocationPackageForServer() {

        getLocation();
        String[] userInfo = memory.getProfileInfo(this);

        locationPackage =  new JSONObject();
        try {
            locationPackage.put("plate_num", userInfo[1]);
            locationPackage.put("car_type", userInfo[2]);
            locationPackage.put("car_color", userInfo[3]);
            locationPackage.put("longitude", longitude);
            locationPackage.put("latitude", latitude);
            locationPackage.put("send_time", getTime());
            locationPackage.put("nearby_cars", nearbyCars.toString());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // gets everything ready for messagePackage
    /**
     *  message
     *  sender_plate_num
     *  sender_car_type
     *  sender_car_color
     *  target_plate_num
     *  target_car_type
     *  target_car_color
     *  longitude
     *  latitude
     *  send_time
     *  nearby_cars
     */
    void setUpMessagePackage(String message) {
        getLocation();
        String[] userInfo = memory.getProfileInfo(this);

        messagePackage =  new JSONObject();
        try {
            messagePackage.put("message", message);
            messagePackage.put("sender_plate_num", userInfo[1]);
            messagePackage.put("sender_car_type", userInfo[2]);
            messagePackage.put("sender_car_color", userInfo[3]);
            messagePackage.put("target_plate_num", null);
            messagePackage.put("target_car_type", null);
            messagePackage.put("target_car_color", null);
            messagePackage.put("longitude", longitude);
            messagePackage.put("latitude", latitude);
            messagePackage.put("send_time", getTime());
            messagePackage.put("nearby_cars", nearbyCars.toString());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // gets location
    void getLocation() {
        Location location = locationHelper.getLocation();
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    // gets current time in format: 'Jan 20, 2018 7:26:14 PM'
    String getTime() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }


    //---Activity Info------------------------------------------------------------------------------
    /**
     * Functions for dealing with activity states
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            setUpMessagePackage(matches.get(0).toString());
            server.sendMessageToServer(messagePackage);

            toast(matches.get(0).toString());
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        locationHelper.checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("Connection failed:", " ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        //mLastLocation = locationHelper.getLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        locationHelper.connectApiClient();
    }


    // Permission check functions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // redirects to utils
        locationHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    //---Debug Functions----------------------------------------------------------------------------
    /**
        Functions that are helpful for debugging - including toast()
     */

    // makes toasts easy
    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
