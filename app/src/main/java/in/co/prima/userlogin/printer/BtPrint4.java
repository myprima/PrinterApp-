package in.co.prima.userlogin.printer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.co.prima.userlogin.R;
import in.co.prima.userlogin.activity.LoginActivity;
import in.co.prima.userlogin.activity.PlanActivity;
import in.co.prima.userlogin.activity.RegisterActivity;
import in.co.prima.userlogin.app.AppConfig;
import in.co.prima.userlogin.app.AppController;
import in.co.prima.userlogin.helper.SQLiteHandler;
import in.co.prima.userlogin.helper.SessionManager;

import static java.util.Collections.replaceAll;

// Created by md
//ends

public class BtPrint4 extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private SQLiteHandler db;
    private Button btnLogout;
    private String[] state = { "1", "2", "3", "4",
            "5", "6", "7", "8",
            "9","10" };
    Spinner spinnerOsversions;
    TextView selVersion;
    btPrintFile btPrintService = null;
    TextView plan_d;
    // Layout Views
//    private TextView mTitle;
    private EditText mRemoteDevice;
    Button mConnectButton;
    // Debugging
    private static final String TAG1 = "btprint";
    private static final boolean D = true;
    TextView voucher;
    TextView tvData;
    TextView mLog = null;
    Button mBtnExit = null;
    Button mBtnScan = null;
    //Button mBtnBrowseForFile = null;
    private ProgressDialog pDialog;
    Button mBtnSelectFile;
    TextView mTxtFilename;
    Button mBtnPrint;
   
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Intent request codes for demo files list
    private static final int REQUEST_SELECT_DEMO_FILE = 3;
    // Intent request codes for file browser
    private static final int REQUEST_SELECT_FILE = 4;
    private SessionManager session;

    BluetoothAdapter mBluetoothAdapter = null;

    View _view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //show
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btprint_main);
        Intent intent = getIntent();
        final String stringData= intent.getStringExtra("groupname");
        final String netid= intent.getStringExtra("netid");
        final String username = intent.getStringExtra("username");
        TextView strdata = (TextView) findViewById(R.id.planname);
        db = new SQLiteHandler(getApplicationContext());
        final HashMap<String, String> user = db.getUserDetails();
        plan_d = (TextView) findViewById(R.id.check);
        plan_d.setVisibility(View.INVISIBLE);
        strdata.setText(stringData);
        btnLogout = (Button) findViewById(R.id.btnLogout);
       /* final String netid = user.get("created_at");*/

        voucher = (TextView) findViewById(R.id.check);
        selVersion = (TextView) findViewById(R.id.selVersion);
        spinnerOsversions = (Spinner) findViewById(R.id.osversions);
        ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, state);
        adapter_state
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOsversions.setAdapter(adapter_state);
        spinnerOsversions.setOnItemSelectedListener(this);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
      

        mRemoteDevice = (EditText) findViewById(R.id.remote_device);
        mRemoteDevice.setText(R.string.bt_default_address);
       
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser(username);
            }
        });
        //connect button
        mConnectButton = (Button) findViewById(R.id.buttonConnect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToDevice();
            }
        });

        addLog("btprint2 started");

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        
        mBtnScan = (Button) findViewById(R.id.button_scan);
        mBtnScan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startDiscovery();
            }
        });

      

        mBtnPrint = (Button) findViewById(R.id.btnPrintFile);
        mBtnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String no_of_voucher ="1";
                no_of_voucher = (String) voucher.getText();
                Log.e(TAG1, "Exception in printFile:outside " +netid);
                 getVoucher(stringData,no_of_voucher,netid,username);

                String result = plan_d.getText().toString();
                Log.e(TAG1, "Out of getVoucher "+result);
              

            }
        });
       
    }


    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG1, "++ ON START ++");

        if (mBluetoothAdapter != null) {
             if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                if (btPrintService == null)
                    setupComm();
                addLog("starting print service...");//if (mChatService == null) setupChat();
            }
        }
    }
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, PlanActivity.class));
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");
        addLog("onResume");
      
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (btPrintService != null) btPrintService.stop();
        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }
      
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    byte[] escpQuery() {
        byte[] buf;
        String sBuf = "?{QST:HW}";
        ByteBuffer buf2;
        Charset charset = Charset.forName("UTF-8");
        buf2 = charset.encode(sBuf);
        buf2.put(0, (byte) 0x1B);
        return buf2.array();
    }

  

   public void printFile(String result) {
      
        String[] separated = result.split(",");
        int length = separated.length;
        for (int i = 0; i < length; i++) {
            String Voucher_no = separated[i];
            try {


                String Content = "       Company Name\n" +
                        "\" +\n" +
                        "                \"       8511 WHITESBURG DR\\r\\n\" +\n" +
                        "                \"      HUNTSVILLE, AL 35802\\r\\n\" +\n" +
                        "                \"         (256)585-6389\\r\\n\\r\\n\" +\n" +
                        "                \" Merchant ID: 1312\\r\\n\" +\n" +
                        "                \" Ref #: 0092\\r\\n\\r\\n\" +\n" +
                        "                \"\u001Bw)      Voucher \\r\\n\" +\n" + Voucher_no +
                        "                \"\u001Bw( XXXXXXXXXXX4003\\r\\n\" +\n" +
                        "                \" AMEX       Entry Method: Swiped\\r\\n\\r\\n\\r\\n\" +\n" +
                        "                \" Total:               $    53.22\\r\\n\\r\\n\\r\\n\" +\n" +
                        "                \" 12/21/12               13:41:23\\r\\n\" +\n" +
                        "                \" Inv #: 000092 Appr Code: 565815\\r\\n\" +\n" +
                        "                \" Transaction ID: 001194600911275\\r\\n\" +\n" +
                        "                \" Apprvd: Online   Batch#: 000035\\r\\n\\r\\n\\r\\n\" +\n" +
                        "                \"          Cutomer Copy\\r\\n\" +\n" +
                        "                \"           Thank You!\n" +
                        "\n" +
                        "\n" +
                        "\n";
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                String Content1 = "             Company\n\n\n" +
                        "Date " + currentDateTimeString + "\n" +
                        "Plan Name :    Plan" +
                        "Your WIFI Voucher code is :\n-----------------------------------------\n                 " + Voucher_no +
                        "\n----------------------------------------- \n \n" +
                        "\n" +
                        "                      -Powered by Company name\n" +

                        "\n" +
                        "\n";
                File myFile = new File("/sdcard/mysdfile.txt");
                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter =
                        new OutputStreamWriter(fOut);
                myOutWriter.append(Content1);
                myOutWriter.close();
                fOut.close();
            } catch (Exception e) {
            }


            String fileName = "/sdcard/mysdfile.txt";
            if (btPrintService.getState() != btPrintFile.STATE_CONNECTED) {
                myToast("Please connect first!", "Error");
                return;
            }

            if (Voucher_no.length() > 0) {      // here changed
                InputStream inputStream = null;
                ByteArrayInputStream byteArrayInputStream;
                Integer totalWrite = 0;
                StringBuffer sb = new StringBuffer();
                try {
                    if (fileName.startsWith("/")) {
                        inputStream = new FileInputStream(fileName);
                        addLog("Using regular file: '" + fileName + "'");
                    } else {
                        inputStream = this.getAssets().open(fileName);
                    }

                    byte[] buf = new byte[2048];
                    int readCount = 0;
                    do {
                        readCount = inputStream.read(buf);
                        if (readCount > 0) {
                            totalWrite += readCount;
                            byte[] bufOut = new byte[readCount];
                            System.arraycopy(buf, 0, bufOut, 0, readCount);
                            btPrintService.write(bufOut);
                        }
                    } while (readCount > 0);
                    inputStream.close();
                    addLog(String.format("printed " + totalWrite.toString() + " bytes"));
                    Toast.makeText(getApplicationContext(),
                            "Successfull Printed", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    printerror(Voucher_no);
                    Log.e(TAG, "Exception in printFile: " + e.getMessage());
                    addLog("printing failed!" + fileName);
                }
            } else {
                addLog("no demo file");
            }
        }
    }

    void myToast(String sInfo, String sTitle){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));

        try {
            Resources resources = getResources();
        }catch(Exception e){
            Log.e(TAG, "can not assign toast image: "+ e.getMessage());
        }

        ((TextView) layout.findViewById(R.id.toast_text)).setText(sInfo);
        TextView txt = (TextView)layout.findViewById(R.id.toast_text);
        if(sInfo.length()>10) {
            float textSize=txt.getTextSize(); //size in pixels
            textSize=textSize/2;
            txt.setTextSize(textSize);
        }
        Toast toast = new Toast(getBaseContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    void myToast(String sInfo){
        myToast(sInfo, "Information");
    }

    private void ensureDiscoverable() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void addLog(String s) {
        Log.d(TAG, s);
        int scrollAmount=0;
        try {
            scrollAmount = mLog.getLayout().getLineTop(mLog.getLineCount()) - mLog.getHeight();
        }catch(NullPointerException e){
            scrollAmount=0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    boolean bDiscoveryStarted = false;
    void startDiscovery() {
        if (bDiscoveryStarted)
            return;
        bDiscoveryStarted = true;
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    boolean bFileBrowserStarted=false;
    boolean bFileListStared = false;
    void startFileList() {
        if (bFileListStared)
            return;
        bFileListStared = true;
  }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuScan:
                startDiscovery();
                return true;
            case R.id.mnuDiscoverable:
                ensureDiscoverable();
                return true;
            case R.id.mnuFilelist:
                startFileList();
                return true;
        }
        return false;
    }

    void printESCP() {
        if (btPrintService != null) {
            if (btPrintService.getState() == btPrintFile.STATE_CONNECTED) {
                String message = btPrintService.printESCP();
                byte[] buf = message.getBytes();
                btPrintService.write(buf);
                addLog("ESCP printed");
            }
        }
    }

    private void setupComm() {
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.id.remote_device);
        Log.d(TAG, "setupComm()");
        btPrintService = new btPrintFile(this, mHandler);
        if (btPrintService == null)
            Log.e(TAG, "btPrintService init() failed");
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgTypes.MESSAGE_STATE_CHANGE:
                    Bundle bundle = msg.getData();
                    int status = bundle.getInt("state");
                    if (D)
                        Log.i(TAG, "handleMessage: MESSAGE_STATE_CHANGE: " + msg.arg1);  //arg1 was not used! by btPrintFile
                    setConnectState(msg.arg1);
                    switch (msg.arg1) {
                        case btPrintFile.STATE_CONNECTED:
                            addLog("connected to: " + mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            Log.i(TAG, "handleMessage: STATE_CONNECTED: " + mConnectedDeviceName);
                            break;
                        case btPrintFile.STATE_CONNECTING:
                            addLog("connecting...");
                            Log.i(TAG, "handleMessage: STATE_CONNECTING: " + mConnectedDeviceName);
                            break;
                        case btPrintFile.STATE_LISTEN:
                            addLog("connection ready");
                            Log.i(TAG, "handleMessage: STATE_LISTEN");
                            break;
                        case btPrintFile.STATE_IDLE:
                            addLog("STATE_NONE");
                            Log.i(TAG, "handleMessage: STATE_NONE: not connected");
                            break;
                        case btPrintFile.STATE_DISCONNECTED:
                            addLog("disconnected");
                            Log.i(TAG, "handleMessage: STATE_DISCONNECTED");
                            break;
                    }
                    break;
                case msgTypes.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case msgTypes.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    addLog("recv>>>" + readMessage);
                    break;
                case msgTypes.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(msgTypes.DEVICE_NAME);
                    //Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    myToast(mConnectedDeviceName, "Connected");
                    Log.i(TAG, "handleMessage: CONNECTED TO: " + msg.getData().getString(msgTypes.DEVICE_NAME));
                    //printESCP();
                    updateConnectButton(false);

                    break;
                case msgTypes.MESSAGE_TOAST:
//                    Toast toast = Toast.makeText(getApplicationContext(), msg.getData().getString(msgTypes.TOAST), Toast.LENGTH_SHORT);//.show();
//                    toast.setGravity(Gravity.CENTER,0,0);
//                    toast.show();
                    myToast(msg.getData().getString(msgTypes.TOAST));
                    Log.i(TAG, "handleMessage: " + msg.getData().getString(msgTypes.TOAST));
                    addLog(msg.getData().getString(msgTypes.TOAST));
                    break;
                case msgTypes.MESSAGE_INFO:
                    addLog(msg.getData().getString(msgTypes.INFO));
                    //mLog.append(msg.getData().getString(msgTypes.INFO));
                    //mLog.refreshDrawableState();
                    String s = msg.getData().getString(msgTypes.INFO);
                    if (s.length() == 0)
                        s = String.format("int: %i" + msg.getData().getInt(msgTypes.INFO));
                    Log.i(TAG, "handleMessage: INFO: " + s);
                    break;
            }
        }
    };

    void connectToDevice() {
        String remote = mRemoteDevice.getText().toString();
        if (remote.length() == 0)
            return;
        if (btPrintService.getState() == btPrintFile.STATE_CONNECTED) {
            btPrintService.stop();
            setConnectState(btPrintFile.STATE_DISCONNECTED);
            return;
        }

        String sMacAddr = remote;
        if (sMacAddr.contains(":") == false && sMacAddr.length() == 12)
        {
            char[] cAddr = new char[17];

            for (int i=0, j=0; i < 12; i += 2)
            {
                sMacAddr.getChars(i, i+2, cAddr, j);
                j += 2;
                if (j < 17)
                {
                    cAddr[j++] = ':';
                }
            }

            sMacAddr = new String(cAddr);
        }
        BluetoothDevice device;
        try {
            device = mBluetoothAdapter.getRemoteDevice(sMacAddr);
        }catch (Exception e){
            myToast("Invalid BT MAC address");
            device=null;
        }

        if (device != null) {
            addLog("connecting to " + sMacAddr);
            btPrintService.connect(device);
        } else {
            addLog("unknown remote device!");
        }
    }

    void connectToDevice(BluetoothDevice _device) {
        if (_device != null) {
            addLog("connecting to " + _device.getAddress());
            btPrintService.connect(_device);
        } else {
            addLog("unknown remote device!");
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    String curFileName = data.getStringExtra("GetFileName");
                    String curPath = data.getStringExtra("GetPath");
                    if(!curPath.endsWith("/")) {
                        curPath += "/";
                    }
                    String fullName=curPath+curFileName;
                    mTxtFilename.setText(fullName);
                    addLog("Filebrowser Result_OK: '"+fullName+"'");
                }
                bFileBrowserStarted=false;
                break;

            case REQUEST_CONNECT_DEVICE:
                addLog("onActivityResult: requestCode==REQUEST_CONNECT_DEVICE");
                if (resultCode == Activity.RESULT_OK) {
                    addLog("resultCode==OK");
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    addLog("onActivityResult: got device=" + address);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mRemoteDevice.setText(device.getAddress());
                    // Attempt to connect to the device
                    addLog("onActivityResult: connecting device...");
                    //btPrintService.connect(device);
                    connectToDevice(device);
                }
                bDiscoveryStarted = false;
                break;
            case REQUEST_ENABLE_BT:
                addLog("requestCode==REQUEST_ENABLE_BT");
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "onActivityResult: resultCode==OK");
                    // Bluetooth is now enabled, so set up a chat session
                    Log.i(TAG, "onActivityResult: starting setupComm()...");
                    setupComm();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "onActivityResult: BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    void setConnectState(Integer iState) {
        switch (iState) {
            case btPrintFile.STATE_CONNECTED:
                updateConnectButton(true);
                break;
            case btPrintFile.STATE_DISCONNECTED:
                updateConnectButton(false);
                break;
            case btPrintFile.STATE_CONNECTING:
                addLog("connecting...");
                break;
            case btPrintFile.STATE_LISTEN:
                addLog("listening...");
                break;
            case btPrintFile.STATE_IDLE:
                addLog("state none");
                break;
            default:
                addLog("unknown state var " + iState.toString());
        }
    }

    void updateConnectButton(boolean bConnected){
        if(bConnected) {
            mConnectButton.setText(R.string.button_disconnect_text);
            /*mConnectButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disconnectme, 0, 0, 0);*/
        }
        else{
            mConnectButton.setText(R.string.button_connect_text);
           /* mConnectButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.connectme, 0,0,0);*/
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        spinnerOsversions.setSelection(i);
        String selState = (String) spinnerOsversions.getSelectedItem();
        voucher.setText(selState);
       // voucher.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    // check logout
    private void logoutUser(final String username)
    {
        TelephonyManager tManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        final String macid = tManager.getDeviceId();

        String tag_string_req = "req_login";
        //pDialog.setMessage("Logging out ...");
        //showDialog();
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGOUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "Login Response: " + response.toString());
/*                hideDialog();*/


                try {
                    JSONObject jObj1 = new JSONObject(response);
                    int error = jObj1.getInt("err");

                    // Check for error node in json
                    if (error == 0) {
                        // user successfully logged in
                        // Create login session
                        // Launch main activity
                        session.setLogin(false);
                        db.deleteUsers();
                        Intent intent = new Intent(BtPrint4.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else
                    {
                        // Error in login. Get the error message
                        String errorMsg = jObj1.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e)
                {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
              /*  hideDialog();*/
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("macid", macid);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
            /*session.setLogin(false);*/
        db.deleteUsers();
    }
//
private void printerror(final String voucher)
{
    TelephonyManager tManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
    final String macid = tManager.getDeviceId();

    String tag_string_req = "req_login";
    //pDialog.setMessage("Logging out ...");
    //showDialog();
    StringRequest strReq = new StringRequest(Method.POST,
            AppConfig.URL_LOGOUT, new Response.Listener<String>() {

        @Override
        public void onResponse(String response)
        {
            Log.d(TAG, "Login Response: " + response.toString());
/*                hideDialog();*/


            try {
                JSONObject jObj1 = new JSONObject(response);
                int error = jObj1.getInt("err");

                // Check for error node in json
                if (error == 0) {
                    Toast.makeText(getApplicationContext(),
                            "error code send successfully", Toast.LENGTH_LONG).show();
                } else
                {
                    // Error in login. Get the error message
                    String errorMsg = jObj1.getString("error_msg");
                    Toast.makeText(getApplicationContext(),
                            errorMsg, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e)
            {
                // JSON error
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }, new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Login Error: " + error.getMessage());
            Toast.makeText(getApplicationContext(),
                    error.getMessage(), Toast.LENGTH_LONG).show();
              /*  hideDialog();*/
        }
    }) {

        @Override
        protected Map<String, String> getParams() {
            // Posting parameters to login url
            Map<String, String> params = new HashMap<String, String>();
            params.put("voucher", voucher);

            return params;
        }

    };

    // Adding request to request queue
    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
            /*session.setLogin(false);*/
    db.deleteUsers();
}
    //
    public void getVoucher(final String stringData, final String no_of_voucher, final String netid,final String username )
    {
        TelephonyManager tManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        final String macid = tManager.getDeviceId();
        String tag_string_req = "req_loginqqqqqqq";

       // pDialog.setMessage("Getting voucher......");
        Log.e(TAG, "enter into getvoucher " +netid+stringData+no_of_voucher);
       // showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_VOUCHER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "Login Response: " + response.toString());
               // hideDialog();


                try {
                    JSONObject jObj1 = new JSONObject(response);
                    Log.d(TAG, "Login Response: " + jObj1.toString());
                    JSONObject jObj = jObj1.getJSONObject("result");
                    int error = jObj1.getInt("err");

                    if (error == 0) {
                        int i = 0;
                        JSONArray jarr = jObj.getJSONArray("data");
                        for(i=0;i<jarr.length();i++) {
                            String v = jarr.getString(i);
                            printFile(v);
                            Log.e(TAG1, "Exception in array " +jarr);
                            Log.e(TAG1, "Exception in printFile:outside " +v);

                        }
                    } else
                    {
                        Log.d(TAG, "Getvoucher Response: in else  " + jObj1.toString());
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e)
                {
                    Log.d(TAG, "Getvoucher Response: in catchblock  ");
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                /*hideDialog();*/
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("netid",netid);
                params.put("no_of_vouchers",no_of_voucher);
                params.put("groupname",stringData);
                params.put("macid", macid);
                params.put("device","POS");
                params.put("username",username);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("accept-encoding", "");
                return headers;
            }

        };
        Log.e(TAG, "enter into request " +strReq);
        // Adding request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strReq);

      /*  AppController.getInstance().addToRequestQueue(strReq, tag_string_req);*/
    }
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }



}